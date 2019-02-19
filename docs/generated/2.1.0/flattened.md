# Flattend Granule API

Flattened granules are a representation of the granules which inherit all the metadata from the parent collection.

## Table of Contents

* Paths
  - [`HEAD` /flattened-granule](#op-head-flattened-granule) 
  - [`GET` /flattened-granule](#op-get-flattened-granule) 
  - [`HEAD` /flattened-granule/{id}](#op-head-flattened-granule-id) 
  - [`GET` /flattened-granule/{id}](#op-get-flattened-granule-id) 
  - [`POST` /search/flattened-granule](#op-post-search-flattened-granule) 




## Paths

### <span id="op-head-flattened-granule" > `HEAD` /flattened-granule </span>
Get Flattened Granule Info








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-get-flattened-granule" > `GET` /flattened-granule </span>
Get Flattened Granule Info








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_

###### application/json


<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
      <th>Accepted values</th>
    </tr>
  </thead>
  <tbody>
      <tr>
        <td>data</td>
        <td>
          array(object)
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
      <tr>
        <td>data.type</td>
        <td>
          string
        </td>
        <td></td>
        <td><code>count</code></td>
      </tr>
      <tr>
        <td>data.id</td>
        <td>
          string
        </td>
        <td></td>
        <td><code>collection</code>, <code>granule</code></td>
      </tr>
      <tr>
        <td>data.count</td>
        <td>
          integer
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>


##### Example _(generated)_

```json
{
  "data": [
    {
      "type": "count",
      "id": "collection",
      "count": 0
    }
  ]
}
```

#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-head-flattened-granule-id" > `HEAD` /flattened-granule/{id} </span>
Flattened Granule by ID


#### Path parameters

##### id

UUID of the flattened granule


<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>In</th>
      <th>Description</th>
      <th>Accepted values</th>
    </tr>
  </thead>
  <tbody>
      <tr>
        <td>id  <strong>(required)</strong></td>
        <td>
          string
        </td>
        <td>path</td>
        <td>UUID of the flattened granule</td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_


##### 404 - Request Parsing Error

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-get-flattened-granule-id" > `GET` /flattened-granule/{id} </span>
Flattened Granule by ID


#### Path parameters

##### id

UUID of the flattened granule


<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>In</th>
      <th>Description</th>
      <th>Accepted values</th>
    </tr>
  </thead>
  <tbody>
      <tr>
        <td>id  <strong>(required)</strong></td>
        <td>
          string
        </td>
        <td>path</td>
        <td>UUID of the flattened granule</td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_


##### 404 - Request Parsing Error

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-post-search-flattened-granule" > `POST` /search/flattened-granule </span>
Retrieve flattened granule metadata

Retrieve flattened granule metadata records matching the text query string, spatial, and/or temporal filter.





#### Request body

###### application/json



[Search Request](schema/request.md)


##### Example _(generated)_

```json
{
  "queries": [
    {
      "type": "queryText",
      "value": "string"
    }
  ],
  "filters": [
    {
      "type": "datetime",
      "before": "2019-02-16T04:36:12Z",
      "after": "2019-02-16T04:36:12Z",
      "relation": "intersects"
    }
  ],
  "facets": false,
  "summary": true,
  "page": {
    "max": 10,
    "offset": 0
  }
}
```




#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_

###### application/json


<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Type</th>
      <th>Description</th>
      <th>Accepted values</th>
    </tr>
  </thead>
  <tbody>
      <tr>
        <td>meta</td>
        <td>
          object
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
      <tr>
        <td>meta.took</td>
        <td>
          integer
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
      <tr>
        <td>meta.total</td>
        <td>
          integer
        </td>
        <td>total number of results matching the search</td>
        <td><em>Any</em></td>
      </tr>
      <tr>
        <td>meta.facets</td>
        <td>
          object
        </td>
        <td>included if search request specified facets:true</td>
        <td><em>Any</em></td>
      </tr>
      <tr>
        <td>data</td>
        <td>
          array
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>


##### Example _(generated)_

```json
{
  "meta": {
    "took": 0,
    "total": 0,
    "facets": {}
  },
  "data": [
    {
      "type": "collection",
      "id": "string",
      "attributes": {
        "title": "string",
        "thumbnail": "http://example.com",
        "beginYear": "2019-02-16",
        "endYear": "2019-02-16",
        "spatialBounding": {
          "type": "Point",
          "coordinates": [
            -360,
            -90
          ]
        },
        "links": [
          {
            "linkProtocol": "string",
            "linkFunction": "string",
            "linkDescription": "string",
            "linkUrl": "http://example.com",
            "linkName": "string"
          }
        ]
      }
    }
  ]
}
```

##### 400 - Bad Request

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

