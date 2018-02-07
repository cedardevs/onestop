package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang3.StringEscapeUtils
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
    def miscellaneous = parseMiscellaneous(metadata)

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
        dsmmAverage                     : dsmmMap.average,
        updateFrequency                 : miscellaneous.updateFrequency,
        presentationForm                : miscellaneous.presentationForm
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
    thumbnail = StringEscapeUtils.unescapeXml(idInfo.graphicOverview.MD_BrowseGraphic.fileName.CharacterString.text())

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

    def keywordGroups = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    keywordGroups.each { group ->
      def keywordsInGroup = group.'**'.findAll { it.name() == 'keyword' }
      keywordsInGroup.each { k ->
        def text = k.CharacterString.text() ?: k.Anchor.text()
        def namespace = group.thesaurusName.CI_Citation.title.CharacterString.text()

        if (text) {
          text = text.trim()
          if (namespace.toLowerCase().contains('gcmd')) {
            switch (namespace) {
              case { it.toLowerCase().contains('science') }:
                text = normalizeHierarchyKeyword(text)
                gcmdScience.addAll(tokenizeHierarchyKeyword(text))
                break
              case { it.toLowerCase().contains('location') || it.toLowerCase().contains('place') }:
                text = normalizeHierarchyKeyword(text)
                gcmdLocations.addAll(tokenizeHierarchyKeyword(text))
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
            }
          }
          keywords.add(text)
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

  static final char[] capitalizingDelimiters = [' ', '/', '.', '(', '-', '_'].collect({ it as char })

  static String normalizeHierarchyKeyword(String text) {
    return WordUtils.capitalizeFully(text, capitalizingDelimiters)
        .replace('Earth Science > ', '')
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

  static Map parseTemporalBounding(GPathResult metadata) {

    def time = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }?.temporalElement?.EX_TemporalExtent?.extent

    def beginDate, beginIndeterminate, endDate, endIndeterminate, instant, instantIndeterminate
    if(time) {
      beginDate = time.TimePeriod.beginPosition.text() ?:
          time.TimePeriod.begin.TimeInstant.timePosition.text() ?: null
      beginIndeterminate = time.TimePeriod.beginPosition.@indeterminatePosition.text() ?:
          time.TimePeriod.begin.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
      endDate = time.TimePeriod.endPosition.text() ?:
          time.TimePeriod.end.TimeInstant.timePosition.text() ?: null
      endIndeterminate = time.TimePeriod.endPosition.@indeterminatePosition.text() ?:
          time.TimePeriod.end.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
      instant = time.TimeInstant.timePosition.text() ?: null
      instantIndeterminate = time.TimeInstant.timePosition.@indeterminatePosition.text() ?: null
    }


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

  static Map parseSpatialInfo(GPathResult metadata) {
    def space = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }?.geographicElement
    def bbox = space?.'**'?.find { it -> it.name() == 'EX_GeographicBoundingBox' }

    def spatialBounding = parseBounding(bbox)
    def isGlobal = checkIsGlobal(spatialBounding)

    return ["spatialBounding": spatialBounding, "isGlobal": isGlobal]
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

  static Map parseSpatialInfo(String xml) {
    return parseSpatialInfo(new XmlSlurper().parseText(xml))
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

  static Map mergeCollectionAndGranule(Map collection, Map granule) {
    return collection + granule
  }
}
