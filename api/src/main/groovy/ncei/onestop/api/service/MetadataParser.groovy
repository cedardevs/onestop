package ncei.onestop.api.service

import groovy.json.JsonBuilder


class MetadataParser {

    public static void main(String[] args) {
        def cl = ClassLoader.systemClassLoader
        def metadata = cl.getResourceAsStream("DEM_metadata.xml").text
        //def metadata = cl.getResourceAsStream("GHRSST_metadata.xml").text
        println parseXMLMetadata(metadata)
    }

    public static String parseXMLMetadata(String xml) {

        def fileIdentifier
        def parentIdentifier
        def title
        def alternateTitle
        def description
        def keywords = [] as Set
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

        // Basic info:
        fileIdentifier = metadata.fileIdentifier.CharacterString.text()
        parentIdentifier = metadata.parentIdentifier.CharacterString.text()
        title = idInfo.citation.CI_Citation.title.CharacterString.text()
        alternateTitle = idInfo.citation.CI_Citation.alternateTitle.CharacterString.text()
        description = idInfo.abstract.CharacterString.text()
        thumbnail = idInfo.graphicOverview.MD_BrowseGraphic.fileName.CharacterString.text()
        language = metadata.language.LanguageCode.@codeListValue.text() ?: metadata.language.CharacterString.text()
        resourceLanguage = idInfo.language.LanguageCode.@codeListValue.text() ?: idInfo.language.CharacterString.text()

        // Miscellaneous dates:
        modifiedDate = metadata.dateStamp.Date.text() ?: metadata.dateStamp.DateTime.text()
        def dates = idInfo.citation.CI_Citation.'**'.findAll { it.name() == 'date' }
        dates.each { date ->
            def dateType = date.CI_Date.dateType.CI_DateTypeCode.@codeListValue.text()
            if(dateType == 'publication') {
                publicationDate = date.CI_Date.date.Date.text()
            } else if(dateType == 'creation') {
                creationDate = date.CI_Date.date.Date.text()
            } else if(dateType == 'revision') {
                revisionDate = date.CI_Date.date.Date.text()
            }
        }

        // Keywords:
        def topicCategories = idInfo.topicCategory.'**'.findAll { it.name() == 'MD_TopicCategoryCode' }*.text()
        topicCategories.each {  e ->
            keywords.add( [ keywordText: e, keywordType: null, keywordNamespace: null ] )
        }

        def descriptiveKeywords = idInfo.descriptiveKeywords.'**'.findAll { it.name() == 'MD_Keywords' }
        descriptiveKeywords.each { e ->
            def keywordGroup = e.'**'.findAll { it.name() == 'keyword'}
            keywordGroup.each { k ->
                keywords.add([
                        keywordText     : k.CharacterString.text(),
                        keywordType     : e.type.MD_KeywordTypeCode.@codeListValue.text(),
                        keywordNamespace: e.thesaurusName.CI_Citation.title.CharacterString.text()
                ])
            }
        }

        // Temporal bounding:
        def time = idInfo.extent.EX_Extent.'**'.find { e ->
            e.@id.text() == 'boundingExtent'
        }.temporalElement.EX_TemporalExtent.extent
        def beginDate = time.TimePeriod.beginPosition.text() ?:
                time.TimePeriod.begin.TimeInstant.timePosition.text()
        def beginIndeterminate = time.TimePeriod.beginPosition.@indeterminatePosition.text() ?:
                time.TimePeriod.begin.TimeInstant.timePosition.@indeterminatePosition.text()
        def endDate = time.TimePeriod.endPosition.text() ?:
                time.TimePeriod.end.TimeInstant.timePosition.text()
        def endIndeterminate = time.TimePeriod.endPosition.@indeterminatePosition.text() ?:
                time.TimePeriod.end.TimeInstant.timePosition.@indeterminatePosition.text()
        def instant = time.TimeInstant.timePosition.text()
        def instantIndeterminate = time.TimeInstant.timePosition.@indeterminatePosition.text()

        temporalBounding.put('beginDate', beginDate)
        temporalBounding.put('beginIndeterminate', beginIndeterminate)
        temporalBounding.put('endDate', endDate)
        temporalBounding.put('endIndeterminate', endIndeterminate)
        temporalBounding.put('instant', instant)
        temporalBounding.put('instantIndeterminate', instantIndeterminate)

        // Spatial bounding:
        // TODO

        // Acquisition instrument:
        def instruments = metadata.acquisitionInformation.MI_AcquisitionInformation.instrument
                .'**'.findAll { it.name() == 'MI_Instrument' }
        instruments.each { e ->
            acquisitionInstruments.add( [
                    instrumentIdentifier: e.identifier.MD_Identifier.code.CharacterString.text(),
                    instrumentType: e.type.CharacterString.text(),
                    instrumentDescription: e.description.CharacterString.text()
            ] )
        }

        // Acquisition operation:
        def operations = metadata.acquisitionInformation.MI_AcquisitionInformation.operation
                .'**'.findAll { it.name == 'MI_Operation' }
        operations.each { e ->
            acquisitionOperations.add( [
                    operationDescription: e.description.CharacterString.text(),
                    operationIdentifier: e.identifier.MD_Identifier.code.CharacterString.text(),
                    operationStatus: e.status.MD_ProgressCode.@codeListValue.text(),
                    operationType: e.type.MI_OperationTypeCode.@codeListValue.text() // FIXME not sure on path
            ] )
        }

        // Acquisition platform:
        def platforms = metadata.acquisitionInformation.MI_AcquisitionInformation.platform
                .'**'.findAll { it.name() == 'MI_Platform' }
        platforms.each { e ->
            acquisitionPlatforms.add( [
                    platformIdentifier: e.identifier.MD_Identifier.code.CharacterString.text(),
                    platformDescription: e.description.CharacterString.text(),
                    platformSponsor: e.sponsor.CI_ResponsibleParty.organisationName
                            .'**'.findAll { it.name() == 'CharacterString' }*.text()
            ] )
        }

        // Links:
        def linkage = metadata.'**'.findAll { it.name() == 'CI_OnlineResource' }
        linkage.each { e ->
            links.add( [
                    linkName: e.name.CharacterString.text(),
                    linkUrl: e.linkage.URL.text(),
                    linkDescription: e.description.CharacterString.text(),
                    linkFunction: e.function.CI_OnLineFunctionCode.@codeListValue.text()
            ] )
        }

        // Contacts:
        def contactInfo = metadata.'**'.findAll { it.name() == 'CI_ResponsibleParty' }
        contactInfo.each { e ->
            contacts.add( [
                    individualName: e.individualName.CharacterString.text() ?: e.individualName.Anchor.text(),
                    organizationName: e.organisationName.CharacterString.text() ?: e.organisationName.Anchor.text(),
                    role: e.role.CI_RoleCode.@codeListValue.text()
            ] )
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
            securityConstraints.add( [
                    classification: e.classification.MD_ClassificationCode.@codeListValue.text(),
                    userNote: e.userNote.CharacterString.text(),
                    classificationSystem: e.classificationSystem.CharacterString.text(),
                    handlingDescription: e.handlingDescription.CharacterString.text()
            ] )
        }

        // Grid info:
        def gridInfo = metadata.spatialRepresentationInfo.MD_GridSpatialRepresentation
       def axisInfo = gridInfo.'**'.findAll { it.name() == 'MD_Dimension'}
        def axisProps = []
        axisInfo.each { e ->
            axisProps.add( [
                    dimensionName: e.dimensionName.MD_DimensionNameTypeCode.@codeListValue.text(),
                    dimensionSize: e.dimensionSize.Integer.text() ? e.dimensionSize.Integer.toInteger() : e.dimensionSize.Integer.text(),
                    resolutionUnits: e.resolution.Measure.@uom.text(),
                    resolutionSize: e.resolution.Measure.text()
            ] )
        }
        grid.put('numberOfDimensions', gridInfo.numberOfDimensions.Integer.toInteger())
        grid.put('axisDimensionProperties', axisProps)
        grid.put('cellGeometry', gridInfo.cellGeometry.MD_CellGeometryCode.@codeListValue.text())
        grid.put('transformationParameterAvailability', gridInfo.transformationParameterAvailability.Boolean.toBoolean())


        // Build JSON:
        def json = new JsonBuilder()
        json    fileIdentifier: fileIdentifier,
                parentIdentifier: parentIdentifier,
                title: title,
                alternateTitle: alternateTitle,
                description: description,
                keywords: keywords,
                temporalBounding: temporalBounding,
                spatialBounding: spatialBounding,
                acquisitionInstruments: acquisitionInstruments,
                acquisitionOperations: acquisitionOperations,
                acquisitionPlatforms: acquisitionPlatforms,
                links: links,
                contacts: contacts,
                thumbnail: thumbnail,
                modifiedDate: modifiedDate,
                creationDate: creationDate,
                revisionDate: revisionDate,
                publicationDate: publicationDate,
                language: language,
                resourceLanguage: resourceLanguage,
                resourceConstraints: resourceConstraints,
                securityConstraints: securityConstraints,
                grid: grid

        return json.toPrettyString()
    }
}
