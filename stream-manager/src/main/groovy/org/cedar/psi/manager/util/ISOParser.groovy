package org.cedar.psi.manager.util

import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.text.StringEscapeUtils
import org.cedar.schemas.avro.geojson.LineString
import org.cedar.schemas.avro.geojson.Point
import org.cedar.schemas.avro.geojson.Polygon
import org.cedar.schemas.avro.psi.*

class ISOParser {

  static String parseXMLMetadata(String xml) {
    return JsonOutput.toJson(parseXMLMetadataToDiscovery(xml))
  }

  static Discovery parseXMLMetadataToDiscovery(String xml) {

    def metadata = new XmlSlurper().parseText(xml)

    // Parse related data maps from the xml:
    def citationInfo = parseCitationInfo(metadata)
    def keywordsMap = parseKeywordsAndTopics(metadata)
    def dsmmMap = parseDSMM(metadata)
    def spatialMap = parseSpatialInfo(metadata)
    def services = parseServices(metadata)
    def miscellaneous = parseMiscellaneous(metadata)

    def builder = Discovery.newBuilder()
    builder.fileIdentifier = citationInfo.fileIdentifier
    builder.parentIdentifier = citationInfo.parentIdentifier
    builder.hierarchyLevelName = citationInfo.hierarchyLevelName
    builder.doi = citationInfo.doi
    builder.purpose = citationInfo.purpose
    builder.status = citationInfo.status
    builder.credit = citationInfo.credit
    builder.title = citationInfo.title
    builder.alternateTitle = citationInfo.alternateTitle
    builder.description = citationInfo.description
    builder.keywords = keywordsMap.keywords as List
    builder.topicCategories = keywordsMap.topicCategories as List
    builder.temporalBounding = parseTemporalBounding(metadata)
    builder.spatialBounding = spatialMap.spatialBounding
    builder.isGlobal = spatialMap.isGlobal
    builder.acquisitionInstruments = acquisitionInstruments(metadata)
    builder.acquisitionOperations = acquisitionOperations(metadata)
    builder.acquisitionPlatforms = acquisitionPlatforms(metadata)
    builder.dataFormats = parseDataFormats(metadata)
    builder.links = parseLinks(metadata)
    builder.responsibleParties = parseResponsibleParties(metadata)
    builder.thumbnail = citationInfo.thumbnail
    builder.thumbnailDescription = citationInfo.thumbnailDescription
    builder.creationDate = citationInfo.creationDate as String
    builder.revisionDate = citationInfo.revisionDate as String
    builder.publicationDate = citationInfo.publicationDate as String
    builder.citeAsStatements = citationInfo.citeAsStatements as List
    builder.crossReferences = citationInfo.crossReferences as List
    builder.largerWorks = citationInfo.largerWorks as List
    builder.useLimitation = citationInfo.useLimitation
    builder.legalConstraints = citationInfo.legalConstraints as List
    builder.accessFeeStatement = citationInfo.accessFeeStatement
    builder.orderingInstructions = citationInfo.orderingInstructions
    builder.edition = citationInfo.edition
    builder.dsmmAccessibility = dsmmMap.Accessibility
    builder.dsmmDataIntegrity = dsmmMap.DataIntegrity
    builder.dsmmDataQualityAssessment = dsmmMap.DataQualityAssessment
    builder.dsmmDataQualityAssurance = dsmmMap.DataQualityAssurance
    builder.dsmmDataQualityControlMonitoring = dsmmMap.DataQualityControlMonitoring
    builder.dsmmPreservability = dsmmMap.Preservability
    builder.dsmmProductionSustainability = dsmmMap.ProductionSustainability
    builder.dsmmTransparencyTraceability = dsmmMap.TransparencyTraceability
    builder.dsmmUsability = dsmmMap.Usability
    builder.dsmmAverage = dsmmMap.average
    builder.updateFrequency = miscellaneous.updateFrequency
    builder.presentationForm = miscellaneous.presentationForm
    builder.services = services as List

    return builder.build()
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

    fileIdentifier = metadata.fileIdentifier.CharacterString.text() ?: null
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
    title = idInfo.citation.CI_Citation.title.CharacterString.text() ?: null
    alternateTitle = idInfo.citation.CI_Citation.alternateTitle.CharacterString.text() ?: null
    description = idInfo.abstract.CharacterString.text() ?: null
    def thumbnailPath = idInfo.graphicOverview.MD_BrowseGraphic
    thumbnail = StringEscapeUtils.unescapeXml(thumbnailPath.fileName.CharacterString.text()) ?: null
    thumbnailDescription = thumbnailPath.fileDescription.CharacterString.text() ?: null

    // Miscellaneous dates:
    def dates = idInfo.citation.CI_Citation.'**'.findAll { it.name() == 'date' }
    dates.each { date ->
      def dateType = date.CI_Date.dateType.CI_DateTypeCode.@codeListValue.text()
      if (dateType == 'publication') {
        publicationDate = date.CI_Date.date.Date.text() ?: null
      }
      else if (dateType == 'creation') {
        creationDate = date.CI_Date.date.Date.text() ?: null
      }
      else if (dateType == 'revision') {
        revisionDate = date.CI_Date.date.Date.text() ?: null
      }
    }

    // Cite-As Statements
    def otherConstraints = idInfo.resourceConstraints.MD_LegalConstraints.'**'.findAll {
      it.name() == 'otherConstraints'
    }
    def citationConstraints = otherConstraints.findAll { it.CharacterString.text().toLowerCase().contains('cite') }
    citeAsStatements = citationConstraints.collect { it.CharacterString.text() }.toSet()

    // Cross References & Larger Works
    def aggregationInfo = metadata.'**'.findAll { it.name() == 'aggregationInfo' }

    aggregationInfo.each { aggInfo ->
      def associationType = aggInfo.MD_AggregateInformation.associationType.DS_AssociationTypeCode.@codeListValue.text() ?: null
      if (associationType == 'crossReference' || associationType == 'largerWorkCitation') {
        def citation = aggInfo.MD_AggregateInformation.aggregateDataSetName.CI_Citation
        def onlineResources = citation.'**'.findAll { it.name() == 'CI_OnlineResource' }

        def links = onlineResources.collect { parseLink(it) }

        def referenceBuilder = Reference.newBuilder()
        referenceBuilder.title = citation.title.CharacterString.text() ?: null
        referenceBuilder.date  = citation.date.CI_Date.date.Date.text() ?: null
        referenceBuilder.links = links
        def reference = referenceBuilder.build()

        if (associationType == 'crossReference') {
          crossReferences.add(reference)
        }
        else if (associationType == 'largerWorkCitation') {
          largerWorks.add(reference)
        }
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

  static Map parseKeywordsAndTopics(GPathResult metadata) {
    def extractKnownText = { k ->
      def text = k.CharacterString.text() ?: k.Anchor.text()
      return text.trim()
    }

    def idInfo = metadata.identificationInfo.MD_DataIdentification

    def keywords = [] as Set
    def topicCategories = [] as Set

    topicCategories.addAll(idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text())

    def keywordGroups = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
    keywordGroups.each { group ->
      def namespace = group.thesaurusName.CI_Citation.title.CharacterString.text()
      def type = group.type.MD_KeywordTypeCode.@codeListValue.text() ?: null
      def keywordsInGroup = group.'**'.findAll { it.name() == 'keyword' }
      def values = [] as Set

      keywordsInGroup.each { k ->
        def text = k.CharacterString.text() ?: k.Anchor.text()
        if (text) {
          // Replace any non-trimmed whitespace with a single space character (e.g., in case of tabs or linefeeds)
          values.add(text.trim().replaceAll("\\s+", " "))
        }
      }

      // Add whole group of keywords
      def builder = KeywordsElement.newBuilder()
      builder.values = values.toList()
      builder.type = type
      builder.namespace = namespace
      keywords.add(builder.build())
    }

    return [
        keywords       : keywords.toList(),
        topicCategories: topicCategories.toList()
    ]
  }

  static TemporalBounding parseTemporalBounding(GPathResult metadata) {
    def boundingExtent = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent

    def description = boundingExtent[0].description.CharacterString.text() ?: null
    def time = boundingExtent.temporalElement?.'**'?.find { it -> it.name() == 'EX_TemporalExtent' }?.extent

    String beginText, beginIndeterminateText, endText, endIndeterminateText, instantText, instantIndeterminateText
    if (time) {
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
    }

    // returns avro TemporalBounding object
    def builder = TemporalBounding.newBuilder()
    builder.beginDate = beginText
    builder.beginIndeterminate = beginIndeterminateText
    builder.endDate = endText
    builder.endIndeterminate = endIndeterminateText
    builder.instant = instantText
    builder.instantIndeterminate = instantIndeterminateText
    builder.description = description

    return builder.build()
  }

  static Map parseSpatialInfo(GPathResult metadata) {
    def space = metadata.identificationInfo.MD_DataIdentification.extent.EX_Extent.geographicElement
    def bbox = space?.'**'?.find { it -> it.name() == 'EX_GeographicBoundingBox' }

    def spatialBounding = parseBounding(bbox)
    def isGlobal = checkIsGlobal(spatialBounding)

    return ["spatialBounding": spatialBounding, "isGlobal": isGlobal]
  }

  static Object parseBounding(def bbox) {
    // bbox is empty, return a null GeoJSON object
    if (!bbox) {
      return null
    }

    def west = (bbox.westBoundLongitude == "null" || bbox.westBoundLongitude == "") ? null : bbox.westBoundLongitude.Decimal.toDouble()
    def east = (bbox.eastBoundLongitude == "null" || bbox.eastBoundLongitude == "") ? null : bbox.eastBoundLongitude.Decimal.toDouble()
    def north = (bbox.northBoundLatitude == "null" || bbox.northBoundLatitude == "") ? null : bbox.northBoundLatitude.Decimal.toDouble()
    def south = (bbox.southBoundLatitude == "null" || bbox.southBoundLatitude == "") ? null : bbox.southBoundLatitude.Decimal.toDouble()

    // all corners are null, return a null GeoJSON object
    if (west == null && east == null && north == null && south == null) {
      return null
    }

    def coordinates, builder

    if (west == east && north == south) {
      builder = Point.newBuilder()
      coordinates = [west, north]
    }
    else if (west == east || north == south) {
      // Note: Because we are parsing the 'Geographic Bounding Box' element, only horizontal or vertical lines can be
      //       determined. A diagonal line will be interpreted as a polygon.
      builder = LineString.newBuilder()
      coordinates = [[west, south], [east, north]]
    }
    else {
      // this returns with correlating null values as well
      builder = Polygon.newBuilder()
      coordinates = [[[west, south], [east, south], [east, north], [west, north], [west, south]]]
    }

    // returns avro Geometry object
    builder.coordinates = coordinates

    return builder.build()

  }

  static def checkIsGlobal(def bounds) {
    if (bounds?.type != 'Polygon') {
      return false
    }

    def coords = bounds.coordinates[0]
    def west = coords[0][0]
    def east = coords[1][0]
    def north = coords[2][1]
    def south = coords[0][1]

    return west == -180 && east == 180 && north == 90 && south == -90
  }

  static List<Instruments> acquisitionInstruments(GPathResult metadata) {
    def acquisitionInstruments = [] as Set
    def instruments = metadata.acquisitionInformation.MI_AcquisitionInformation.'**'.findAll {
      it.name() == 'MI_Instrument'
    }

    instruments.each { e ->
      def builder = Instruments.newBuilder()
      builder.instrumentIdentifier = e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null
      builder.instrumentType = e.type.CharacterString.text() ?: e.type.Anchor.text() ?: null
      builder.instrumentDescription = e.description.CharacterString.text() ?: null
      acquisitionInstruments.add(builder.build())
    }

    return acquisitionInstruments.toList()
  }

  static List<Operation> acquisitionOperations(GPathResult metadata) {
    def results = [] as Set
    def operations = metadata.acquisitionInformation.MI_AcquisitionInformation.'**'.findAll {
      it.name() == 'MI_Operation'
    }

    operations.each { e ->
      def builder = Operation.newBuilder()
      builder.operationDescription = e.description.CharacterString.text() ?: null
      builder.operationIdentifier = e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null
      builder.operationStatus = e.status.MD_ProgressCode.@codeListValue.text() ?: null
      builder.operationType = e.type.MI_OperationTypeCode.@codeListValue.text() ?: null // FIXME not sure on path
      results.add(builder.build())
    }
    return results.toList()
  }

  static List<Platform> acquisitionPlatforms(GPathResult metadata) {
    def results = [] as Set
    def platforms = metadata.acquisitionInformation.MI_AcquisitionInformation.'**'.findAll {
      it.name() == 'MI_Platform'
    }

    platforms.each { e ->
      def builder = Platform.newBuilder()
      builder.platformIdentifier = e.identifier.MD_Identifier.code.CharacterString.text() ?: e.identifier.MD_Identifier.code.Anchor.text() ?: null
      builder.platformDescription = e.description.CharacterString.text() ?: null
      builder.platformSponsor = e.sponsor.CI_ResponsibleParty.organisationName.'**'.findAll {
        it.name() == 'CharacterString'
      }*.text()
      results.add(builder.build())
    }
    return results.toList()
  }

  static List<DataFormat> parseDataFormats(GPathResult metadata) {
    def formats = metadata.distributionInfo.MD_Distribution.'**'.findAll { it.name() == 'MD_Format' }
    def uniqueFormat = formats.collect(ISOParser.&parseDataFormat).findAll() as Set
    return uniqueFormat.toList()
  }

  static DataFormat parseDataFormat(GPathResult node) {
    if (!node) { return null }
    def builder = DataFormat.newBuilder()
    builder.name    = node?.name?.CharacterString.text() ? (node.name.CharacterString.text() as String).toUpperCase() : null
    builder.version = node.version.CharacterString.text() ?: null
    return builder.build()
  }

  static List<Link> parseLinks(GPathResult metadata) {
    def linkNodes = metadata.distributionInfo.MD_Distribution.'**'.findAll {
      it.name() == 'CI_OnlineResource'
    }
    def uniqueLinks = linkNodes.collect(ISOParser.&parseLink).findAll() as Set
    return uniqueLinks.toList()
  }

  static Link parseLink(GPathResult node) {
    if (!node) { return null }
    def builder = Link.newBuilder()
    builder.linkName        = node.name?.CharacterString?.text()?.trim() ?: null
    builder.linkProtocol    = node.protocol?.CharacterString?.text()?.trim() ?: null
    builder.linkUrl         = node.linkage?.URL?.text() ? StringEscapeUtils.unescapeXml(node.linkage.URL.text()) : null
    builder.linkDescription = node.description?.CharacterString?.text()?.trim() ?: null
    builder.linkFunction    = node.function?.CI_OnLineFunctionCode?.@codeListValue?.text()?.trim() ?: null
    return builder.build()
  }

  static List<ResponsibleParty> parseResponsibleParties(GPathResult metadata) {
    def partyNodes = metadata.identificationInfo.MD_DataIdentification.'**'.findAll {
      it.name() == 'CI_ResponsibleParty'
    }
    def uniqueParties = partyNodes.collect(ISOParser.&parseParty).findAll() as Set
    return uniqueParties.toList()
  }

  static ResponsibleParty parseParty(GPathResult node) {
    if (!node) { return null }
    String individualName = node.individualName.CharacterString.text() ?: node.individualName.Anchor.text() ?: null
    String organizationName = node.organisationName.CharacterString.text() ?: node.organisationName.Anchor.text() ?: null
    String positionName = node.positionName.CharacterString.text() ?: node.positionName.Anchor.text() ?: null
    String role = node.role.CI_RoleCode.@codeListValue.text() ?: null
    String email = node.contactInfo.CI_Contact.address.CI_Address.electronicMailAddress.CharacterString.text() ?: null
    String phone = node.contactInfo.CI_Contact.phone.CI_Telephone.voice.CharacterString.text() ?: null
    def builder = ResponsibleParty.newBuilder()
    builder.individualName  = individualName
    builder.organizationName= organizationName
    builder.positionName    = positionName
    builder.role            = role
    builder.email           = email
    builder.phone           = phone
    return builder.build()
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
    if (dsmm) {
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

  static Map parseMiscellaneous(GPathResult metadata) {
    def dataId = metadata.identificationInfo.MD_DataIdentification ?: null
    def updateFrequency = dataId.resourceMaintenance.MD_MaintenanceInformation.maintenanceAndUpdateFrequency.MD_MaintenanceFrequencyCode.@codeListValue.text() ?: null
    def presentationForm = dataId.citation.CI_Citation.presentationForm.CI_PresentationFormCode.@codeListValue.text() ?: null
    return [
        updateFrequency : updateFrequency,
        presentationForm: presentationForm
    ]
  }

  static List<Service> parseServices(GPathResult metadata) {
    def serviceNodes = metadata.identificationInfo.'**'.findAll {
      it.name() == 'SV_ServiceIdentification'
    }
    def uniqueServices = serviceNodes.collect(ISOParser.&parseService).findAll().toSet()
    return uniqueServices.toList()
  }

  static Service parseService(GPathResult node) {
    if (!node) { return null }
    def operationNodes = node.'**'.findAll {
      it.name() == 'containsOperations'
    }
    def uniqueOperations = operationNodes.collect({parseLink(it?.SV_OperationMetadata?.connectPoint?.CI_OnlineResource)}).findAll().toSet()
    def builder = Service.newBuilder()
    builder.title          = (node?.citation?.CI_Citation?.title?.CharacterString as String).trim()
    builder.alternateTitle = (node?.citation?.CI_Citation?.alternateTitle?.CharacterString as String).trim()
    builder.description    = (node?.abstract?.CharacterString as String).trim()
    builder.date           = node?.citation?.CI_Citation?.date?.Date as String
    builder.dateType       = node?.citation?.CI_Citation?.dateType?.CI_DateTypeCode as String
    builder.pointOfContact = parseParty(node?.pointOfContact?.CI_ResponsibleParty)
    builder.operations     = uniqueOperations.toList()
    return builder.build()
  }

}
