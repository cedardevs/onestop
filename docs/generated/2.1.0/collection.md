# Collection API



## Table of Contents

* Paths
  - [`HEAD` /collection](#op-head-collection) 
  - [`GET` /collection](#op-get-collection) 
  - [`HEAD` /collection/{id}](#op-head-collection-id) 
  - [`GET` /collection/{id}](#op-get-collection-id) 
  - [`POST` /search/collection](#op-post-search-collection) 




## Paths

### <span id="op-head-collection" > `HEAD` /collection </span>
Get Collection Info








#### Responses



##### 200 - Successful operation

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-get-collection" > `GET` /collection </span>
Get Collection Info








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

### <span id="op-head-collection-id" > `HEAD` /collection/{id} </span>
Collection by ID


#### Path parameters

##### id

UUID of the collection


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
        <td>UUID of the collection</td>
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

### <span id="op-get-collection-id" > `GET` /collection/{id} </span>
Collection by ID


#### Path parameters

##### id

UUID of the collection


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
        <td>UUID of the collection</td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>








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
        <td>meta.totalGranules</td>
        <td>
          integer
        </td>
        <td></td>
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
    "totalGranules": 0
  },
  "data": [
    {
      "type": "collection",
      "id": "string"
    }
  ]
}
```

##### 404 - Request Parsing Error

###### Headers
_No headers specified_


#### Tags

<div class="tags">
  <div class="tags__tag"></div>
</div>

### <span id="op-post-search-collection" > `POST` /search/collection </span>
Retrieve collection metadata

Retrieve collection metadata records matching the text query string, spatial, and/or temporal filter.





#### Request body

###### application/json



[Search Request](schema/request.md)


##### Example

> basic text query



```json
{
  "queries": [
    {
      "type": "queryText",
      "value": "climate"
    }
  ],
  "filters": [],
  "facets": true,
  "page": {
    "max": 20,
    "offset": 0
  }
}
```

##### Example

> multiple science theme filters selected



```json
{
  "queries": [
    {
      "type": "queryText",
      "value": "weather"
    }
  ],
  "filters": [
    {
      "type": "facet",
      "name": "science",
      "values": [
        "Agriculture",
        "Atmosphere > Atmospheric Radiation > Incoming Solar Radiation"
      ]
    }
  ],
  "facets": true,
  "page": {
    "max": 20,
    "offset": 0
  }
}
```

##### Example

> multiple facets selected



```json
{
  "queries": [
    {
      "type": "queryText",
      "value": "weather"
    }
  ],
  "filters": [
    {
      "type": "facet",
      "name": "science",
      "values": [
        "Agriculture"
      ]
    },
    {
      "type": "facet",
      "name": "instruments",
      "values": [
        "ADCP > Acoustic Doppler Current Profiler"
      ]
    }
  ],
  "facets": true,
  "page": {
    "max": 20,
    "offset": 0
  }
}
```

##### Example

> Next Page request



```json
{
  "queries": [
    {
      "type": "queryText",
      "value": "weather"
    }
  ],
  "filters": [],
  "facets": false,
  "page": {
    "max": 20,
    "offset": 20
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

## Schemas


#### fullCollectionResponse

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
        <td>meta.totalGranules</td>
        <td>
          integer
        </td>
        <td></td>
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
    "totalGranules": 0
  },
  "data": [
    {
      "type": "collection",
      "id": "string"
    }
  ]
}
```

#### collectionMetadataObject

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
        <td>totalGranules</td>
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
  "totalGranules": 0
}
```

#### collectionDataObject

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
        <td>type</td>
        <td>
          string
        </td>
        <td></td>
        <td><code>collection</code></td>
      </tr>
      <tr>
        <td>id</td>
        <td>
          string
        </td>
        <td></td>
        <td><em>Any</em></td>
      </tr>
  </tbody>
</table>

##### Example _(generated)_

```json
{
  "type": "collection",
  "id": "string"
}
```
