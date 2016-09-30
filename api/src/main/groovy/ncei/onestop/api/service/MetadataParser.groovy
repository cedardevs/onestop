package ncei.onestop.api.service

import groovy.json.JsonOutput
import org.apache.commons.lang3.text.WordUtils

class MetadataParser {

  public static Map parseIdentifierInfo(String xml) {
    def slurped = new XmlSlurper().parseText(xml)
    return [
        id: slurped.fileIdentifier.CharacterString.text(),
        parentId: slurped.parentIdentifier.Anchor.text() ?: slurped.parentIdentifier.CharacterString.text() ?: null
    ]
  }

  public static String parseXMLMetadata(String xml) {
    return JsonOutput.toJson(parseXMLMetadataToMap(xml))
  }

  public static Map parseXMLMetadataToMap(String xml) {

    def fileIdentifier
    def parentIdentifier
    def title
    def alternateTitle
    def description
    def keywords = [] as Set
    def topicCategories = [] as Set
    def gcmdScience = [] as Set
    def gcmdLocations = [] as Set
    def gcmdPlatforms = [] as Set
    def gcmdInstruments = [] as Set
    def gcmdProjects = [] as Set
    def gcmdDataResolution = [] as Set
    def gcmdDataCenters = [] as Set
    def temporalBounding = [:]
    def spatialBounding = [:]
    def acquisitionInstruments = [] as Set
    def acquisitionOperations = [] as Set
    def acquisitionPlatforms = [] as Set
    def links = [] as Set
    def contacts = [] as Set
    def thumbnail
    def modifiedDate
    def creationDate
    def revisionDate
    def publicationDate

    def metadata = new XmlSlurper().parseText(xml)
    def idInfo = metadata.identificationInfo.MD_DataIdentification

    // Basic info:
    fileIdentifier = metadata.fileIdentifier.CharacterString.text()
    parentIdentifier = metadata.parentIdentifier.Anchor.text() ?: metadata.parentIdentifier.CharacterString.text() ?: null
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

    // Keywords & topics:
    topicCategories.addAll(idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text())

    def descriptiveKeywords = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    descriptiveKeywords.each { e ->
      def keywordGroup = e.'**'.findAll { it.name() == 'keyword' }
      keywordGroup.each { k ->

        def text = k.CharacterString.text() as String
        def namespace = e.thesaurusName.CI_Citation.title.CharacterString.text()

        if(text) {
          if(namespace.toLowerCase().contains('gcmd')) {
            switch(namespace) {
              case {it.toLowerCase().contains('science')}:
                text = WordUtils.capitalizeFully(text,
                    " " as char, "/" as char, "." as char, "(" as char, "-" as char, "_" as char)
                text = text.replace('Earth Science > ', '')
                gcmdScience.add(text)
                break
              case {it.toLowerCase().contains('location') || it.toLowerCase().contains('place')}:
                def locationKeywords = WordUtils.capitalizeFully(text,
                    " " as char, "/" as char, "." as char, "(" as char, "-" as char, "_" as char)
                gcmdLocations.add(locationKeywords)
                break
              case {it.toLowerCase().contains('platform')}:
                gcmdPlatforms.add(text)
                break
              case {it.toLowerCase().contains('instrument')}:
                gcmdInstruments.add(text)
                break
              case {it.toLowerCase().contains('data center')}:
                gcmdDataCenters.add(text)
                break
              case {it.toLowerCase().contains('data resolution')}:
                gcmdDataResolution.add(text)
                break
              case {it.toLowerCase().contains('project')}:
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

    // Temporal bounding:
    def time = idInfo.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }.temporalElement.EX_TemporalExtent.extent
    def beginDate = time.TimePeriod.beginPosition.text() ?:
        time.TimePeriod.begin.TimeInstant.timePosition.text() ?: null
    def beginIndeterminate = time.TimePeriod.beginPosition.@indeterminatePosition.text() ?:
        time.TimePeriod.begin.TimeInstant.timePosition.@indeterminatePosition.text()
    def endDate = time.TimePeriod.endPosition.text() ?:
        time.TimePeriod.end.TimeInstant.timePosition.text() ?: null
    def endIndeterminate = time.TimePeriod.endPosition.@indeterminatePosition.text() ?:
        time.TimePeriod.end.TimeInstant.timePosition.@indeterminatePosition.text()
    def instant = time.TimeInstant.timePosition.text() ?: null
    def instantIndeterminate = time.TimeInstant.timePosition.@indeterminatePosition.text()

    temporalBounding.put('beginDate', beginDate)
    temporalBounding.put('beginIndeterminate', beginIndeterminate)
    temporalBounding.put('endDate', endDate)
    temporalBounding.put('endIndeterminate', endIndeterminate)
    temporalBounding.put('instant', instant)
    temporalBounding.put('instantIndeterminate', instantIndeterminate)

    // Spatial bounding:
    def space = idInfo.extent.EX_Extent.'**'.find { e ->
      e.@id.text() == 'boundingExtent'
    }.geographicElement
    def bbox = space.'**'.find { it -> it.name() == 'EX_GeographicBoundingBox' }
    if (bbox) {
      spatialBounding.put('type', 'envelope')
      spatialBounding.put('coordinates', [
          [bbox.westBoundLongitude.Decimal.toFloat(), bbox.northBoundLatitude.Decimal.toFloat()],
          [bbox.eastBoundLongitude.Decimal.toFloat(), bbox.southBoundLatitude.Decimal.toFloat()]])
    }

    // Acquisition instrument:
    def instruments = metadata.acquisitionInformation.MI_AcquisitionInformation.instrument
        .'**'.findAll { it.name() == 'MI_Instrument' }
    instruments.each { e ->
      acquisitionInstruments.add([
          instrumentIdentifier : e.identifier.MD_Identifier.code.CharacterString.text(),
          instrumentType       : e.type.CharacterString.text(),
          instrumentDescription: e.description.CharacterString.text()
      ])
    }

    // Acquisition operation:
    def operations = metadata.acquisitionInformation.MI_AcquisitionInformation.operation
        .'**'.findAll { it.name == 'MI_Operation' }
    operations.each { e ->
      acquisitionOperations.add([
          operationDescription: e.description.CharacterString.text(),
          operationIdentifier : e.identifier.MD_Identifier.code.CharacterString.text(),
          operationStatus     : e.status.MD_ProgressCode.@codeListValue.text(),
          operationType       : e.type.MI_OperationTypeCode.@codeListValue.text() // FIXME not sure on path
      ])
    }

    // Acquisition platform:
    def platforms = metadata.acquisitionInformation.MI_AcquisitionInformation.platform
        .'**'.findAll { it.name() == 'MI_Platform' }
    platforms.each { e ->
      acquisitionPlatforms.add([
          platformIdentifier : e.identifier.MD_Identifier.code.CharacterString.text(),
          platformDescription: e.description.CharacterString.text(),
          platformSponsor    : e.sponsor.CI_ResponsibleParty.organisationName
              .'**'.findAll { it.name() == 'CharacterString' }*.text()
      ])
    }

    // Links:
    def linkage = metadata.distributionInfo.MD_Distribution.'**'.findAll { it.name() == 'CI_OnlineResource' }
    linkage.each { e ->
      links.add([
          linkName       : e.name.CharacterString.text(),
          linkUrl        : e.linkage.URL.text(),
          linkDescription: e.description.CharacterString.text(),
          linkFunction   : e.function.CI_OnLineFunctionCode.@codeListValue.text()
      ])
    }

    // Contacts:
    def contactInfo = metadata.'**'.findAll { it.name() == 'CI_ResponsibleParty' }
    contactInfo.each { e ->
      contacts.add([
          individualName  : e.individualName.CharacterString.text() ?: e.individualName.Anchor.text(),
          organizationName: e.organisationName.CharacterString.text() ?: e.organisationName.Anchor.text(),
          role            : e.role.CI_RoleCode.@codeListValue.text()
      ])
    }

    // Build JSON:
    def json = [
        fileIdentifier        : fileIdentifier,
        parentIdentifier      : parentIdentifier,
        title                 : title,
        alternateTitle        : alternateTitle,
        description           : description,
        keywords              : keywords,
        topicCategories       : topicCategories,
        gcmdScience           : gcmdScience,
        gcmdLocations         : gcmdLocations,
        gcmdInstruments       : gcmdInstruments,
        gcmdPlatforms         : gcmdPlatforms,
        gcmdProjects          : gcmdProjects,
        gcmdDataCenters       : gcmdDataCenters,
        gcmdDataResolution    : gcmdDataResolution,
        temporalBounding      : temporalBounding,
        spatialBounding       : spatialBounding,
        acquisitionInstruments: acquisitionInstruments,
        acquisitionOperations : acquisitionOperations,
        acquisitionPlatforms  : acquisitionPlatforms,
        links                 : links,
        contacts              : contacts,
        thumbnail             : thumbnail,
        modifiedDate          : modifiedDate,
        creationDate          : creationDate,
        revisionDate          : revisionDate,
        publicationDate       : publicationDate
    ]

    return json
  }

  public static Map mergeCollectionAndGranule(Map collection, Map granule) {
    return collection + granule
  }
}
