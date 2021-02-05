<div align="center"><a href="/onestop/api">OneStop API</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

# Search API Responses

## Table of Contents
* [Response Format](#response-format)

## Response Format
OneStop API responses are built around the [JSON API Specification](http://jsonapi.org/). Successful responses (with facets requested) are formatted as such, where the fields within the `attributes` object are specified [here](search-fields):
``` json
 {  
 "data": [
     {
       "id"        : "1",
       "type"      : "collection",
       "attributes": { ... }
     },
     {
       "id"        : "2",
       "type"      : "collection",
       "attributes": { ... }
     }
   ],
  "meta": {
    "took"  : 100,
    "total" : 2,
    "facets": {
      "science"             : [
        {
          "Oceans": {
            "count": 2
          }
        },
        {
          "Oceans > Bathymetry": {
            "count": 1
          }
        }
      ],
      "services"            : [],
      "locations"           : [],
      "instruments"         : [],
      "platforms"           : [],
      "projects"            : [],
      "dataCenters"         : [],
      "horizontalResolution": [],
      "verticalResolution"  : [],
      "temporalResolution"  : []
    }
  }
}
```

<hr>
<div align="center"><a href="#">Top of Page</a></div>