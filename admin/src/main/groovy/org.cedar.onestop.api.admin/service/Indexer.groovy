package org.cedar.onestop.api.admin.service

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.WordUtils
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.parse.ISOParser
import org.xml.sax.SAXException
import org.cedar.onestop.elastic.common.ElasticsearchConfig

import java.time.temporal.ChronoUnit

import static org.cedar.schemas.avro.psi.ValidDescriptor.*

@Slf4j
class Indexer {

  static Map validateMessage(String id, ParsedRecord messageMap) {

    // FIXME Improve testability of failures by creating an Enum for invalid messages

    def failure = [title: 'Invalid record']
    List<String> details = []

    def discovery = messageMap?.discovery
    if(discovery == null || discovery == Discovery.newBuilder().build()) {
      details << "Discovery metadata missing. No metadata to load into OneStop."
    }
    else {
      def analysis = messageMap?.analysis
      if(analysis == null || analysis == Analysis.newBuilder().build()) {
        details << "Analysis metadata missing. Cannot verify metadata quality for OneStop."
      }
      else {
        def titles = analysis.titles
        def identification = analysis.identification
        def temporal = analysis.temporalBounding
        def spatial = analysis.spatialBounding

        if (!identification?.fileIdentifierExists && !identification?.doiExists) {
          details << "Missing identifier - record contains neither a fileIdentifier nor a DOI"
        }
        if(messageMap.type == null || !identification.matchesIdentifiers) {
          details << "Metadata type error -- hierarchyLevelName is 'granule' but no parentIdentifier provided OR type unknown."
        }
        if (!titles.titleExists) {
          details << "Missing title"
        }
        if (temporal.beginDescriptor == INVALID) {
          details << "Invalid beginDate"
        }
        if (temporal.endDescriptor == INVALID) {
          details << "Invalid endDate"
        }
        if (temporal.beginDescriptor != UNDEFINED && temporal.endDescriptor != UNDEFINED && temporal.instantDescriptor == INVALID) {
          details << "Invalid instant-only date"
        }
        if (spatial.spatialBoundingExists && !spatial.isValid) {
          details << "Invalid geoJSON for spatial bounding"
        }
      }

    }

    if (details.size() > 0 ) {
      log.info("INVALID RECORD [ $id ]. VALIDATION FAILURES:  $details ")
      failure.detail = details.join(', ')
      failure.valid = false
      return failure
    }
    else {
      return [valid: true]
    }
  }

  static Map xmlToParsedRecord(String xmlDoc) {
    Map result = [:]
    def parsedRecordBuilder = ParsedRecord.newBuilder()
    try {
      // Convert XML to ParsedRecord -- Discovery and Analysis objects only
      log.debug('Parsing XML for discovery metadata')
      Discovery discovery = ISOParser.parseXMLMetadataToDiscovery(xmlDoc)
      log.debug('Analyzing discovery metadata')
      Analysis analysis = Analyzers.analyze(discovery)

      // Determine RecordType (aka granule or collection) from Discovery & Analysis info
      String parentIdentifier = discovery.parentIdentifier
      String hierarchyLevelName = discovery.hierarchyLevelName

      RecordType type
      if(hierarchyLevelName == null || hierarchyLevelName != 'granule') {
        type = RecordType.collection
      }
      else {
        // Pro-tip: In Java 11 future, "null || isBlank()" will suffice...
        if(parentIdentifier == null || parentIdentifier.isEmpty() || parentIdentifier.isAllWhitespace()) {
          // Set to null rather than throwing an error so that this can be caught at the validation step
          type = null
        }
        else {
          type = RecordType.granule
        }
      }

      parsedRecordBuilder.setType(type)
      parsedRecordBuilder.setDiscovery(discovery)
      parsedRecordBuilder.setAnalysis(analysis)
      ParsedRecord parsedRecord = parsedRecordBuilder.build()
      result.parsedRecord = parsedRecord
    }
    catch (SAXException e) {
      result.error = [
          title : 'Load request failed due to malformed XML.',
          detail: ExceptionUtils.getRootCauseMessage(e)
      ]
    }
    catch (Exception e) {
      result.error = [
          title : 'Load request failed due to malformed data.',
          detail: ExceptionUtils.getRootCauseMessage(e)
      ]
    }
    return result
  }

  static Map reformatMessageForSearch(ParsedRecord record) {
    Discovery discovery = record.discovery
    Analysis analysis = record.analysis

    Map discoveryMap = AvroUtils.avroToMap(discovery, true)

    // Records validated before getting to this point to no null record.type possible (always granule or collection)
    discoveryMap.type = record.type == RecordType.granule ?
        ElasticsearchConfig.TYPE_GRANULE : ElasticsearchConfig.TYPE_COLLECTION

    // create GCMD keywords
    Map gcmdKeywords = createGcmdKeyword(discovery)
    discoveryMap.putAll(gcmdKeywords)

    // create contacts ,creators and publishers
    Map<String, Set> partyData = parseResponsibleParties(discovery.responsibleParties)
    discoveryMap.putAll(partyData)

    // update temporal Bounding
    def temporalData = readyDatesForSearch(discovery.temporalBounding, analysis.temporalBounding)
    discoveryMap.temporalBounding = temporalData

    // drop fields
    discoveryMap.remove("responsibleParties")

    List<Service> services = discovery.services
    discoveryMap.remove('services')
    discoveryMap.serviceLinks = createServices(services)

    // create data format name list for this record
    discoveryMap.dataFormat = createDataFormat(discovery)

    // create protocol list (from links and service links of this record)
    // https://github.com/OSGeo/Cat-Interop/blob/master/LinkPropertyLookupTable.csv
    discoveryMap.linkProtocol = createLinkProtocol(discovery)
    discoveryMap.serviceLinkProtocol = createServiceLinkProtocol(discovery)

    discoveryMap.services = ''
    return discoveryMap
  }

  static List<Link> getLinksForService(Service service) {
    List<Link> operations = service?.operations
    if(operations) {
      return operations.sort()
    }
    return []
  }

  static List<Map> createServices(List<Service> services) {
    if(services) {
      return services.collect { service ->
        [
            title: service.title,
            alternateTitle: service.alternateTitle,
            description: service.description,
            links: AvroUtils.avroCollectionToList(getLinksForService(service), true).sort()
        ]
      }
    }
    return []
  }

  // create data format names
  static Set<String> createDataFormat(Discovery discovery) {
    Set<String> dataFormatKeywords = []

    List<DataFormat> dataFormats = discovery.dataFormats
    dataFormats.each { dataFormat ->

      // normalize (clean whitespace/trim/uppercase/nullify empty string) contents of DataFormat to check for uniqueness
      String dataFormatKeyword = normalizeHierarchicalDataFormat(dataFormat)
      if(dataFormatKeyword) {
        // adding to set ensures unique entries (with the help of normalization)
        dataFormatKeywords.addAll(tokenizeHierarchyKeyword(dataFormatKeyword))
      }
    }
    return dataFormatKeywords
  }

  // create link protocols
  static Set<String> createLinkProtocol(Discovery discovery) {
    // initialize set for unique protocols in links
    Set<String> linkProtocol = []

    // add links to linkProtocol set
    List<Link> links = discovery.links
    links.each { link ->
      def linkProtocolKeyword = normalizeLinkProtocol(link)
      if(linkProtocolKeyword) {
        linkProtocol.addAll(tokenizeHierarchyKeyword(linkProtocolKeyword))
      }
    }
    return linkProtocol
  }

  static Set<String> createServiceLinkProtocol(Discovery discovery) {
    // initialize set for unique protocols in service links
    Set<String> serviceLinkProtocol = []

    // add service links to serviceLinkProtocol set
    List<Link> serviceLinks = discovery.services.collect { getLinksForService(it) }.flatten()
    serviceLinks.each { serviceLink ->
      def serviceLinkProtocolKeyword = normalizeLinkProtocol(serviceLink)
      if(serviceLinkProtocolKeyword) {
        serviceLinkProtocol.addAll(tokenizeHierarchyKeyword(serviceLinkProtocolKeyword))
      }
    }
    return serviceLinkProtocol
  }

  // create GCMD keyword lists
  static Map createGcmdKeyword(Discovery discovery) {
    def gcmdScience = [] as Set
    def gcmdScienceServices = [] as Set
    def gcmdLocations = [] as Set
    def gcmdPlatforms = [] as Set
    def gcmdInstruments = [] as Set
    def gcmdProjects = [] as Set
    def gcmdHorizontalResolution = [] as Set
    def gcmdVerticalResolution = [] as Set
    def gcmdTemporalResolution = [] as Set
    def gcmdDataCenters = [] as Set

    //remove and create new keywords with out accession values
    def keywords = discovery.keywords.findAll({ keys ->
      keys.namespace != 'NCEI ACCESSION NUMBER'
    }).collect({AvroUtils.avroToMap(it, true)})

    keywords.each { group ->
      def namespace = group.namespace.toLowerCase() ?: ""
      def keywordsInGroup = group.values ?: null
      if (namespace.contains('gcmd') || namespace.contains('global change master directory')) {
        switch (namespace) {
          case { namespace.contains('science')}:
            keywordsInGroup.each { k ->
              def keyword = k.toLowerCase()
              if(keyword.startsWith('earth science services')) {
                def text = normalizeHierarchyKeyword(k)
                gcmdScienceServices.addAll(tokenizeHierarchyKeyword(text))
              } else if (keyword.startsWith('earth science')) {
                def text = normalizeHierarchyKeyword(k)
                gcmdScience.addAll(tokenizeHierarchyKeyword(text))
              }
            }
            break
          case { namespace.contains('location') || namespace.contains('place') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdLocations.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { namespace.contains('platform') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdPlatforms.add(text)
            }
            break
          case { namespace.contains('instrument') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdInstruments.add(text)
            }
            break
          case { namespace.contains('data center') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdDataCenters.add(text)
            }
            break
          case { namespace.contains('horizontal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdHorizontalResolution.add(text)
            }
            break
          case { namespace.contains('vertical data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdVerticalResolution.add(text)
            }
            break
          case { namespace.contains('temporal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdTemporalResolution.add(text)
            }
            break
          case { namespace.contains('project') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdProjects.add(text)
            }
            break
          default:
            // Namespace didn't meet our checks for known GCMD format
            break
        }
      }
    }

    return [
        keywords                : keywords,
        accessionValues         : [],  // FIXME this needs to be in place until we can use ES6 ignore_missing flags
        gcmdScienceServices     : gcmdScienceServices,
        gcmdScience             : gcmdScience,
        gcmdLocations           : gcmdLocations,
        gcmdInstruments         : gcmdInstruments,
        gcmdPlatforms           : gcmdPlatforms,
        gcmdProjects            : gcmdProjects,
        gcmdDataCenters         : gcmdDataCenters,
        gcmdHorizontalResolution: gcmdHorizontalResolution,
        gcmdVerticalResolution  : gcmdVerticalResolution,
        gcmdTemporalResolution  : gcmdTemporalResolution
    ]
  }

  /*
  Create contacts, creators and publishers from responsibleParties
  */

  static Map<String, String> parseParty(ResponsibleParty party) {
    String individualName = party.individualName ?: null
    String organizationName = party.organizationName ?: null
    String positionName = party.positionName ?: null
    String role = party.role ?: null
    String email = party.email ?: null
    String phone = party.phone ?: null
    return [
        individualName  : individualName,
        organizationName: organizationName,
        positionName    : positionName,
        role            : role,
        email           : email,
        phone           : phone
    ]
  }

  static Map<String, Set> parseResponsibleParties(List<ResponsibleParty> responsibleParties) {
    Set contacts = []
    Set contactRoles = ['pointOfContact', 'distributor']
    Set creators = []
    Set creatorRoles = ['resourceProvider', 'originator', 'principalInvestigator', 'author', 'collaborator', 'coAuthor']
    Set publishers = []
    Set publisherRoles = ['publisher']

    responsibleParties.each { party ->
      def parsedParty = parseParty(party)
      log.debug("parsedParty: $parsedParty")
      if (contactRoles.contains(parsedParty.role)) {
        contacts.add(parsedParty)
      }
      else if (creatorRoles.contains(parsedParty.role)) {
        creators.add(parsedParty)
      }
      else if (publisherRoles.contains(parsedParty.role)) {
        publishers.add(parsedParty)
      }
    }
    return [contacts: contacts, creators: creators, publishers: publishers]
  }

  static Map readyDatesForSearch(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    def beginDate, beginYear, endDate, endYear

    // If bounding is actually an instant, set search fields accordingly
    if (analysis.rangeDescriptor == TimeRangeDescriptor.INSTANT) {
      beginDate = analysis.instantUtcDateTimeString as String
      def year = parseYear(beginDate)

      // Add time and/or date to endDate based on precision
      switch (analysis.instantPrecision) {
        case ChronoUnit.DAYS.toString():
          // End of day
          endDate = "${bounding.instant}T23:59:59Z" as String
          break
        case ChronoUnit.YEARS.toString():
          if (!analysis.instantIndexable) {
            // Paleo date, so only return year value (null out dates)
            beginDate = null
            endDate = null
          }
          else {
            // Last day of year + end of day
            endDate = "${bounding.instant}-12-31T23:59:59Z" as String
          }
          break
        default:
          // Precision is NANOS so use instant value as-is
          endDate = beginDate
          break
      }

      return [
          beginDate: beginDate,
          beginYear: year,
          endDate: endDate,
          endYear: year
      ]
    }
    else {
      // If dates exist and are validSearchFormat (only false here if paleo, since we filtered out bad data earlier),
      // use value from analysis block where dates are UTC datetime normalized
      return [
          beginDate: analysis.beginDescriptor == VALID && analysis.beginIndexable ? analysis.beginUtcDateTimeString as String : null,
          endDate: analysis.endDescriptor == VALID && analysis.endIndexable ? analysis.endUtcDateTimeString as String : null,
          beginYear: parseYear(analysis.beginUtcDateTimeString as String),
          endYear: parseYear(analysis.endUtcDateTimeString as String)
      ]
    }
  }

  static Long parseYear(String utcDateTime) {
    if (!utcDateTime) {
      return null
    }
    else {
      // Watch out for BCE years
      return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)))
    }

  }

  // helper functions
  static String normalizeHierarchicalDataFormat(DataFormat dataFormat) {

    String name = null
    String version = null
    if (dataFormat.name) {
      name = cleanInternalKeywordWhitespace(dataFormat.name).trim().toUpperCase()
      if (dataFormat.version) {
        version = cleanInternalKeywordWhitespace(dataFormat.version).trim().toUpperCase()
      }
    }

    name = name?.isEmpty() ? null : name
    version = version?.isEmpty() ? null : version

    String cleanDataFormat = name
    if (version) {
      cleanDataFormat += " > " + version
    }
    return cleanDataFormat
  }

  static String normalizeLinkProtocol(Link link) {
    String protocol = link?.linkProtocol
    if(protocol) {
      String cleanProtocol = cleanInternalKeywordWhitespace(protocol).trim().toUpperCase()
      return cleanProtocol
    }
    return null
  }

  static String normalizeHierarchyKeyword(String text) {
    def cleanText = cleanInternalKeywordWhitespace(text)
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace('Earth Science > ', '').replace('Earth Science Services > ', '')
  }

  static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    def cleanText = cleanInternalKeywordWhitespace(text)
    def elements = Arrays.asList(cleanText.split(' > '))
    String longName = elements.last()
    if (longName == longName.toUpperCase() || longName == longName.toLowerCase()) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters)
      elements.set(elements.size() - 1, longName)
    }
    return String.join(' > ', elements)
  }

  static final char[] capitalizingDelimiters = [' ', '/', '.', '(', '-', '_'].collect({ it as char })

  static String cleanInternalKeywordWhitespace(String text) {
    String cleanString = text.replaceAll("\\s+", " ")
    return cleanString
  }

  static List<String> tokenizeHierarchyKeyword(String text) {
    def result = []
    def i = text.length()
    while (i > 0) {
      text = text.substring(0, i).trim()
      result.add(text)
      i = text.lastIndexOf('>', i)
    }
    return result
  }
}
