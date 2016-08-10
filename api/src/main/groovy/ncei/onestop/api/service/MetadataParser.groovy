package ncei.onestop.api.service

import groovy.json.JsonOutput
import org.apache.commons.lang3.text.WordUtils


class MetadataParser {

  public static Map parseStorageInfo(String xml) {
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
    def gcmdKeywords = [] as Set
    def gcmdScience = [] as Set
    def gcmdLocations = [] as Set
    def gcmdPlatforms = [] as Set
    def gcmdInstruments = [] as Set
    def gcmdProjects = [] as Set
    def gcmdDataResolution = [] as Set
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
    def language
    def resourceLanguage
    def resourceConstraints = [] as Set
    def securityConstraints = [] as Set
    def grid = [:]

    def metadata = new XmlSlurper().parseText(xml)
    def idInfo = metadata.identificationInfo.MD_DataIdentification
    /*
    TODO
    idInfo points to only identificationInfo node with MD_DataIdentification child, but there are some (in DEM)
    where child is SV_ServiceIdentification. Discuss with Anna -- should these be parsed as well? GP does, but
    data is stored across several fields included & not included here.
     */

    // Basic info:
    fileIdentifier = metadata.fileIdentifier.CharacterString.text()
    parentIdentifier = metadata.parentIdentifier.Anchor.text() ?: metadata.parentIdentifier.CharacterString.text() ?: null
    title = idInfo.citation.CI_Citation.title.CharacterString.text()
    alternateTitle = idInfo.citation.CI_Citation.alternateTitle.CharacterString.text() ?: null
    description = idInfo.abstract.CharacterString.text()
    thumbnail = idInfo.graphicOverview.MD_BrowseGraphic.fileName.CharacterString.text()
    language = metadata.language.LanguageCode.@codeListValue.text() ?: metadata.language.CharacterString.text()
    resourceLanguage = idInfo.language.LanguageCode.@codeListValue.text() ?: idInfo.language.CharacterString.text()

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

    // Keywords:
    topicCategories.addAll(idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text())

    def descriptiveKeywords = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    descriptiveKeywords.each { e ->
      def keywordGroup = e.'**'.findAll { it.name() == 'keyword' }
      keywordGroup.each { k ->

        def text = k.CharacterString.text()
        //def type = e.type.MD_KeywordTypeCode.@codeListValue.text() //fixme can use this for gcmd type since it's already standardized
        def namespace = e.thesaurusName.CI_Citation.title.CharacterString.text()

        def gcmd = parseGCMDKeywordNamespace(text, namespace)
        if(gcmd) {
          gcmdKeywords.add(gcmd)

        } else {
          if(text) {
            keywords.add(text)
          } else {
           return
          }
      }
      }
    }

    gcmdKeywords.each {e ->
      switch(e.keywordNamespace) {
        case KeywordType.Science:
          gcmdScience.add(e.keywordText)
          break
        case KeywordType.Location:
          gcmdLocations.add(e.keywordText)
          break
        case KeywordType.Platform:
          gcmdPlatforms.add(e.keywordText)
          break
        case KeywordType.Instrument:
          gcmdInstruments.add(e.keywordText)
          break
        case KeywordType.Project:
          gcmdProjects.add(e.keywordText)
          break
        case KeywordType.Data_Center:
          gcmdDataCenters.add(e.keywordText)
          break
        case KeywordType.Data_Resolution:
          gcmdDataResolution.add(e.keywordText)
          break
        default:
          break
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

    // Resource constraints:
    def constraints = metadata.'**'.findAll { it.name() == 'MD_Constraints' }
    constraints.each { resourceConstraints.add(it.useLimitation.CharacterString.text()) }

    constraints = metadata.'**'.findAll { it.name() == 'MD_LegalConstraints' }
    constraints.each {
      resourceConstraints.add(it.useLimitation.CharacterString.text()) // FIXME include these?
      resourceConstraints.add(it.otherConstraints.CharacterString.text())
    }
    resourceConstraints.removeAll([''])  // FIXME not sure why empty strings are showing up

    // Security constraints:
    constraints = metadata.'**'.findAll { it.name() == 'MD_SecurityConstraints' }
    constraints.each { e ->
      securityConstraints.add([
          classification      : e.classification.MD_ClassificationCode.@codeListValue.text(),
          userNote            : e.userNote.CharacterString.text(),
          classificationSystem: e.classificationSystem.CharacterString.text(),
          handlingDescription : e.handlingDescription.CharacterString.text()
      ])
    }

    // Grid info:
    def gridInfo = metadata.spatialRepresentationInfo.MD_GridSpatialRepresentation
    def axisInfo = gridInfo.'**'.findAll { it.name() == 'MD_Dimension' }
    def axisProps = []
    axisInfo.each { e ->
      axisProps.add([
          dimensionName  : e.dimensionName.MD_DimensionNameTypeCode.@codeListValue.text(),
          dimensionSize  : e.dimensionSize.Integer.text() ? e.dimensionSize.Integer.toInteger() : e.dimensionSize.Integer.text(),
          resolutionUnits: e.resolution.Measure.@uom.text(),
          resolutionSize : e.resolution.Measure.text()
      ])
    }
    grid.put('numberOfDimensions', gridInfo.numberOfDimensions.Integer.text() ? gridInfo.numberOfDimensions.Integer.toInteger() : gridInfo.numberOfDimensions.Integer.text())
    grid.put('axisDimensionProperties', axisProps)
    grid.put('cellGeometry', gridInfo.cellGeometry.MD_CellGeometryCode.@codeListValue.text())
    grid.put('transformationParameterAvailability', gridInfo.transformationParameterAvailability.Boolean.toBoolean())

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
        publicationDate       : publicationDate,
        language              : language,
        resourceLanguage      : resourceLanguage,
        resourceConstraints   : resourceConstraints,
        securityConstraints   : securityConstraints,
        grid                  : grid
    ]

    return json
  }

  public static Map mergeCollectionAndGranule(Map collection, Map granule) {

    def json = [
        fileIdentifier        : granule.fileIdentifier,
        parentIdentifier      : granule.parentIdentifier,
        title                 : granule.title,
        alternateTitle        : granule.alternateTitle ?: collection.alternateTitle ?: collection.title, //fixme?
        description           : granule.description,
        keywords              : granule.keywords,
        temporalBounding      : granule.temporalBounding,
        spatialBounding       : granule.spatialBounding,
        acquisitionInstruments: granule.acquisitionInstruments ?: collection.acquisitionInstruments,
        acquisitionOperations : granule.acquisitionOperations ?: collection.acquisitionOperations,
        acquisitionPlatforms  : granule.acquisitionPlatforms ?: collection.acquisitionPlatforms,
        links                 : granule.links,
        contacts              : collection.contacts,
        thumbnail             : granule.thumbnail ?: collection.thumbnail,
        modifiedDate          : granule.modifiedDate,
        creationDate          : granule.creationDate,
        revisionDate          : granule.revisionDate,
        publicationDate       : granule.publicationDate,
        language              : granule.language ?: collection.language,
        resourceLanguage      : granule.resourceLanguage,
        resourceConstraints   : granule.resourceConstraints ?: collection.resourceConstraints,
        securityConstraints   : granule.securityConstraints ?: collection.securityConstraints,
        grid                  : granule.grid
    ]

    return json
  }


  public static Map parseGCMDKeywordNamespace(String text, String namespace) {
    if(!namespace.toLowerCase().contains('gcmd')) { return null }

    def keywords
    def type
    switch (namespace) {
      case {it.toLowerCase().contains('location') || it.toLowerCase().contains('place')}:
        type = KeywordType.Location
        break
      case {it.toLowerCase().contains('science')}:
        type = KeywordType.Science
        break
      case {it.toLowerCase().contains('platform')}:
        type = KeywordType.Platform
        break
      case {it.toLowerCase().contains('instrument')}:
        type = KeywordType.Instrument
        break
      case {it.toLowerCase().contains('project')}:
        type = KeywordType.Project
        break
      case {it.toLowerCase().contains('data center')}:
        type = KeywordType.Data_Center
        break
      case {it.toLowerCase().contains('data resolution')}:
        type = KeywordType.Data_Resolution
        break
      default:
        type = null
        break
    }

    if(type == KeywordType.Science || type == KeywordType.Location) {
      keywords = WordUtils.capitalizeFully(text,
          " " as char, "/" as char, "." as char, "(" as char, "-" as char, "_" as char)
    } else {
      keywords = text
    }

    if(type == KeywordType.Science) {
      keywords = keywords.replace('Earth Science > ', '')
    }

    return [
        keywordText: keywords,
        keywordNamespace: type
    ]
  }

  public static enum KeywordType {
    Location, Science, Platform, Instrument, Project, Data_Center, Data_Resolution
  }
}
