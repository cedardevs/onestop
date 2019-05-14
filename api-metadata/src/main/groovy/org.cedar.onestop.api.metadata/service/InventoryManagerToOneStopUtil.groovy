package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.WordUtils
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.parse.ISOParser
import org.xml.sax.SAXException

import java.time.temporal.ChronoUnit

import static org.cedar.schemas.avro.psi.ValidDescriptor.*

@Slf4j
class InventoryManagerToOneStopUtil {

  static Map validateMessage(String id, ParsedRecord messageMap) {
    def discovery = messageMap?.discovery
    def analysis = messageMap?.analysis
    def titles = analysis?.titles
    def identification = analysis?.identification
    def temporal = analysis?.temporalBounding

    def failure = [title: 'Invalid record']
    List<String> details = []

    // Validate record
    if (discovery == null) {
      details << "Missing discovery metadata"
    }
    if (analysis == null) {
      details << "Missing analysis metadata"
    }
    if (titles == null) {
      details << "Missing title analysis"
    }
    if (identification == null) {
      details << "Missing identification analysis"
    }
    if (temporal == null) {
      details << "Missing temporal analysis"
    }
    if (identification && (!identification?.fileIdentifierExists && !identification?.doiExists)) {
      details << "Missing identifier - record contains neither a fileIdentifier nor a DOI"
    }
    if (titles && !titles.titleExists) {
      details << "Missing title"
    }
    if (discovery && identification && discovery.hierarchyLevelName == 'granule' && !identification.parentIdentifierExists) {
      details << "Mismatch between metadata type and identifiers detected"
    }
    if (temporal && temporal.beginDescriptor == INVALID) {
      details << "Invalid beginDate"
    }
    if (temporal && temporal.endDescriptor == INVALID) {
      details << "Invalid endDate"
    }
    if (temporal && temporal.beginDescriptor != UNDEFINED && temporal.endDescriptor != UNDEFINED && temporal.instantDescriptor == INVALID) {
      details << "Invalid instant-only date"
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

  static Map xmlToParsedRecord(String xmlDoc){
    Map result = [:]
    def parsedRecordBuilder = ParsedRecord.newBuilder()
    try {
      log.debug('Parsing XML for discovery metadata')
      Discovery discovery = ISOParser.parseXMLMetadataToDiscovery(xmlDoc)
      log.debug('Analyzing discovery metadata')
      Analysis analysis = Analyzers.analyze(discovery)
      Map source = AvroUtils.avroToMap(discovery, true)
      RecordType type = source.parentIdentifier ? RecordType.granule : RecordType.collection
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

    // create gcmdkeywords
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
    discoveryMap.services = [] // FIXME this needs to be in place until we can use ES6 ignore_missing flags
    return discoveryMap
  }

  //Create GCMD keyword lists
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
      def it = group.namespace.toLowerCase() ?: ""
      def keywordsInGroup = group.values ?: null
      if (it.contains('gcmd') || it.contains('global change master directory')) {
        switch (it) {
          case { it.contains('earth science services') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdScienceServices.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('earth science') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdScience.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('location') || it.contains('place') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdLocations.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('platform') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdPlatforms.add(text)
            }
            break
          case { it.contains('instrument') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdInstruments.add(text)
            }
            break
          case { it.contains('data center') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdDataCenters.add(text)
            }
            break
          case { it.contains('horizontal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdHorizontalResolution.add(text)
            }
            break
          case { it.contains('vertical data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdVerticalResolution.add(text)
            }
            break
          case { it.contains('temporal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdTemporalResolution.add(text)
            }
            break
          case { it.contains('project') }:
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
  static String normalizeHierarchyKeyword(String text) {
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace('Earth Science > ', '').replace('Earth Science Services > ', '')
  }

  static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    def elements = Arrays.asList(cleanText.split(' > '))
    String longName = elements.last()
    if (longName == longName.toUpperCase() || longName == longName.toLowerCase()) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters)
      elements.set(elements.size() - 1, longName)
    }
    return String.join(' > ', elements)
  }

  static final char[] capitalizingDelimiters = [' ', '/', '.', '(', '-', '_'].collect({ it as char })

  static String cleanInternalGCMDKeywordWhitespace(String text) {
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
