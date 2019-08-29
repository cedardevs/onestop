# Indexed ISO Metadata Fields
All of the following fields are indexed for every data type (collection and granule). Click the links for more information on each field (i.e., XPaths used for parsing, explanations of created fields, sub-fields in instances where the listed field is actually an object, etc.).
* [stagedDate](#stageddate)
* [fileIdentifier](#fileidentifier)
* [parentIdentifier](#parentIdentifier)
* [doi](#doi)
* [purpose](#purpose)
* [status](#status)
* [credit](#credit)
* [hierarchyLevelName](#hierarchylevelname)
* [title](#title)
* [alternateTitle](#alternatetitle)
* [description](#description)
* [keywords](#keywords-general--specific)
* [accessionValues](#keywords-general--specific)
* [gcmd](#keywords-general--specific){Science, ScienceServices, Locations, Instruments, Platforms, Projects, DataCenters, HorizontalResolution, VerticalResolution, TemporalResolution}
* [topicCategories](#topiccategories)
* [temporalBounding](#temporalbounding)
* [spatialBounding](#spatialbounding)
* [isGlobal](#isglobal)
* [acquisitionInstruments](#acquisitioninstruments)
* [acquisitionOperations](#acquisitionoperations)
* [acquisitionPlatforms](#acquisitionplatforms)
* [dataFormats](#dataformats)
* [links](#links)
* [contacts](#contacts)
* [creators](#creators)
* [publishers](#publishers)
* [thumbnail](#thumbnail)
* [thumbnailDescription](#thumbnaildescription)
* [creationDate](#creationdate)
* [revisionDate](#revisiondate)
* [publicationDate](#publicationdate)
* [citeAsStatements](#citeasstatements)
* [crossReferences](#crossreferences)
* [largerWorks](#largerworks)
* [useLimitation](#uselimitation)
* [legalConstraints](#legalconstraints)
* [accessFeeStatement](#accessfeestatement)
* [orderingInstructions](#orderinginstructions)
* [edition](#edition)
* [dsmm](#dsmm-values){Accessibility, DataIntegrity, DataQualityAssessment, DataQualityAssurance, DataQualityControlMonitoring, Preservability, ProductionSustainability, TransparencyTraceability, Usability, Average}
* [updateFrequency](#updatefrequency)
* [presentationForm](#presentationform)
* [services](#services)
* [serviceLinks](#serviceLinks)
# Fields In Detail
## _All parsed fields are nullable unless otherwise stated._

### `stagedDate`
* Unix timestamp of when ISO metadata record was ingested into a Staging index. This field exists entirely for internal management purposes. When the ETL job runs every 10 minutes, it updates the Search indices with any records from Staging that have a `stagedDate` value newer than the max value of what is in the respective Search index.

***

### `fileIdentifier`
This field must be present.
> /gmi:MI_Metadata/gmd:fileIdentifier/gco:CharacterString[1]

***

### `parentIdentifier`
Either path is ingested, in this order:
> /gmi:MI_Metadata/gmd:parentIdentifier/gmx:Anchor[1] 

> /gmi:MI_Metadata/gmd:parentIdentifier/gco:CharacterString[1]

***

### `doi`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor[@title="DOI"][1]

***

### `purpose`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:purpose[1]

***

### `status`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:status/gmd:MD_ProgressCode/@codeListValue[1]

***

### `credit`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:credit[1]

***

### `hierarchyLevelName`
This field must be present and have a value of "granule" (not case sensitive) in order for a record to be identified as such. Otherwise, it will be identified as a collection record.
> /gmi:MI_Metadata/gmd:hierarchyLevelName/gco:CharacterString[1]

***

### `title`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString[1]

***

### `alternateTitle`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString[1]

***

### `description`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString[1]

***

### Keywords, General & Specific
The top level path of all keyword objects is:
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification//gmd:MD_Keywords

#### `keywords`
For every keyword found at the top level path **except** accession values (see next sub-section), a `keywords` object is created with 
the following sub-fields.
Sub-fields in the table below are relative to the above path. For `values`, which is listed twice, either path is accepted.

| Sub-Field            | XPath |
|----------------------|-------|
| namespace            | ./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString[1] |
| type                 | ./gmd:type/gmd:MD_KeywordTypeCode/@codeListValue[1] |
| values               | ./gmd:keyword/gco:CharacterString |
| values               | ./gmd:keyword/gmx:Anchor |

For all keyword `values`, leading and trailing whitespace characters are trimmed.

#### `accessionValues`
The `accessionValues` are extracted in the same manner as other keyword text. However, since they are not actually keywords, they are stored separately. These are determined by a namespace value equal to 'NCEI ACCESSION NUMBER' (not case-sensitive) and only the extracted values are stored.

#### GCMD Keywords
[GCMD Keywords](https://earthdata.nasa.gov/about/gcmd/global-change-master-directory-gcmd-keywords) are a special set of hierarchical Earth science keywords that we support as "facet filters". These are determined by the namespace text containing either 'GCMD' or 'Global Change Master Directory' (not case-sensitive) and, for the specific categories:

| Category                | Namespace Text (not case-sensitive) |
|-------------------------|-------|
| Science                 | 'earth science' AND NOT 'services' |
| ScienceServices         | 'earth science services' |
| Locations               | 'location' OR 'place' |
| Instruments             | 'instrument' |
| Platforms               | 'platform' |
| Projects                | 'project' |
| DataCenters             | 'data center' |
| HorizontalResolution    | 'horizontal data resolution' |
| VerticalResolution      | 'vertical data resolution' |
| TemporalResolution      | 'temporal data resolution' |

These keywords are normalized before indexing to be title-cased; have all excess internal whitespace characters removed; to be capitalized around the delimiters `' ', '/', '.', '(', '-' and '_'`; and to attempt to maintain any acronyms present in keywords of the format 'Short Name > Long Name'. Also of note, 'Earth Science > ' and 'Earth Science Services > ' are removed from the text for science and science services keywords, respectively. 

***

### `topicCategories`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory//gmd:MD_TopicCategoryCode

***

### `temporalBounding`
The top level path of this object is:
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent[1]

Sub-fields in the table below are relative to the above path, except for `description` (full path given in this case). For fields listed twice, either path is accepted but the first path takes precedence.

In the event that the values found for either `beginDate` or `endDate` represent a year further back than -100,000,000, **only** the respective `year` field will be populated and the `date` field will be null.

| Sub-Field            | XPath |
|----------------------|-------|
| beginDate            | ./gml:TimePeriod/gml:beginPosition[1] |
| beginDate            | ./gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition[1] |
| beginIndeterminate   | ./gml:TimePeriod/gml:beginPosition/@indeterminatePosition[1] |
| beginIndeterminate   | ./gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition/@indeterminatePosition[1] |
| beginYear            | _This field is either parsed from `beginDate` or the text found in the path if it is a year value._
| endDate              | ./gml:TimePeriod/gml:endPosition[1] |
| endDate              | ./gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition[1] |
| endIndeterminate     | ./gml:TimePeriod/gml:endPosition/@indeterminatePosition[1] |
| endIndeterminate     | ./gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition/@indeterminatePosition[1] |
| endYear              | _This field is either parsed from `endDate` or the text found in the path if it is a year value._
| instant              | ./gml:TimeInstant/gml:timePosition[1] |
| instantIndeterminate | ./gml:TimeInstant/gml:timePosition/@indeterminatePosition[1] |
| description          | /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString[1]

***

### `spatialBounding`
The top level path of this object is:
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox[1]

From here, the vertices of the bounding box (which is stored as a GeoJSON Polygon) are determined by `./gmd:westBoundLongitutude/gco:Decimal` (plus east longitude, and north & south latitude). If `west == east` AND `north == south`, the geometry is interpreted as a GeoJSON Point. Likewise, if `west == east` OR `north == south`, the geometry is interpreted as a GeoJSON LineString. In the future, a geographic element in the metadata that is NOT a bounding box could be used to more accurately depict the spatial bounding.

***

### `isGlobal`
This field is calculated upon ingest of the `spatialBounding` and set to **true** if and only if the data is a bounding box from [-180, 180] longitude and [-90 to 90] latitude.

***

### `acquisitionInstruments`
The top level path of this object is:
> /gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation//gmi:MI_Instrument

Sub-fields in the table below are relative to the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| instrumentIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString[1] |
| instrumentIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor[1] |
| instrumentType                    | ./gmi:type/gco:CharacterString[1] |
| instrumentType                    | ./gmi:type/gmx:Anchor[1] |
| instrumentDescription             | ./gmi:description/gco:CharacterString[1] |

***

### `acquisitionOperations`
The top level path of this object is:
> /gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation//gmi:MI_Operation

Sub-fields in the table below are relative to the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                        | XPath |
|----------------------------------|-------|
| operationIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString[1] |
| operationIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor[1] |
| operationType                    | ./gmi:type/gmi:MI_OperationTypeCode/@codeListValue[1] |
| operationStatus                  | ./gmi:status/gmd:MD_ProgressCode/@codeListValue[1] |
| operationDescription             | ./gmi:description/gco:CharacterString[1] |

***

### `acquisitionPlatforms`
The top level path of this object is:
> /gmi:MI_Metadata/gmi:acquisitionInformation/gmi:MI_AcquisitionInformation//gmi:MI_Platform

Sub-fields in the table below are relative to the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                       | XPath |
|---------------------------------|-------|
| platformIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString[1] |
| platformIdentifier              | ./gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor[1] |
| platformDescription             | ./gmi:description/gco:CharacterString[1] |
| platformSponsor                 | ./gmi:sponsor/gmd:CI_ResponsibleParty/gmd:organisationName//gco:CharacterString |

***

### `dataFormats`
The top level path of this object is:
> /gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution//gmd:MD_Format

Sub-fields in the table below are relative to the above path.

| Sub-Field                 | XPath |
|---------------------------|-------|
| name                      | ./gmd:name/gco:CharacterString[1] |
| version                   | ./gmd:version/gco:CharacterString[1] |

***

### `links`
The top level path of this object is:
> /gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution//gmd:CI_OnlineResource

Sub-fields in the table below are relative to the above path.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| linkName                          | ./gmd:name/gco:CharacterString[1] |
| linkProtocol                      | ./gmd:protocol/gco:CharacterString[1] |
| linkUrl                           | ./gmd:linkage/gmd:URL[1] |
| linkDescription                   | ./gmd:description/gco:CharacterString[1] |
| linkFunction                      | ./gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue[1] |

***

### `contacts`
This object is determined by the path:
> **/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification//gmd:CI_ResponsibleParty**/gmd:role/gmd:CI_RoleCode[@codeListValue='pointOfContact' or @codeListValue='distributor']

Sub-fields in the table below are relative to the bolded part of the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| individualName                    | ./gmd:individualName/gco:CharacterString[1] |
| individualName                    | ./gmd:individualName/gmx:Anchor[1] |
| organizationName                  | ./gmd:organisationName/gco:CharacterString[1] |
| organizationName                  | ./gmd:organisationName/gmx:Anchor[1] |
| positionName                      | ./gmd:positionName/gco:CharacterString[1] |
| positionName                      | ./gmd:positionName/gmx:Anchor[1] |
| role                              | ./gmd:role/gmd:CI_RoleCode/@codeListValue[1] |
| email                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString[1] |
| phone                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString[1] |

***

### `creators`
This object is determined by the path:
> **/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification//gmd:CI_ResponsibleParty**/gmd:role/gmd:CI_RoleCode[@codeListValue='resourceProvider' or @codeListValue='originator' or @codeListValue='principalInvestigator' or @codeListValue='author' or @codeListValue='collaborator' or @codeListValue='coAuthor']

Sub-fields in the table below are relative to the bolded part of the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| individualName                    | ./gmd:individualName/gco:CharacterString[1] |
| individualName                    | ./gmd:individualName/gmx:Anchor[1] |
| organizationName                  | ./gmd:organisationName/gco:CharacterString[1] |
| organizationName                  | ./gmd:organisationName/gmx:Anchor[1] |
| positionName                      | ./gmd:positionName/gco:CharacterString[1] |
| positionName                      | ./gmd:positionName/gmx:Anchor[1] |
| role                              | ./gmd:role/gmd:CI_RoleCode/@codeListValue[1] |
| email                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString[1] |
| phone                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString[1] |

***

### `publishers`
This object is determined by the path:
> **/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification//gmd:CI_ResponsibleParty**/gmd:role/gmd:CI_RoleCode[@codeListValue='publisher']

Sub-fields in the table below are relative to the bolded part of the above path. For fields listed twice, either path is accepted but the first path takes precedence.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| individualName                    | ./gmd:individualName/gco:CharacterString[1] |
| individualName                    | ./gmd:individualName/gmx:Anchor[1] |
| organizationName                  | ./gmd:organisationName/gco:CharacterString[1] |
| organizationName                  | ./gmd:organisationName/gmx:Anchor[1] |
| positionName                      | ./gmd:positionName/gco:CharacterString[1] |
| positionName                      | ./gmd:positionName/gmx:Anchor[1] |
| role                              | ./gmd:role/gmd:CI_RoleCode/@codeListValue[1] |
| email                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString[1] |
| phone                             | ./gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString[1] |

***

### `thumbnail`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString[1]

***

### `thumbnailDescription`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString[1]

***

### `creationDate`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[/dateType/CI_DateTypeCode[@codeListValue='creation']]/gmd:date/gco:Date[1]

***

### `revisionDate`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[/dateType/CI_DateTypeCode[@codeListValue='revision']]/gmd:date/gco:Date[1]

***

### `publicationDate`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[dateType/CI_DateTypeCode[@codeListValue='publication']]/gmd:date/gco:Date[1]

***

### `citeAsStatements`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString[text()[contains(.,'cite')]]

Note: Text 'cite' is not case-sensitive.

***

### `crossReferences`
The top level path of this object is:
> /gmd:MD_DataIdentification//gmd:aggregationInfo/gmd:MD_AggregateInformation[gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue='crossReference']/gmd:aggregateDataSetName/gmd:CI_Citation

Sub-fields in the table below are relative to the above path.

| Sub-Field                 | XPath |
|---------------------------|-------|
| title                     | ./gmd:title/gco:CharacterString[1] |
| date                      | ./gmd:date/gmd:CI_Date/gmd:date/gco:Date[1] |
| links.linkName            | .//gmd:CI_OnlineResource/gmd:name/gco:CharacterString |
| links.linkProtocol        | .//gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString |
| links.linkUrl             | .//gmd:CI_OnlineResource/gmd:linkage/gmd:URL |
| links.linkDescription     | .//gmd:CI_OnlineResource/gmd:description/gco:CharacterString |
| links.linkFunction        | .//gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue |

***

### `largerWorks`
The top level path of this object is:
> /gmd:MD_DataIdentification//gmd:aggregationInfo/gmd:MD_AggregateInformation[gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue='largerWorkCitation']/gmd:aggregateDataSetName/gmd:CI_Citation

Sub-fields in the table below are relative to the above path.

| Sub-Field                 | XPath |
|---------------------------|-------|
| title                     | ./gmd:title/gco:CharacterString[1] |
| date                      | ./gmd:date/gmd:CI_Date/gmd:date/gco:Date[1] |
| links.linkName            | .//gmd:CI_OnlineResource/gmd:name/gco:CharacterString |
| links.linkProtocol        | .//gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString |
| links.linkUrl             | .//gmd:CI_OnlineResource/gmd:linkage/gmd:URL |
| links.linkDescription     | .//gmd:CI_OnlineResource/gmd:description/gco:CharacterString |
| links.linkFunction        | .//gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue |

***

### `useLimitation`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString[1]

***

### `legalConstraints`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString

***

### `accessFeeStatement`
> /gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:fees/gco:CharacterString[1]

***

### `orderingInstructions`
> /gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions/gco:CharacterString[1]

***

### `edition`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString[1]

***

### DSMM Values
A record has DSMM values if the following XPath is present:
> /gmi:MI_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency[gmd:nameOfMeasure/gco:CharacterString='Data Stewardship Maturity Assessment']

From here, every DSMM value is collected with the path: 

> ./gmd:result/gmd:DQ_QuantitativeResult//gco:Record 

where the measure is determined by: 

> ./gmd:result/gmd:DQ_QuantitativeResult//gco:Record/gco:CodeListValue/@codeList[text()[substring-after(.,'#')]]

and the score is given by:

> ./gmd:result/gmd:DQ_QuantitativeResult//gco:Record/gco:CodeListValue

Measures are: 
* Accessibility
* DataIntegrity
* DataQualityAssessment
* DataQualityAssurance
* DataQualityControlMonitoring
* Preservability
* ProductionSustainability
* TransparencyTraceability
* Usability

The score text is mapped to a numerical value as defined in the table below:

| CodeListValue       | Numerical Equivalent |
|---------------------|-------|
| notAvailable        | 0 |
| adHoc               | 1 |
| minimal             | 2 |
| intermediate        | 3 |
| advanced            | 4 |
| optimal             | 5 |

The `dsmmAverage` field is simply the mean average calculation of the numerical scores of the given measures.

***

### `updateFrequency`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue[1]

***

### `presentationForm`
> /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:presentationForm/gmd:CI_PresentationFormCode/@codeListValue[1]

***

### `services`
***Depreciating- this field will be empty for newly created documents but still exists in the index for older records.*** 
The entire block of XML is ingested and stored as a Base64-encoded string object. Multiple sections results in an array of these strings.
> /gmi:MI_Metadata/gmd:identificationInfo//srv:SV_ServiceIdentification

***

### `serviceLinks`
The top level object of this object is 
> /gmi:MI_Metadata/gmd:identificationInfo//srv:SV_ServiceIdentification

Sub-fields in the table below are relative to the above path.

| Sub-Field                         | XPath |
|-----------------------------------|-------|
| title                             | .//CI_Citation/gmd:title/gco:CharacterString
| alternativeTitle                  | .//CI_Citation/gmd:title/gco:CharacterString
| description                       | ./abstract/gco:CharacterString
| links.linkName                    | .//srv:SV_OperationMetadata//gmd:CI_OnlineResource/gmd:name/gco:CharacterString[1] |
| links.linkProtocol                | .//srv:SV_OperationMetadata//gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString[1] |
| links.linkUrl                     | .//srv:SV_OperationMetadata//gmd:CI_OnlineResource/gmd:linkage/gmd:URL[1] |
| links.linkDescription             | .//srv:SV_OperationMetadata//gmd:CI_OnlineResource/gmd:description/gco:CharacterString[1] |
| links.linkFunction                | .//srv:SV_OperationMetadata//gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue[1] |
