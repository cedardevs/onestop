@Grapes(
    @Grab(group='org.apache.commons', module='commons-text', version='1.2')
)

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.WordUtils
import groovy.xml.XmlUtil

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery


def json = new JsonSlurper().parse(System.in.newReader())
print(MetadataParser.parseXMLMetadata(json.rawMetadata))

class MetadataParser {

  static Map parseIdentifierInfo(String xml) {
    def slurped = new XmlSlurper().parseText(xml)

    def identifiers = slurped.identificationInfo.MD_DataIdentification.citation.CI_Citation.'**'.findAll {
      it.name() == 'identifier'
    }
    String doi = identifiers.findResult(null, { identifier ->
      if (identifier.MD_Identifier.authority.CI_Citation.title.CharacterString.text() == 'Digital Object Identifier (DOI)') {
        return identifier.MD_Identifier.code.Anchor.text()
      }
    })

    return [
        fileId  : slurped.fileIdentifier.CharacterString.text(),
        doi     : doi,
        parentId: slurped.parentIdentifier.Anchor.text() ?: slurped.parentIdentifier.CharacterString.text() ?: null
    ]
  }

  static String parseXMLMetadata(String xml) {
    return JsonOutput.toJson(parseXMLMetadataToMap(xml))
  }

  static Map parseXMLMetadataToMap(String xml) {

    def metadata = new XmlSlurper().parseText(xml)

    // Parse related data maps from the xml:
    def citationInfo = parseCitationInfo(metadata)
    def keywordsMap = parseKeywordsAndTopics(metadata)
    def acquisitionInfo = parseAcquisitionInfo(metadata)
    def dsmmMap = parseDSMM(metadata)
    def spatialMap = parseSpatialInfo(metadata)
    def responsibleParties = parseDataResponsibleParties(metadata)
    def services = parseServices(metadata)
    def miscellaneous = parseMiscellaneous(metadata)

    // Build JSON:
    def json = [
        fileIdentifier                  : citationInfo.fileIdentifier,
        parentIdentifier                : citationInfo.parentIdentifier,
        hierarchyLevelName              : citationInfo.hierarchyLevelName,
        doi                             : citationInfo.doi,
        purpose                         : citationInfo.purpose,
        status                          : citationInfo.status,
        credit                          : citationInfo.credit,
        title                           : citationInfo.title,
        alternateTitle                  : citationInfo.alternateTitle,
        description                     : citationInfo.description,
        keywords                        : keywordsMap.keywords,
        accessionValues                 : keywordsMap.accessionValues,
        topicCategories                 : keywordsMap.topicCategories,
        gcmdScienceServices             : keywordsMap.gcmdScienceServices,
        gcmdScience                     : keywordsMap.gcmdScience,
        gcmdLocations                   : keywordsMap.gcmdLocations,
        gcmdInstruments                 : keywordsMap.gcmdInstruments,
        gcmdPlatforms                   : keywordsMap.gcmdPlatforms,
        gcmdProjects                    : keywordsMap.gcmdProjects,
        gcmdDataCenters                 : keywordsMap.gcmdDataCenters,
        gcmdHorizontalResolution        : keywordsMap.gcmdHorizontalResolution,
        gcmdVerticalResolution          : keywordsMap.gcmdVerticalResolution,
        gcmdTemporalResolution          : keywordsMap.gcmdTemporalResolution,
        temporalBounding                : parseTemporalBounding(metadata),
        spatialBounding                 : spatialMap.spatialBounding,
        isGlobal                        : spatialMap.isGlobal,
        acquisitionInstruments          : acquisitionInfo.acquisitionInstruments,
        acquisitionOperations           : acquisitionInfo.acquisitionOperations,
        acquisitionPlatforms            : acquisitionInfo.acquisitionPlatforms,
        dataFormats                     : parseDataFormats(metadata),
        links                           : parseLinks(metadata),
        contacts                        : responsibleParties.contacts,
        creators                        : responsibleParties.creators,
        publishers                      : responsibleParties.publishers,
        thumbnail                       : citationInfo.thumbnail,
        thumbnailDescription            : citationInfo.thumbnailDescription,
        creationDate                    : citationInfo.creationDate,
        revisionDate                    : citationInfo.revisionDate,
        publicationDate                 : citationInfo.publicationDate,
        citeAsStatements                : citationInfo.citeAsStatements,
        crossReferences                 : citationInfo.crossReferences,
        largerWorks                     : citationInfo.largerWorks,
        useLimitation                   : citationInfo.useLimitation,
        legalConstraints                : citationInfo.legalConstraints,
        accessFeeStatement              : citationInfo.accessFeeStatement,
        orderingInstructions            : citationInfo.orderingInstructions,
        edition                         : citationInfo.edition,
        dsmmAccessibility               : dsmmMap.Accessibility,
        dsmmDataIntegrity               : dsmmMap.DataIntegrity,
        dsmmDataQualityAssessment       : dsmmMap.DataQualityAssessment,
        dsmmDataQualityAssurance        : dsmmMap.DataQualityAssurance,
        dsmmDataQualityControlMonitoring: dsmmMap.DataQualityControlMonitoring,
        dsmmPreservability              : dsmmMap.Preservability,
        dsmmProductionSustainability    : dsmmMap.ProductionSustainability,
        dsmmTransparencyTraceability    : dsmmMap.TransparencyTraceability,
        dsmmUsability                   : dsmmMap.Usability,
        dsmmAverage                     : dsmmMap.average,
        updateFrequency                 : miscellaneous.updateFrequency,
        presentationForm                : miscellaneous.presentationForm,
        services                        : services,
        stagedDate                      : System.currentTimeMillis()
    ]

    return json
  }

  static Map parseCitationInfo(GPathResult metadata) {
    def fileIdentifier
    def parentIdentifier
    def hierarchyLevelName
    def doi
    def purpose
    def status
    def credit
    def title
    def alternateTitle
    def description
    def thumbnail
    def thumbnailDescription
    def creationDate
    def revisionDate
    def publicationDate
    Set citeAsStatements = []
    Set crossReferences = []
    Set largerWorks = []
    def useLimitation
    def legalConstraints
    def accessFeeStatement
    def orderingInstructions
    def edition

    def idInfo = metadata.identificationInfo.MD_DataIdentification

    fileIdentifier = metadata.fileIdentifier.CharacterString.text()
    parentIdentifier = metadata.parentIdentifier.Anchor.text() ?: metadata.parentIdentifier.CharacterString.text() ?: null
    hierarchyLevelName = metadata.hierarchyLevelName.CharacterString.text().toLowerCase() ?: null

    purpose = idInfo.purpose.text() ?: null
    status = idInfo.status.MD_ProgressCode.@codeListValue.text() ?: null
    credit = idInfo.credit.text() ?: null

    def identifiers = idInfo.citation.CI_Citation.'**'.findAll { it.name() == 'identifier' }
    doi = identifiers.findResult(null, { identifier ->
      def anchor = identifier.MD_Identifier.code.Anchor
      def titleTag = anchor.'@xlink:title'.text()
      if (titleTag == 'DOI') {
        return anchor.text()
      }
    })
    title = idInfo.citation.CI_Citation.title.CharacterString.text()
    alternateTitle = idInfo.citation.CI_Citation.alternateTitle.CharacterString.text() ?: null
    description = idInfo.abstract.CharacterString.text()
    def thumbnailPath = idInfo.graphicOverview.MD_BrowseGraphic
    thumbnail = StringEscapeUtils.unescapeXml(thumbnailPath.fileName.CharacterString.text())
    thumbnailDescription = thumbnailPath.fileDescription.CharacterString.text() ?: null

    // Miscellaneous dates:
    def dates = idInfo.citation.CI_Citation.'**'.findAll { it.name() == 'date' }
    dates.each { date ->
      def dateType = date.CI_Date.dateType.CI_DateTypeCode.@codeListValue.text()
      if (dateType == 'publication') {
        publicationDate = date.CI_Date.date.Date.text() ?: null
      } else if (dateType == 'creation') {
        creationDate = date.CI_Date.date.Date.text() ?: null
      } else if (dateType == 'revision') {
        revisionDate = date.CI_Date.date.Date.text() ?: null
      }
    }

    // Cite-As Statements
    def otherConstraints = idInfo.resourceConstraints.MD_LegalConstraints.'**'.findAll { it.name() == 'otherConstraints' }
    def citationConstraints = otherConstraints.findAll { it.CharacterString.text().toLowerCase().contains('cite') }
    citeAsStatements = citationConstraints.collect { it.CharacterString.text() }.toSet()

    // Cross References & Larger Works
    def aggregationInfo = metadata.'**'.findAll { it.name() == 'aggregationInfo' }

    aggregationInfo.each { aggInfo ->
      def associationType = aggInfo.MD_AggregateInformation.associationType.DS_AssociationTypeCode.@codeListValue.text() ?: null
      def initiativeType = aggInfo.MD_AggregateInformation.initiativeType.DS_InitiativeTypeCode.@codeListValue.text() ?: null
      def citation = aggInfo.MD_AggregateInformation.aggregateDataSetName.CI_Citation
      def onlineResource = aggInfo.CI_OnlineResource

      def aggPubDate = null
      def aggDates = citation.'**'.findAll { it.name() == 'date' }
      aggDates.each { date ->
        def dateType = date.CI_Date.dateType.CI_DateTypeCode.@codeListValue.text()
        if(dateType == 'publication') {
          aggPubDate = date.CI_Date.date.Date.text() ?: null
        }
      }

      def aggTitle = citation.title.text() ?: null
      def code = citation.identifier.MD_Identifier.code.CharacterString.text() ?: null
      def link = [
          linkName       : onlineResource.name.CharacterString.text() ?: null,
          linkProtocol   : onlineResource.protocol.CharacterString.text() ?: null,
          linkUrl        : onlineResource.linkage.URL.text() ? StringEscapeUtils.unescapeXml(onlineResource.linkage.URL.text()) : null,
          linkDescription: onlineResource.description.CharacterString.text() ?: null,
          linkFunction   : onlineResource.function.CI_OnLineFunctionCode.@codeListValue.text() ?: null
      ]

      if(associationType == 'crossReference') {
        crossReferences.add([
            title: aggTitle,
            code: code,
            publicationDate: aggPubDate,
            link: link
        ])
      }
      else if(associationType == 'largerWorkCitation') {
        largerWorks.add([
            title: title,
            code: code,
            publicationDate: aggPubDate,
            link: link
        ])
      }
    }

    // Use Limitation, Legal Constraints, Access Fee Statements, Ordering Instructions, and Edition
    useLimitation = idInfo.resourceConstraints.MD_Constraints.useLimitation.CharacterString.text() ?: null
    legalConstraints = otherConstraints.collect { return it.CharacterString.text() ?: null } as Set
    accessFeeStatement = metadata.distributionInfo.MD_Distribution.distributionOrderProcess.MD_StandardOrderProcess.fees.CharacterString.text() ?: null
    orderingInstructions = metadata.distributionInfo.MD_Distribution.distributionOrderProcess.MD_StandardOrderProcess.orderingInstructions.CharacterString.text() ?: null
    edition = idInfo.citation.CI_Citation.edition.CharacterString.text() ?: null

    return [
        fileIdentifier      : fileIdentifier,
        parentIdentifier    : parentIdentifier,
        hierarchyLevelName  : hierarchyLevelName,
        doi                 : doi,
        purpose             : purpose,
        status              : status,
        credit              : credit,
        title               : title,
        alternateTitle      : alternateTitle,
        description         : description,
        thumbnail           : thumbnail,
        thumbnailDescription: thumbnailDescription,
        creationDate        : creationDate,
        revisionDate        : revisionDate,
        publicationDate     : publicationDate,
        citeAsStatements    : citeAsStatements,
        crossReferences     : crossReferences,
        largerWorks         : largerWorks,
        useLimitation       : useLimitation,
        legalConstraints    : legalConstraints,
        accessFeeStatement  : accessFeeStatement,
        orderingInstructions: orderingInstructions,
        edition             : edition
    ]
  }

  static Map parseCitationInfo(String xml) {
    return parseCitationInfo(new XmlSlurper().parseText(xml))
  }

  static Map parseKeywordsAndTopics(GPathResult metadata) {

    def extractKnownText = { k ->
      def text = k.CharacterString.text() ?: k.Anchor.text()
      return text.trim()
    }

    def idInfo = metadata.identificationInfo.MD_DataIdentification

    def keywords = [] as Set
    def accessionValues = [] as Set
    def topicCategories = [] as Set
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

    topicCategories.addAll(idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text())

    def keywordGroups = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    keywordGroups.each { group ->
      def namespace = group.thesaurusName.CI_Citation.title.CharacterString.text()
      def type = group.type.MD_KeywordTypeCode.@codeListValue.text() ?: null
      def keywordsInGroup = group.'**'.findAll { it.name() == 'keyword' }
      def values = [] as Set

      if(namespace.toUpperCase() == 'NCEI ACCESSION NUMBER') {
        // Accession values are NOT keywords
        keywordsInGroup.each { k ->
          accessionValues.add(extractKnownText(k))
        }
      }

      else {
        if(namespace.toLowerCase().contains('gcmd') || namespace.toLowerCase().contains('global change master directory')) {
          switch (namespace.toLowerCase()) {
            case { it.contains('science') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                if(text.toLowerCase().startsWith('earth science services')) {
                  text = normalizeHierarchyKeyword(text)
                  gcmdScienceServices.addAll(tokenizeHierarchyKeyword(text))
                }
                else if(text.toLowerCase().startsWith('earth science')) {
                  text = normalizeHierarchyKeyword(text)
                  gcmdScience.addAll(tokenizeHierarchyKeyword(text))
                }
                values.add(text)
              }
              break
            case { it.contains('location') || it.contains('place') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = normalizeHierarchyKeyword(text)
                gcmdLocations.addAll(tokenizeHierarchyKeyword(text))
                values.add(text)
              }
              break
            case { it.contains('platform') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = normalizeNonHierarchicalKeyword(text)
                gcmdPlatforms.add(text)
                values.add(text)
              }
              break
            case { it.contains('instrument') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = normalizeNonHierarchicalKeyword(text)
                gcmdInstruments.add(text)
                values.add(text)
              }
              break
            case { it.contains('data center') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = normalizeNonHierarchicalKeyword(text)
                gcmdDataCenters.add(text)
                values.add(text)
              }
              break
            case { it.contains('horizontal data resolution') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = WordUtils.capitalizeFully(text, capitalizingDelimiters)
                gcmdHorizontalResolution.add(text)
                values.add(text)
              }
              break
            case { it.contains('vertical data resolution') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = WordUtils.capitalizeFully(text, capitalizingDelimiters)
                gcmdVerticalResolution.add(text)
                values.add(text)
              }
              break
            case { it.contains('temporal data resolution') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = WordUtils.capitalizeFully(text, capitalizingDelimiters)
                gcmdTemporalResolution.add(text)
                values.add(text)
              }
              break
            case { it.contains('project') }:
              keywordsInGroup.each { k ->
                def text = extractKnownText(k)
                text = normalizeNonHierarchicalKeyword(text)
                gcmdProjects.add(text)
                values.add(text)
              }
              break
            default:
              // Namespace didn't meet our checks, save as regular keywords only
              keywordsInGroup.each { k ->
                values.add(extractKnownText(k))
              }
          }
        }

        else {
          // Not a known namespace
          keywordsInGroup.each { k ->
            def text = k.CharacterString.text() ?: k.Anchor.text()
            if(text) { // Just in case, since we don't know this namespace...
              values.add(text.trim())
            }
          }
        }

        // Add whole group of keywords
        keywords.add([
            values: values,
            type: type,
            namespace: namespace
        ])
      }
    }

    return [
        keywords                : keywords,
        accessionValues         : accessionValues,
        topicCategories         : topicCategories,
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

  static String cleanInternalGCMDKeywordWhitespace(String text) {
    def elements = text.split('>')
    def trimmed = elements.collect { e ->  e.trim() }
    return String.join(' > ', trimmed)
  }

  static final char[] capitalizingDelimiters = [' ', '/', '.', '(', '-', '_'].collect({ it as char })

  static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    def elements = Arrays.asList(cleanText.split(' > '))
    String longName = elements.last()
    if(longName == longName.toUpperCase() || longName == longName.toLowerCase()) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters)
      elements.set(elements.size() - 1, longName)
    }
    return String.join(' > ', elements)
  }

  static String normalizeHierarchyKeyword(String text) {
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace('Earth Science > ', '').replace('Earth Science Services > ', '')
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

  static Map parseKeywordsAndTopics(String xml) {
    return parseKeywordsAndTopics(new XmlSlurper().parseText(xml))
  }

  // Year must be in the range [-292275055,292278994] in order to be parsed as date by ES (Joda time magic number)
  static final MIN_DATE_LONG = -292275055L
  static final MAX_DATE_LONG = 292278994L

  // handle 3 optional date formats in priority of full-parse option to minimal-parse options
  static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)

  // use custom formatter for when time zone information is not supplied in a LocalDateTime format for ES's happiness
  static final DateTimeFormatter ELASTIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  static Map elasticDateInfo(String date) {

    // don't bother parsing if there's nothing here
    if(!date) {
      return null
    }

    // default to null
    String elasticDate = null
    TemporalAccessor parsedDate = null
    Long year

    // paleo dates can be longs
    if(date.isLong()) {
      elasticDate = date
      year = Long.parseLong(date)
      // we only care about the year if outside of limits
      if(year < MIN_DATE_LONG || year > MAX_DATE_LONG) {
        elasticDate = null
      }
    }
    else {
      // the "::" operator in Java8 is ".&" in groovy until groovy fully adopts "::"
      parsedDate = PARSE_DATE_FORMATTER.parseBest(date, ZonedDateTime.&from as TemporalQuery, LocalDateTime.&from as TemporalQuery, LocalDate.&from as TemporalQuery)
      year = parsedDate.get(ChronoField.YEAR)

      // date is in format like: 2010-12-30T00:00:00Z
      if(parsedDate instanceof ZonedDateTime) {
        elasticDate = date
      }
      // date is in format like: 2010-12-30T00:00:00
      else if(parsedDate instanceof LocalDateTime) {
        // assume UTC
        ZonedDateTime parsedDateUTC = parsedDate.atZone(ZoneId.of("UTC"))
        elasticDate = parsedDateUTC.format(ELASTIC_DATE_FORMATTER)
        // re-evaluate year in off-chance year was affected by zone id
        year = parsedDateUTC.get(ChronoField.YEAR)
      }
      // date is in format like: 2010-12-30
      else if(parsedDate instanceof LocalDate) {
        elasticDate = date
      }
    }

    def dateInfo = [ "date": elasticDate, "year":  year]
    return dateInfo
  }

  static Map parseTemporalBounding(GPathResult metadata) {

    def boundingExtent = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent

    def description = boundingExtent[0].description.CharacterString.text() ?: null
    def time = boundingExtent.temporalElement?.'**'?.find { it -> it.name() == 'EX_TemporalExtent'}?.extent

    String beginText, beginIndeterminateText, endText, endIndeterminateText, instantText, instantIndeterminateText
    def begin, end, instant
    if(time) {
      // parse potential date fields out of XML
      beginText = time.TimePeriod.beginPosition.text() ?:
          time.TimePeriod.begin.TimeInstant.timePosition.text() ?: null
      beginIndeterminateText = time.TimePeriod.beginPosition.@indeterminatePosition.text() ?:
          time.TimePeriod.begin.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
      endText = time.TimePeriod.endPosition.text() ?:
          time.TimePeriod.end.TimeInstant.timePosition.text() ?: null
      endIndeterminateText = time.TimePeriod.endPosition.@indeterminatePosition.text() ?:
          time.TimePeriod.end.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
      instantText = time.TimeInstant.timePosition.text() ?: null
      instantIndeterminateText = time.TimeInstant.timePosition.@indeterminatePosition.text() ?: null

      // massage the date fields for elastic search (handles multiple formats and invalid dates)
      begin = elasticDateInfo(beginText)
      end = elasticDateInfo(endText)
      instant = elasticDateInfo(instantText)
    }

    return [
        beginDate           : begin?.date,
        beginIndeterminate  : beginIndeterminateText,
        beginYear           : begin?.year,
        endDate             : end?.date,
        endIndeterminate    : endIndeterminateText,
        endYear             : end?.year,
        instant             : instant?.date,
        instantIndeterminate: instantIndeterminateText,
        description         : description
    ]
  }

  static Map parseTemporalBounding(String xml) {
    return parseTemporalBounding(new XmlSlurper().parseText(xml))
  }

  static Map parseSpatialInfo(GPathResult metadata) {
    def space = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement
    def bbox = space?.'**'?.find { it -> it.name() == 'EX_GeographicBoundingBox' }

    def spatialBounding = parseBounding(bbox)
    def isGlobal = checkIsGlobal(spatialBounding)

    return ["spatialBounding": spatialBounding, "isGlobal": isGlobal]
  }

  static Map parseSpatialInfo(String xml) {
    return parseSpatialInfo(new XmlSlurper().parseText(xml))
  }

  static def parseBounding(def bbox) {
    if (!bbox) { return null }

    def west = (bbox.westBoundLongitude == "null" ||  bbox.westBoundLongitude == "") ? null : bbox.westBoundLongitude.Decimal.toFloat()
    def east = (bbox.eastBoundLongitude == "null" ||  bbox.eastBoundLongitude == "") ? null : bbox.eastBoundLongitude.Decimal.toFloat()
    def north = (bbox.northBoundLatitude == "null" || bbox.northBoundLatitude == "")  ? null : bbox.northBoundLatitude.Decimal.toFloat()
    def south = (bbox.southBoundLatitude == "null" || bbox.southBoundLatitude == "") ? null : bbox.southBoundLatitude.Decimal.toFloat()

    if (!west || !east || !north || !south) { return null }

    def type = (west == east && north == south) ? 'Point' : 'Polygon'
    def coordinates = type == 'Point' ? [west, north] : [[[west, south], [east, south], [east, north], [west, north], [west, south]]]

    if(type == 'Polygon') {
      // Check for valid polygon coordinates (no vertices should be the same)
      def clone = coordinates[0].collect {it}
      clone.removeAt(0)
      def unique = clone.unique(false)
      if (clone != unique) {
        throw new Exception("Invalid spatial bounding box provided.")
      }
    }


    return [type: type, coordinates: coordinates]
  }

  static def checkIsGlobal(def bounds) {
    if (bounds?.type != 'Polygon') { return false }

    def coords = bounds.coordinates[0]
    def west = coords[0][0]
    def east = coords[1][0]
    def north = coords[2][1]
    def south = coords[0][1]

    return west == -180 && east == 180 && north == 90 && south == -90
  }

  static Map parseAcquisitionInfo(GPathResult metadata) {

    def acquisitionInstruments = [] as Set
    def acquisitionOperations = [] as Set
    def acquisitionPlatforms = [] as Set

    // Acquisition instrument:
    def instruments = metadata.acquisitionInformation.MI_AcquisitionInformation
        .'**'.findAll { it.name() == 'MI_Instrument' }
    instruments.each { e ->
      acquisitionInstruments.add([
          instrumentIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null,
          instrumentType       : e.type.CharacterString.text() ?: e.type.Anchor.text() ?: null,
          instrumentDescription: e.description.CharacterString.text() ?: null
      ])
    }

    // Acquisition operation:
    def operations = metadata.acquisitionInformation.MI_AcquisitionInformation
        .'**'.findAll { it.name() == 'MI_Operation' }
    operations.each { e ->
      acquisitionOperations.add([
          operationDescription: e.description.CharacterString.text() ?: null,
          operationIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null,
          operationStatus     : e.status.MD_ProgressCode.@codeListValue.text() ?: null,
          operationType       : e.type.MI_OperationTypeCode.@codeListValue.text() ?: null // FIXME not sure on path
      ])
    }

    // Acquisition platform:
    def platforms = metadata.acquisitionInformation.MI_AcquisitionInformation
        .'**'.findAll { it.name() == 'MI_Platform' }
    platforms.each { e ->
      acquisitionPlatforms.add([
          platformIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null,
          platformDescription: e.description.CharacterString.text() ?: null,
          platformSponsor    : e.sponsor.CI_ResponsibleParty.organisationName
              .'**'.findAll { it.name() == 'CharacterString' }*.text()
      ])
    }

    return [
        acquisitionInstruments: acquisitionInstruments,
        acquisitionOperations : acquisitionOperations,
        acquisitionPlatforms  : acquisitionPlatforms
    ]
  }

  static Map parseAcquisitionInfo(String xml) {
    return parseAcquisitionInfo(new XmlSlurper().parseText(xml))
  }

  static Set parseDataFormats(GPathResult metadata) {
    def dataFormats = [] as Set
    def formats = metadata.distributionInfo.MD_Distribution.'**'.findAll { it.name() == 'MD_Format' }
    formats.each { format ->

      def name = format.name.CharacterString.text() ? (format.name.CharacterString.text() as String).toUpperCase() : null
      def version = format.version.CharacterString.text() ?: null

      dataFormats.add([
          name: name,
          version: version
      ])
    }
    return dataFormats
  }

  static Set parseDataFormats(String xml) {
    return parseDataFormats(new XmlSlurper().parseText(xml))
  }

  static Set parseLinks(GPathResult metadata) {

    def links = [] as Set
    def linkage = metadata.distributionInfo.MD_Distribution.'**'.findAll { it.name() == 'CI_OnlineResource' }
    linkage.each { e ->
      links.add([
          linkName       : e.name.CharacterString.text() ?: null,
          linkProtocol   : e.protocol.CharacterString.text() ?: null,
          linkUrl        : e.linkage.URL.text() ? StringEscapeUtils.unescapeXml(e.linkage.URL.text()) : null,
          linkDescription: e.description.CharacterString.text() ?: null,
          linkFunction   : e.function.CI_OnLineFunctionCode.@codeListValue.text() ?: null
      ])
    }
    return links
  }

  static Set parseLinks(String xml) {
    return parseLinks(new XmlSlurper().parseText(xml))
  }

  static List<GPathResult> parseResponsibleParties(GPathResult metadata) {
    return metadata.'**'.findAll {
      it.name() == 'CI_ResponsibleParty'
    }
  }

  static Map<String, String> parseParty(GPathResult party) {
    String individualName = party.individualName.CharacterString.text() ?: party.individualName.Anchor.text() ?: null
    String organizationName = party.organisationName.CharacterString.text() ?: party.organisationName.Anchor.text() ?: null
    String positionName = party.positionName.CharacterString.text() ?: party.positionName.Anchor.text() ?: null
    String role = party.role.CI_RoleCode.@codeListValue.text() ?: null
    String email = party.contactInfo.CI_Contact.address.CI_Address.electronicMailAddress.CharacterString.text() ?: null
    String phone = party.contactInfo.CI_Contact.phone.CI_Telephone.voice.CharacterString.text() ?: null
    return [
        individualName  : individualName,
        organizationName: organizationName,
        positionName    : positionName,
        role            : role,
        email           : email,
        phone           : phone
    ]
  }

  static Map<String, Set> parseDataResponsibleParties(GPathResult metadata) {

    Set contacts = []
    Set contactRoles = ['pointOfContact', 'distributor']
    Set creators = []
    Set creatorRoles = ['resourceProvider', 'originator', 'principalInvestigator', 'author', 'collaborator']
    Set publishers = []
    Set publisherRoles = ['publisher']

    GPathResult dataPath = metadata.identificationInfo.MD_DataIdentification
    List<GPathResult> parties = parseResponsibleParties(dataPath)
    parties.each { party ->
      Map<String, String> parsedParty = parseParty(party)
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

  static Map<String, Set> parseDataResponsibleParties(String xml) {
    return parseDataResponsibleParties(new XmlSlurper().parseText(xml))
  }

  static Map parseDSMM(GPathResult metadata) {
    def dsmmMap = [
        'Accessibility'               : 0,
        'DataIntegrity'               : 0,
        'DataQualityAssessment'       : 0,
        'DataQualityAssurance'        : 0,
        'DataQualityControlMonitoring': 0,
        'Preservability'              : 0,
        'ProductionSustainability'    : 0,
        'TransparencyTraceability'    : 0,
        'Usability'                   : 0
    ]
    def scoreMap = [
        'notAvailable': 0,
        'adHoc'       : 1,
        'minimal'     : 2,
        'intermediate': 3,
        'advanced'    : 4,
        'optimal'     : 5
    ]

    def dsmmValues = []
    def dsmm = metadata.dataQualityInfo.DQ_DataQuality.report.DQ_ConceptualConsistency.'**'.find {
      e -> e.nameOfMeasure.CharacterString.text() == 'Data Stewardship Maturity Assessment'
    }
    if(dsmm) {
      dsmmValues = dsmm.result.DQ_QuantitativeResult.'**'.findAll { it.name() == 'Record' }
    }

    dsmmValues.each { r ->
      def measureUrl = r.CodeListValue.@codeList.text() ?: r.CodeListValue.@codelist.text() // FIXME Handling a typo until it's resolved
      def measure = measureUrl.substring(measureUrl.lastIndexOf('#') + 1)
      def score = scoreMap.get(r.CodeListValue.@codeListValue.text())
      dsmmMap.replace(measure, score)
    }

    def avg = dsmmMap.values().sum() / dsmmMap.size()
    dsmmMap.put('average', avg)

    return dsmmMap
  }

  static Map parseDSMM(String xml) {
    return parseDSMM(new XmlSlurper().parseText(xml))
  }

  static Map parseMiscellaneous(GPathResult metadata) {
    def dataId = metadata.identificationInfo.MD_DataIdentification ?: null
    def updateFrequency = dataId.resourceMaintenance.MD_MaintenanceInformation.maintenanceAndUpdateFrequency.MD_MaintenanceFrequencyCode.@codeListValue.text() ?: null
    def presentationForm = dataId.citation.CI_Citation.presentationForm.CI_PresentationFormCode.@codeListValue.text() ?: null
    return [
        updateFrequency: updateFrequency,
        presentationForm: presentationForm
    ]
  }

  static Map parseMiscellaneous(String xml) {
    return parseMiscellaneous(new XmlSlurper().parseText(xml))
  }

  static Set parseServices(GPathResult metadata) {
    def serviceIds = metadata.identificationInfo.'**'.findAll {
      it.name() == 'SV_ServiceIdentification'
    }
    Set services = []
    serviceIds.each { service ->
      def xmlBlobService = XmlUtil.serialize(service) ?: null
      // blob of XML needs to be base64 encoded for elastic search to include is as 'binary' type
      services.add(xmlBlobService.bytes.encodeBase64().toString())
    }
    return services
  }

  static Set parseServices(String xml) {
    return parseServices(new XmlSlurper().parseText(xml))
  }

  static Map mergeCollectionAndGranule(Map collection, Map granule) {
    return collection + granule
  }
}
