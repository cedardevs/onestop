# Search API Requests

The OneStop API request schema can be viewed [here](https://github.com/cedardevs/onestop/blob/master/search/src/main/resources/onestop-request-schema.json). All available functionality and request format requirements are detailed at length in this document.

The API provides three Search endpoints for search and discovery of data. The following are some examples of well-formatted queries for interacting with the OneStop `/onestop-search/search/collection`, `/onestop-search/search/granule`, and `/onestop-search/search/flattened-granule` (version 2.1+) resource endpoints. Note that either a POST or GET request with a correctly formatted JSON request body will work.

All three endpoints expect the same request schema format to be used but return data of the type in the URL.

## Basic Query

Querying for records containing the word 'temperature':

``` json
 {  
 "queries": [
     {
       "type" : "queryText",
       "value": "temperature"
     }       
   ]
}
```

A query searching for multiple terms can be submitted:

``` json
 {  
 "queries": [
     {
       "type" : "queryText",
       "value": "temperature"
     },
     {
       "type" : "queryText",
       "value": "pressure"
     } 
   ]   
}
```

When providing multiple queryText objects in your search request, keep in mind that they will be combined with a logical AND. A single queryText can be supplied to accomplish the same logical combination (`temperature AND pressure`).

### Query Tips
* Wrap a search phrase in double quotes for an exact match. Note that capitalization is ignored.
  - Example: `"sea surface temperature"`

* Use `+` to indicate that a search term **must** appear in the results and `-` to indicate that it **must not**. Terms without a `+` or `-` are considered optional. This causes `-` characters within terms to be ignored; use double quotes to search for a term with a hyphen in it.
  - Example: `temperature pressure +air -sea`

* Using `AND`, `OR`, and `AND NOT` provides similar logic to `+` and `-`, but they introduce operator precedence which makes for a more complicated query structure. The following example gives the same results as the previous one.
  - Example: `((temperature AND air) OR (pressure AND air) OR air) AND NOT sea`

* The title, description, and keywords of a data set's metadata can be searched directly by appending the field name and a colon to the beginning of your search term (remember -- no spaces before or after the colon and wrap multi-word terms in parentheses). Exact matches can be requested here as well.
  - Example: `description:lakes`
  - Example: `title:"Tsunami Inundation"`
  - Example: `keywords:(ice deformation)`

## Facets (i.e., GCMD Keywords)

All queries will have the option to return relevant facets as well. Facets assist by providing relevant categories and values for further filtering your search. To enable facets, add "facets: true" at the top-level of your query object:

``` json
 {
  "queries": [
    {
      "type" : "queryText",
      "value": "temperature"
    }       
  ],
  "facets": true    
}
```


To search on a returned facet, either alone or in combination with other criteria:

``` json 
{
  "filters":[
    {
      "type"  : "facet",
      "name"  : "science",
      "values": [
        "Atmosphere > Aerosols"
      ]
    }       
  ]    
}
```
**NOTE: The text in** `values` **must match the facet exactly. OneStop does not include** `Earth Science` **or** `Earth Science Services` **at the beginning of the respective keyword categories, as demonstrated in the above example.**

## Geospatial and Temporal Filters
OneStop's spatial and time range filters utilize an optional `relation` parameter to describe how the query geometry/time range should relate to the geometries/time ranges of the results. The relation values used are the same as those used by the underlying Elasticsearch search engine ([documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-shape-query.html#_spatial_relations)).

These relations are defined as such:
* `intersects` -- Results must intersect the query value. This is the default and is used when no `relation` value is specified.
* `within` -- Results are within query value.
* `disjoint` -- Results have nothing in common with query (opposite of `intersects`).
* `contains` -- Results contain the query value.

### Filtering By Geometry
The OneStop API supports geospatial queries using valid GeoJSON (see [this guide](https://macwright.org/2015/03/23/geojson-second-bite) for helpful info). Valid Longitude values
(X-coordinates) are between -180 and +180, valid Latitude values (Y-values) are between -90 and +90 degrees:

Point Filter & Contains Example
``` json
 {
  "filters": [
    {
      "type"    : "geometry",
      "relation": "contains",
      "geometry": {
        "type"       : "Point",
        "coordinates": [
          22.123,
          -45.245
        ]
      }
    }       
  ]    
}
```


Bounding Box Filter & Default (`intersects`) Example
``` json
 {
  "filters": [
    {
      "type": "geometry",
      "geometry":  {
        "type"       : "Polygon",
        "coordinates": [
          [
            [
              -5.99,
              45.99
            ],
            [
              -5.99,
              36.49
            ],
            [
              36.49,
              30.01
            ],
            [
              36.49,
              45.99
            ],
            [
              -5.99,
              45.99
            ]
          ]
        ]
      }          
    }       
  ]    
}
```

### Exclude Global Results Filter
It may be desirable to exclude results which geospatially cover the whole earth. To do this, use the following filter criteria:

``` json
 {
  "filters": [
    {
      "type" : "excludeGlobal",
      "value": true
    }       
  ]    
}
```

### Date Range Filters

OneStop has two ways to filter results by time: `datetime` and `year`. In both types, date ranges may be specified alone or in conjunction with other search criteria. A valid temporal query may contain only the `before` or `after` value, or both, in addition to the optional `relation` descriptor.

For the `datetime` filter, date formats must follow the full ISO 8601 standard in UTC, i.e., `yyyy-MM-dd'T'HH:mm:ss'Z'`. The `datetime` filter may be used to find results with year values greater than or equal to -100,000,000. To filter results by years less than -100,000,000, the `year` filter must be used. As `year` simply filters by year only, it can be used for any year, including the current era. The `year` filter can be used to find very old paleontological results where a datetime value is programmatically not possible.

Fully-Defined Datetime Range With Omitted Relation Example (default `intersects`)
``` json
 {
  "filters": [
    {
      "type"  : "datetime",
      "before": "2016-06-15T20:20:58Z",
      "after" : "2015-09-22T10:30:06.000Z"
    }       
  ]    
}
```

Year Range With Declared Relation Example
``` json
 {
  "filters": [
    {
      "type"     : "year",
      "relation" : "within",
      "before"   : -1000000000
    }       
  ]    
}
```

## Pagination and Summarized Results
The number of results returned can be specified by the `page` property object in your request body. Max specifies the maximum number of results to be returned. Offset indicates the number of results to skip. If no page element is found in the request, the API defaults to the first 10 results (i.e., `max` is 10 and `offset` is 0). In the example below, results returned would be numbers 41 through 60, assuming a request matching at least 60 documents. If the max value specified exceeds the number of matching results, all matching results will be returned.

Elasticsearch imposes a hard limit of only the first 10,000 results returned. This is in place to protect the cluster as paging through a large result set is a very memory-intensive operation. **Therefore, please be mindful of this limitation and attempt to revise your search criteria to match a smaller number of documents.**

The API defaults to a "summary" subset of fields (i.e., `title`, `thumbnail`, `spatialBounding`, `beginDate`, `beginYear`, `endDate`, `endYear`, `links`, `serviceLinks`, `citeAsStatements`, and `internalParentIdentifier` for granules and flattened-granules) in the response for each result unless "summary: false" is included in the request body.
``` json
 {
  "page": {
    "max"   : 20,
    "offset": 40
  },
  "summary": false    
}
```

Another use for `page` is to return zero results, and get just the total count matching the query.
``` json
 {
 "queries": [
    {
      "type" : "queryText",
      "value": "temperature"
    }       
  ],
  "page": {
    "max"   : 0,
    "offset": 0
  }    
}
```

## Putting It All Together

An example of combining multiple search criteria:

``` json
 {
  "queries": [
    {
      "type" : "queryText",
      "value": "temperature"
    }       
  ],
  "filters": [
    {
      "type"  : "facet",
      "name"  : "science",
      "values": [
        "Oceans"
      ]
    },
    {
      "type"  : "datetime",
      "before": "2016-06-15T20:20:58Z",
      "after" : "2015-09-22T10:30:06.000Z"
    },
    {
      "type"    : "geometry",
      "relation": "contains",
      "geometry": {
        "type"       : "Point",
        "coordinates": [
          22.123,
          -45.245
        ]
      }
    }       
  ],
  "page": {
    "max"   : 100,
    "offset": 0
  },
  "summary": false    
}
```

## Query Granules
Once a collection has been identified, you can search against granule records using the collection identifier. A list of collection identifiers can be provided here.

Below is an example querying for granule records referencing a collection identifier (use the collection's "id" value here):

``` json
{
  "queries": [
    {
      "type" : "queryText",
      "value": "+hydrographic +surveys"
    }
  ],
  "filters": [
    {
      "type"    : "geometry",
      "geometry": {
        "type"       : "Polygon",
        "coordinates": [
          [
            [
              -82.760009765625,
              -37.03763967977139
            ],
            [
              -82.760009765625,
              48.32703913063476
            ],
            [
              97.591552734375,
              48.32703913063476
            ],
            [
              97.591552734375,
              -37.03763967977139
            ],
            [
              -82.760009765625,
              -37.03763967977139
            ]
          ]
        ]
      }
    },
    {
      "type"  : "datetime",
      "after" : "2000-01-01T00:00:00Z",
      "before": "2018-01-24T00:00:00Z"
    },
    {
      "type"  : "collection",
      "values": [
        "AWHnwuBhd-0nqoJ0qz0O"
      ]
    }
  ],
  "facets": false,
  "page": {
    "max"   : 20,
    "offset": 0
  }
}
```

## GETting a record
The full metadata for a specific record of any of the three types can be requested using a GET request to the `/onestop-search/{type}/{id}` endpoints, where `{type}` is collection, granule, or flattened-granule, and `{id}` is the "id" value returned in the search results. For collections, a total count of granules connected to that collection is returned in the `meta` portion of the response.

Sending a GET request to any of the `/onestop-search/{type}` endpoints, where no ID is specified, returns information about that type as a whole. At present, this information is limited to the total number of documents of the specified type available to search in OneStop.
