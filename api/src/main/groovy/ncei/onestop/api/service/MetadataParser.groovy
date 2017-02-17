package ncei.onestop.api.service

import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang3.text.WordUtils

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
        id      : doi ? doi.replace('/', '-') : slurped.fileIdentifier.CharacterString.text(),
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

    // Build JSON:
    def json = [
        fileIdentifier                  : citationInfo.fileIdentifier,
        parentIdentifier                : citationInfo.parentIdentifier,
        doi                             : citationInfo.doi,
        title                           : citationInfo.title,
        alternateTitle                  : citationInfo.alternateTitle,
        description                     : citationInfo.description,
        keywords                        : keywordsMap.keywords,
        topicCategories                 : keywordsMap.topicCategories,
        gcmdScience                     : keywordsMap.gcmdScience,
        gcmdLocations                   : keywordsMap.gcmdLocations,
        gcmdInstruments                 : keywordsMap.gcmdInstruments,
        gcmdPlatforms                   : keywordsMap.gcmdPlatforms,
        gcmdProjects                    : keywordsMap.gcmdProjects,
        gcmdDataCenters                 : keywordsMap.gcmdDataCenters,
        gcmdDataResolution              : keywordsMap.gcmdDataResolution,
        temporalBounding                : parseTemporalBounding(metadata),
        spatialBounding                 : parseSpatialBounding(metadata),
        acquisitionInstruments          : acquisitionInfo.acquisitionInstruments,
        acquisitionOperations           : acquisitionInfo.acquisitionOperations,
        acquisitionPlatforms            : acquisitionInfo.acquisitionPlatforms,
        dataFormats                     : parseDataFormats(metadata),
        links                           : parseLinks(metadata),
        contacts                        : parseContacts(metadata),
        thumbnail                       : citationInfo.thumbnail,
        modifiedDate                    : citationInfo.modifiedDate,
        creationDate                    : citationInfo.creationDate,
        revisionDate                    : citationInfo.revisionDate,
        publicationDate                 : citationInfo.publicationDate,
        dsmmAccessibility               : dsmmMap.Accessibility,
        dsmmDataIntegrity               : dsmmMap.DataIntegrity,
        dsmmDataQualityAssessment       : dsmmMap.DataQualityAssessment,
        dsmmDataQualityAssurance        : dsmmMap.DataQualityAssurance,
        dsmmDataQualityControlMonitoring: dsmmMap.DataQualityControlMonitoring,
        dsmmPreservability              : dsmmMap.Preservability,
        dsmmProductionSustainability    : dsmmMap.ProductionSustainability,
        dsmmTransparencyTraceability    : dsmmMap.TransparencyTraceability,
        dsmmUsability                   : dsmmMap.Usability,
        dsmmAverage                     : dsmmMap.average
    ]

    return json
  }

  static Map parseCitationInfo(GPathResult metadata) {
    def fileIdentifier
    def parentIdentifier
    def doi
    def title
    def alternateTitle
    def description
    def thumbnail
    def modifiedDate
    def creationDate
    def revisionDate
    def publicationDate

    def idInfo = metadata.identificationInfo.MD_DataIdentification

    fileIdentifier = metadata.fileIdentifier.CharacterString.text()
    parentIdentifier = metadata.parentIdentifier.Anchor.text() ?: metadata.parentIdentifier.CharacterString.text() ?: null
    def identifiers = idInfo.citation.CI_Citation.'**'.findAll { it.name() == 'identifier' }
    doi = identifiers.findResult(null, { identifier ->
      if (identifier.MD_Identifier.authority.CI_Citation.title.CharacterString.text() == 'Digital Object Identifier (DOI)') {
        return identifier.MD_Identifier.code.Anchor.text()
      }
    })
    title = idInfo.citation.CI_Citation.title.CharacterString.text()
    alternateTitle = idInfo.citation.CI_Citation.alternateTitle.CharacterString.text() ?: null
    description = idInfo.abstract.CharacterString.text()
    thumbnail = idInfo.graphicOverview.MD_BrowseGraphic.fileName.CharacterString.text()

    // Miscellaneous dates:
    modifiedDate = metadata.dateStamp.Date.text() ?: metadata.dateStamp.DateTime.text()
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

    return [
        fileIdentifier  : fileIdentifier,
        parentIdentifier: parentIdentifier,
        doi             : doi,
        title           : title,
        alternateTitle  : alternateTitle,
        description     : description,
        thumbnail       : thumbnail,
        modifiedDate    : modifiedDate,
        creationDate    : creationDate,
        revisionDate    : revisionDate,
        publicationDate : publicationDate
    ]
  }

  static Map parseCitationInfo(String xml) {
    return parseCitationInfo(new XmlSlurper().parseText(xml))
  }

  static Map parseKeywordsAndTopics(GPathResult metadata) {

    def idInfo = metadata.identificationInfo.MD_DataIdentification

    def keywords = [] as Set
    def topicCategories = [] as Set
    def gcmdScience = [] as Set
    def gcmdLocations = [] as Set
    def gcmdPlatforms = [] as Set
    def gcmdInstruments = [] as Set
    def gcmdProjects = [] as Set
    def gcmdDataResolution = [] as Set
    def gcmdDataCenters = [] as Set

    topicCategories.addAll(idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text())

    def descriptiveKeywords = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    descriptiveKeywords.each { e ->
      def keywordGroup = e.'**'.findAll { it.name() == 'keyword' }
      keywordGroup.each { k ->

        def text = k.CharacterString.text() ?: k.Anchor.text()
        def namespace = e.thesaurusName.CI_Citation.title.CharacterString.text()

        if (text) {
          if (namespace.toLowerCase().contains('gcmd')) {
            switch (namespace) {
              case { it.toLowerCase().contains('science') }:
                text = WordUtils.capitalizeFully(text,
                    " " as char, "/" as char, "." as char, "(" as char, "-" as char, "_" as char)
                text = text.replace('Earth Science > ', '')
                gcmdScience.add(text)
                break
              case { it.toLowerCase().contains('location') || it.toLowerCase().contains('place') }:
                def locationKeywords = WordUtils.capitalizeFully(text,
                    " " as char, "/" as char, "." as char, "(" as char, "-" as char, "_" as char)
                gcmdLocations.add(locationKeywords)
                break
              case { it.toLowerCase().contains('platform') }:
                gcmdPlatforms.add(text)
                break
              case { it.toLowerCase().contains('instrument') }:
                gcmdInstruments.add(text)
                break
              case { it.toLowerCase().contains('data center') }:
                gcmdDataCenters.add(text)
                break
              case { it.toLowerCase().contains('data resolution') }:
                gcmdDataResolution.add(text)
                break
              case { it.toLowerCase().contains('project') }:
                gcmdProjects.add(text)
                break
              default:
                keywords.add(text)
                break
            }

          } else {
            keywords.add(text)
          }
        }
      }
    }

    return [
        keywords          : keywords,
        topicCategories   : topicCategories,
        gcmdScience       : gcmdScience,
        gcmdLocations     : gcmdLocations,
        gcmdInstruments   : gcmdInstruments,
        gcmdPlatforms     : gcmdPlatforms,
        gcmdProjects      : gcmdProjects,
        gcmdDataCenters   : gcmdDataCenters,
        gcmdDataResolution: gcmdDataResolution
    ]
  }

  static Map parseKeywordsAndTopics(String xml) {
    return parseKeywordsAndTopics(new XmlSlurper().parseText(xml))
  }

  static Map parseTemporalBounding(GPathResult metadata) {

    def time = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }.temporalElement.EX_TemporalExtent.extent
    def beginDate = time.TimePeriod.beginPosition.text() ?:
        time.TimePeriod.begin.TimeInstant.timePosition.text() ?: null
    def beginIndeterminate = time.TimePeriod.beginPosition.@indeterminatePosition.text() ?:
        time.TimePeriod.begin.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
    def endDate = time.TimePeriod.endPosition.text() ?:
        time.TimePeriod.end.TimeInstant.timePosition.text() ?: null
    def endIndeterminate = time.TimePeriod.endPosition.@indeterminatePosition.text() ?:
        time.TimePeriod.end.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
    def instant = time.TimeInstant.timePosition.text() ?: null
    def instantIndeterminate = time.TimeInstant.timePosition.@indeterminatePosition.text() ?: null

    return [
        beginDate           : beginDate,
        beginIndeterminate  : beginIndeterminate,
        endDate             : endDate,
        endIndeterminate    : endIndeterminate,
        instant             : instant,
        instantIndeterminate: instantIndeterminate
    ]
  }

  static Map parseTemporalBounding(String xml) {
    return parseTemporalBounding(new XmlSlurper().parseText(xml))
  }

  static Map parseSpatialBounding(GPathResult metadata) {

    def spatialBounding = [:]
    def space = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }.geographicElement
    def bbox = space.'**'.find { it -> it.name() == 'EX_GeographicBoundingBox' }
    if (bbox) {
      spatialBounding.put('type', 'envelope')
      spatialBounding.put('coordinates', [
          [bbox.westBoundLongitude.Decimal.toFloat(), bbox.northBoundLatitude.Decimal.toFloat()],
          [bbox.eastBoundLongitude.Decimal.toFloat(), bbox.southBoundLatitude.Decimal.toFloat()]])
    }

    return spatialBounding
  }

  static Map parseSpatialBounding(String xml) {
    return parseSpatialBounding(new XmlSlurper().parseText(xml))
  }

  static Map parseAcquisitionInfo(GPathResult metadata) {

    def acquisitionInstruments = [] as Set
    def acquisitionOperations = [] as Set
    def acquisitionPlatforms = [] as Set

    // Acquisition instrument:
    def instruments = metadata.acquisitionInformation.MI_AcquisitionInformation.instrument
        .'**'.findAll { it.name() == 'MI_Instrument' }
    instruments.each { e ->
      acquisitionInstruments.add([
          instrumentIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: null,
          instrumentType       : e.type.CharacterString.text() ?: null,
          instrumentDescription: e.description.CharacterString.text() ?: null
      ])
    }

    // Acquisition operation:
    def operations = metadata.acquisitionInformation.MI_AcquisitionInformation.operation
        .'**'.findAll { it.name() == 'MI_Operation' }
    operations.each { e ->
      acquisitionOperations.add([
          operationDescription: e.description.CharacterString.text() ?: null,
          operationIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: null,
          operationStatus     : e.status.MD_ProgressCode.@codeListValue.text() ?: null,
          operationType       : e.type.MI_OperationTypeCode.@codeListValue.text() ?: null // FIXME not sure on path
      ])
    }

    // Acquisition platform:
    def platforms = metadata.acquisitionInformation.MI_AcquisitionInformation.platform
        .'**'.findAll { it.name() == 'MI_Platform' }
    platforms.each { e ->
      acquisitionPlatforms.add([
          platformIdentifier : e.identifier.MD_Identifier.code.CharacterString.text() ?: null,
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
    formats.each { e ->
      dataFormats.add(e.name.CharacterString.text() ? (e.name.CharacterString.text() as String).toUpperCase() : null)
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
          linkUrl        : e.linkage.URL.text() ?: null,
          linkDescription: e.description.CharacterString.text() ?: null,
          linkFunction   : e.function.CI_OnLineFunctionCode.@codeListValue.text() ?: null
      ])
    }
    return links
  }

  static Set parseLinks(String xml) {
    return parseLinks(new XmlSlurper().parseText(xml))
  }

  static Set parseContacts(GPathResult metadata) {

    def contacts = [] as Set
    def contactInfo = metadata.'**'.findAll { it.name() == 'CI_ResponsibleParty' }
    contactInfo.each { e ->
      contacts.add([
          individualName  : e.individualName.CharacterString.text() ?: e.individualName.Anchor.text() ?: null,
          organizationName: e.organisationName.CharacterString.text() ?: e.organisationName.Anchor.text() ?: null,
          role            : e.role.CI_RoleCode.@codeListValue.text() ?: null
      ])
    }
    return contacts
  }

  static Set parseContacts(String xml) {
    return parseContacts(new XmlSlurper().parseText(xml))
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

  static Map mergeCollectionAndGranule(Map collection, Map granule) {
    return collection + granule
  }
}
