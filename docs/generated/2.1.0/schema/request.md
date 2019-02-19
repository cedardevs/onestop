# Search Request Schema

The shape of a search request body that can be sent to the OneStop API to execute a search against available metadata. Collections are returned by default unless a collection filter is included, resulting in granules being returned.


| Name | Type | Accepted Values | Default |
|------|------|--------|---------|
| queries| array| `oneOf`: [Text Query](components/textQuery.md)|  |
| filters| array| `oneOf`: [Datetime Filter](components/datetimeFilter.md), [Year Filter](components/yearFilter.md), [Facet Filter](components/facetFilter.md), [Geometry Filter](components/geometryFilter.md), [Collection Filter](components/collectionFilter.md), [Exclude Global Filter](components/excludeGlobalFilter.md)|  |
| facets| boolean| | false |
| summary| boolean| | true |
| page| object| [Pagination](components/page.md)|  |


## queries


### Values

#### Type:
array


#### Accepted Values:



* One Of:
  - [Text Query](components/textQuery.md)


## filters


### Values

#### Type:
array


#### Accepted Values:



* One Of:
  - [Datetime Filter](components/datetimeFilter.md)
  - [Year Filter](components/yearFilter.md)
  - [Facet Filter](components/facetFilter.md)
  - [Geometry Filter](components/geometryFilter.md)
  - [Collection Filter](components/collectionFilter.md)
  - [Exclude Global Filter](components/excludeGlobalFilter.md)


## facets

Flag to request counts of results by GCMD keywords in addition to results.

### Values

#### Type:
boolean
#### Default:
false


#### Accepted Values:





## summary

Flag to request summary of search results instead of full set of attributes.

### Values

#### Type:
boolean
#### Default:
true


#### Accepted Values:





## page

Pagination of results

### Values

#### Type:
object


#### Accepted Values:
* [Pagination](components/page.md)






