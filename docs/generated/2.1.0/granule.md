# Granule API



## Table of Contents

* Paths
  - [`HEAD` /granule](#op-head-granule) 
  - [`GET` /granule](#op-get-granule) 
  - [`HEAD` /granule/{id}](#op-head-granule-id) 
  - [`GET` /granule/{id}](#op-get-granule-id) 
  - [`POST` /search/granule](#op-post-search-granule) 




## Paths

### <span id="op-head-granule" > `HEAD` /granule </span>
Get Granule Info








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-get-granule" > `GET` /granule </span>
Get Granule Info








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

### <span id="op-head-granule-id" > `HEAD` /granule/{id} </span>
Granule by ID


#### Path parameters

##### id

UUID of the granule


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
        <td>UUID of the granule</td>
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

### <span id="op-get-granule-id" > `GET` /granule/{id} </span>
Granule by ID


#### Path parameters

##### id

UUID of the granule


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
        <td>UUID of the granule</td>
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

### <span id="op-post-search-granule" > `POST` /search/granule </span>
Retrieve granule metadata

Retrieve granule metadata records matching the text query string, spatial, and/or temporal filter.





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
      "before": "2019-02-16T04:36:10Z",
      "after": "2019-02-16T04:36:10Z",
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

