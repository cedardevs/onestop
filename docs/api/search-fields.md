<div align="center"><a href="/onestop/api">OneStop API</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

# Search Fields

## Table of Contents
* [All Record Types](#all-record-types)
* [Collections and Flattened Granules Only](#collections-and-flattened-granules-only)
* [Granules Only](#granules-only)

The table below shows all possible fields for a document returned by the OneStop Search API, along with whether they are searchable, and if so, whether an exact match is required (text fields only). Field names that are **bold** are returned when the search request `summary` flag is `true` (default value). Fields marked with a `G` are only returned for granules. Those marked with a `C` are only returned for collections. All fields are returned for flattened-granules, as a flattened granule is a granule and its collection flattened together. Note that for a returned document, if a field is not present it means it is null/was not given in the original metadata.

## All Record Types


## Collections and Flattened Granules Only


## Granules Only


|                            Field                | Searchable 	| Exact 	| Notes |
|-------------------------------------------------|:----------:	|:-----------:	|------ |
| fileIdentifier                                  |      Y     	|      Y      	|       	|
| parentIdentifier<sup>G</sup>                    |      Y     	|      Y      	|        	|
| **internalParentIdentifier**<sup>G</sup>            |      Y     	|      Y      	|Field matching the `id` value of the parent collection. Search against this using the `collectionFilter` option in the search request schema.        	|
| doi                                             |      Y     	|      Y      	|        	|
| **title**                                           |      Y     	|      N      	|        	|
| description                                     |      Y     	|      N      	|        	|
| keywords                                        |      Y     	|      N      	|All keywords found in the metadata, including GCMD values.        	|
| gcmdScience                                     |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdScienceServices                             |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdLocations                                   |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdInstruments                                 |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdPlatforms                                   |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdProjects                                    |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdDataCenters                                 |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdHorizontalResolution                        |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdVerticalResolution                          |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| gcmdTemporalResolution                          |     Y     	|      Y      	|Search against this using the `facetFilter` option in the search request schema.        	|
| **beginDate**                                       |     Y     	|      -      	|Search against this using the `dateTimeFilter` option in the search request schema.        	|
| **beginYear**                                       |     Y     	|      -      	|Year of beginDate. May be populated even if beginDate is null in event of a paleo date. To be supported in future by `dateTimeFilter` option in the search request schema.         	|
| **endDate**                                         |     Y     	|      -      	|Search against this using the `dateTimeFilter` option in the search request schema.        	|
| **endYear**                                         |     Y     	|      -      	|Year of endDate. May be populated even if endDate is null in event of a paleo date. To be supported in future by `dateTimeFilter` option in the search request schema.        	|
| **spatialBounding**                                 |     Y     	|      -      	|Search against this using the `geometryFilter` option in the search request schema.        	|
| isGlobal                                        |     Y     	|      -      	|Search against this using the `excludeGlobalFilter` option in the search request schema.        	|
| dataFormats.name                                |      Y     	|      Y      	|        	|
| dataFormats.version                             |      Y     	|      Y      	|        	|
| **links**.linkName                                  |      N     	|      -      	|        	|
| **links**.linkProtocol                              |      N     	|      -      	|        	|
| **links**.linkUrl                                   |      N     	|      -      	|        	|
| **links**.linkDescription                           |      N     	|      -      	|        	|
| **links**.linkFunction                              |      N     	|      -      	|        	|
| **serviceLinks**.title                              |      N     	|      -      	|        	|
| **serviceLinks**.alternateTitle                     |      N     	|      -      	|        	|
| **serviceLinks**.description                        |      N     	|      -      	|        	|
| **serviceLinks**.links.linkName                     |      N     	|      -      	|        	|
| **serviceLinks**.links.linkProtocol                 |      N     	|      -      	|        	|
| **serviceLinks**.links.linkUrl                      |      N     	|      -      	|        	|
| **serviceLinks**.links.linkDescription              |      N     	|      -      	|        	|
| **serviceLinks**.links.linkFunction                 |      N     	|      -      	|        	|
| individualNames<sup>C</sup>                     |      Y     	|      N      	|        	|
| organizationNames<sup>C</sup>                   |      Y     	|      N      	|        	|
| **thumbnail**                                       |      N     	|      -      	|        	|
| **citeAsStatements**                                |      N     	|      -      	|        	|
| crossReferences.title<sup>C</sup>               |      N     	|      -      	|        	|
| crossReferences.code<sup>C</sup>                |      N     	|      -      	|        	|
| crossReferences.publicationDate<sup>C</sup>     |      N     	|      -      	|        	|
| crossReferences.link.linkName<sup>C</sup>       |      N     	|      -      	|        	|
| crossReferences.link.linkProtocol<sup>C</sup>   |      N     	|      -      	|        	|
| crossReferences.link.linkUrl<sup>C</sup>        |      N     	|      -      	|        	|
| crossReferences.link.linkDescription<sup>C</sup>|      N     	|      -      	|        	|
| crossReferences.link.linkFunction<sup>C</sup>   |      N     	|      -      	|        	|
| largerWorks.title<sup>C</sup>                   |      N     	|      -      	|        	|
| largerWorks.code<sup>C</sup>                    |      N     	|      -      	|        	|
| largerWorks.publicationDate<sup>C</sup>         |      N     	|      -      	|        	|
| largerWorks.link.linkName<sup>C</sup>           |      N     	|      -      	|        	|
| largerWorks.link.linkProtocol<sup>C</sup>       |      N     	|      -      	|        	|
| largerWorks.link.linkUrl<sup>C</sup>            |      N     	|      -      	|        	|
| largerWorks.link.linkDescription<sup>C</sup>    |      N     	|      -      	|        	|
| largerWorks.link.linkFunction<sup>C</sup>       |      N     	|      -      	|        	|
| useLimitation<sup>C</sup>                       |      N     	|      -      	|        	|
| legalConstraints<sup>C</sup>                    |      N     	|      -      	|        	|
| accessFeeStatement<sup>C</sup>                  |      N     	|      -      	|        	|
| orderingInstructions<sup>C</sup>                |      N     	|      -      	|        	|
| edition<sup>C</sup>                             |      N     	|      -      	|        	|
| dsmmAverage<sup>C</sup>                         |      Y     	|      -      	|        	|

<hr>
<div align="center"><a href="#">Top of Page</a></div>