<div align="center"><a href="/onestop/api">OneStop API</a></div>
<hr>

**Estimated Reading Time: 20 minutes**

# Registry Overview

## Table of Contents
* [Intro](#intro)
* [SwaggerHub Generated Documentation](#swaggerhub-generated-documentation)
* [Registry OneStop Endpoint](#registry-onestop-endpoint)
* [Metadata Notes](#metadata-notes)
    * [JSON Records](#json-records)
* [Creating And Replacing a Record](#creating-and-replacing-a-record)
* [Read a Collection/Granule Record](#read-a-collectiongranule-record)
* [Updating an Existing Record](#updating-an-existing-record)
* [Deleting a Record](#deleting-a-record)
* [Resurrecting a Deleted Record](#resurrecting-a-deleted-record)
* [External Resources](#external-resources)

## Intro

The registry provides a horizontally-scalable API and storage for granule and collection-level metadata backed by Kafka. It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events, merging them with previous events to provide a full picture of the metadata for each granule and collection. 

## SwaggerHub Generated Documentation
Our OpenAPI documents are available on SwaggerHub. This should list supported endpoints and parameters necessary.

* [3.0.0-RC1](https://app.swaggerhub.com/apis/cedarbot/OneStop-Search/3.0.0-RC1)

## Registry OneStop Endpoint
Aside from the OpenApi documents listed above there are also the default supported [HTTP Methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods)

**NOTE:** If you ever get a 401 Authorization Required add this to your curl and file in the username and password with valid credentials. 

`-u '<username>:<password>'`

The Registry API endpoint which you would append to the end of a OneStop deployment:

* Old endpoint:
`{context-path}/metadata/{type}/{source}/{id}`

* New endpoint:
`{context-path}/api/registry/metadata/{type}/{source}/{id}`

Where `context-path` is [explicitly set](/onestop/operator/deployment/v2/psi/project-artifacts#config) at time of deployment (otherwise `localhost:8080`)


## Metadata Notes
For granule metadata you need to include the ***`relationships`*** field, which contains the collection UUID as OneStop knows it:

```
{
  "relationships": [
    {
      "type": "COLLECTION",
      "id": <collection-uuid>
    }
  ]
}
```
You can easily include this within Json, but it is impossible to include this within an XML document and must added via a [PATCH](#updating-an-existing-record) HTTP request after the initial metadata upload.

**Note:** The use of backslashes in the curl examples below is simply to allow for carriage returns. This is because this example display doesn't do line wrapping.

### JSON Records

When submitting a JSON record the request body can contain any or all of the following content (links direct to the associated Avro schemas describing the accepted content):

1. [FileLocation](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/fileLocation.avsc): A map of URIs to location objects describing where the file is located
1. [FileInformation](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/fileInformation.avsc): Details about the file that this input object is in reference to
1. [Relationships](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/relationship.avsc): A record of this objects relationships to other objects in the inventory
1. [Publishing](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/publishing.avsc): Information pertaining to whether a file is private and for how long if so
1. [Discovery](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/discovery.avsc): Metadata about the file contents that is meant for discoverability/search/access

The complete set of Avro schemas used by OneStop Inventory Manager can be found in the [schemas-core module of the Schemas repository](https://github.com/cedardevs/schemas/tree/master/schemas-core/src/main/resources/avro). Many fields in the above schemas reference other schemas contained within this repository.


Example Input JSON: 
```
{
  "fileInformation": {
    "name": "The file name",
    "size": The size of the file in bytes,
    "checksums": [
      #A list of checksums for the file
      {
        "algorithm": "checksum algorithm",
        "value": "checksum value"
      }
    ],
    "format": "Optional field to indicate the format of the file",
    "headers": "Optional field to capture a file's headers",
    "optionalAttributes": {
      #A discretionary map of key/value pairs to capture arbitrary attributes
      "EXTRA_ATTRIBUTES": "EXTRA_ATTRIBUTES"
    }
  },
  "fileLocations": {
    "A Uniform Resource Identifier as defined by RFCs 2396 and 3986": {
      "uri": "A Uniform Resource Identifier as defined by RFCs 2396 and 3986",
      "type": "The type of the file location, e.g. an ingest location, access location, etc",
      "deleted": false,
      "restricted": Is access to this location restricted from the public? true/false,
      "asynchronous": Indicates if access to this location is asynchronous, true/false,
      "locality": A string indicating the locality of the data, e.g. a FISMA boundary, an AWS Region, an archive service, etc. or null,
      "lastModified": "Datetime when the location created/last modified, in milliseconds from the unix epoch",
      "serviceType": "The type of service this location belongs to, e.g. Amazon:AWS:S3",
      "optionalAttributes": {
        #key/value pairs to capture extra attributes 
        "EXTRA_ATTRIBUTES": "EXTRA_ATTRIBUTES"
      }
    }
  },
  "relationships": [
    {
      "type": "Relationship type: only COLLECTION for now ",
      "id": "Collection id it belongs to"
    }
  ],
  "discovery": {
    "fileIdentifier": "",
    "parentIdentifier": "",
    "hierarchyLevelName": "",
    "doi": "",
    "status": "onGoing",
    "title": "title, a short description",
    "alternateTitle": "alternate title",
    "description": "description of the metadata",
    "keywords": [],
    "responsibleParties": [],
    "thumbnail": "https://www1.ncdc.noaa.gov/pub/data/metadata/images/C00811_SR_lowRes.png",
    "thumbnailDescription": "Global image of daily AVHRR surface reflectance",
    "creationDate": null,
    "revisionDate": null,
    "publicationDate": "2014-05-21",
    "citeAsStatements":[],
    "crossReferences": [],
    "accessFeeStatement": null,
    "orderingInstructions": null,
    "edition": "Version 4",
    "dsmmAccessibility": 2,
    "dsmmDataIntegrity": 3,
    "services": [ ]
    ...
  }
}
```

## Creating And Replacing a Record

Create a record using a `POST`, or create or replace records using a `PUT`, via the [endpoint](#registry-onestop-endpoint) specified above. 

### Example

Where:
* `type`=collection

```bash
curl -X PUT \
     -H "Content-Type: application/xml" \
     -u '<username>:<password>' \
     https://cedardevs.org/onestop/api/registry/metadata/collection \
     --data-binary @path/to/the/xml-file.xml
```

### Successful response body with the format:
```json
{
  "id"  : "<idValue>",
  "type": "<typeValue>"
}
```

### Unsuccessful response body with the format:
```json
{
  "errors": []
}
```

## Read a Collection/Granule Record 

Retrieve a stored record using `GET` and `HEAD` requests via the [endpoint](#registry-onestop-endpoint) specified above. Requests sent will return the original input metadata in [the Input format](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/input.avsc). Requests sent to `{baseURL}/parsed` will return in the [ParsedRecord format](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/parsedRecord.avsc). The returned object is located in the `data.attributes` key of the returned JSON.

### Example

Where:
* `type`=collection
* `id`=73d16fe3-7ccb-4918-b77f-30e343cdd378

```bash 
curl  \
    -u '<username>:<password>' \
    https://cedardevs.org/onestop/api/registry/metadata/collection/73d16fe3-7ccb-4918-b77f-30e343cdd378
```

### Found records will return a response body with the format:
```json
{
  "links" : {
    "input"     : "<inputUrlValue>",
    "parsed"    : "<parsedUrlValue>",
    "self"      : "<selfReferencingUrlValue>"
  },
  "data" : {
    "id"        : "<idValue>",
    "type"      : "<typeValue>",
    "attributes": "<resultObject>"
  }
}
```
**NOTE**: The `links` object will contain either the `input` or `parsed` URL, but not both. The `self` URL will refer to the endpoint at which the request was received, and that URL is the one that will not be present.

### If the record isn't found and doesn't exist, a response body will be returned with the format:
```json
{
  "links" : {
    "input"   : "<inputUrlValue>",
    "parsed"  : "<parsedUrlValue>",
    "self"    : "<selfReferencingUrlValue>"
  },
  "errors": [
    {
      "status": 404,
      "title" : "NOT_FOUND",
      "detail": "No input exists for <typeValue> with id [<idValue>] from source [<sourceValue>]"
    }
  ]
}
```
**NOTE**: If the request is received at the `{baseURL}/parsed` endpoint, the `links` object will contain both `self` and `input` URLs, under the assumption the parsed record may not yet be available. However, a request received at the `{baseURL}` endpoint will only contain the `self` value.

### If the record isn't found but the `id` references a [deleted record](#deleting-a-record), the following response body will be returned:
```json
{
  "links" : {
    "resurrection": "<resurrectionUrlValue>",
    "self"        : "<selfReferencingUrlValue>"
  },
  "errors": [
    {
      "status"    : 404,
      "title"     : "NOT_FOUND",
      "detail"    : "DELETE processed for <typeValue> with id [<idValue>] from source [<sourceValue>]"
    }
  ]
}
```

## Updating an Existing Record
If the original input metadata format is JSON, `PATCH` requests via the [endpoint](#registry-onestop-endpoint) specified above can be used to modify or add subsections to a record. Currently, `PATCH` requests will fully replace an existing key-value pair or add a new one to the final merged record. JSON lists and objects sent in a `PATCH` request should therefore be the desired _complete_ element.

A patch will need to be performed if you upload a granule metadata without the relationships field. See [Metadata Notes](#metadata-notes) section. 

XML `PATCH` requests are not supported. 

### Example

Where:
* `type`=collection
* `id`=5690da06-2db2-4291-a879-c7e37662dc81
* `collection-uuid`=73d16fe3-7ccb-4918-b77f-30e343cdd378

```bash
curl -X PATCH \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -u '<username>:<password>' \
     https://cedardevs.org/onestop/api/registry/metadata/granule/5690da06-2db2-4291-a879-c7e37662dc81 \
     -d "{ \"relationships\": [{\"type\": \"COLLECTION\", \"id\": 73d16fe3-7ccb-4918-b77f-30e343cdd378}]}"
```
### Successful operations will return a response body with the format:
```json
{
  "id"  : "<idValue>",
  "type": "<typeValue>"
}
```

### Unsuccessful operations will return a response body with the format:
```json
{
  "errors": []
}
```

## Deleting a Record
Removing a record is possible with a `DELETE` request via the [endpoint](#registry-onestop-endpoint) specified above. This will "tombstone" the record in all downstream topics, which deletes it from any sinks connected to PSI (e.g. OneStop). Since Registry is modeled on the Kappa Architecture paradigm (see our [architectural background page](/onestop/api/architectural-overview) for some more info), the event(s) concerning any given record prior to a `DELETE` are still kept and so it is possible to "undo" a `DELETE` with a [resurrection request](#resurrecting-a-deleted-record). But...

**WARNING**: Deleting a record via an intentionally empty request body (i.e. `""`) on a `PUT` or `POST` is a non-guaranteed and unclean way to purge a metadata record from downstream sinks that cannot be undone through the Registry API. _Don't do it!_

### Example
Where:

* `type`=granule
* `id`=5690da06-2db2-4291-a879-c7e37662dc81

```bash
curl -X DELETE \
    -u '<username>:<password>' \
    https://cedardevs.org/onestop/api/registry/metadata/granule/5690da06-2db2-4291-a879-c7e37662dc81
```

### Successful operations will return a response body with the format:
```json
{
  "id"  : "<idValue>",
  "type": "<typeValue>"
}
```

### Unsuccessful operations will return a response body with the format:
```json
{
  "errors": []
}
```

## Resurrecting a Deleted Record
A record which has been `DELETE`d can be resurrected with a `GET` request to `{baseUrl}/resurrection`. 

### Example
Where:

* `type`=granule
* `id`=5690da06-2db2-4291-a879-c7e37662dc81

```bash
curl \
    -u '<username>:<password>' \
    https://cedardevs.org/onestop/api/registry/metadata/granule/5690da06-2db2-4291-a879-c7e37662dc81/resurrection
```

### Successful operations will return a response body with the format:
```json
{
  "id"  : "<idValue>",
  "type": "<typeValue>"
}
```

### Unsuccessful operations will return a response body with the format:
```json
{
  "errors": []
}
```

**NOTE**: This functionality is ONLY available if a record was removed via a `DELETE` request. 

## External Resources
* [Swagger API Docs - 3.0.0-RC1](https://app.swaggerhub.com/apis/cedarbot/OneStop-Search/3.0.0-RC1)
* [OpenAPI documentation](https://cedardevs.org/onestop/api/registry/openapi.yaml) - Details on the OneStop Registry endpoints and parameters. This is the same as the SwaggerHub docs just formatted differently and live, not a snapshot of a release version.
* [Additional HTTP Endpoints](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods)
* [JSON API specifications](https://jsonapi.org/format/) - Information on JSON responses.

<hr>
<div align="center"><a href="#">Top of Page</a></div>