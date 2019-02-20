## Registry Overview

The registry provides a horizontally-scalable API and storage for granule- and collection-level metadata backed by Kafka. 

Incoming metadata can be in either JSON or ISO-19115 XML format.

It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events, merging them with previous events to provide a full picture of the metadata for each granule and collection. 


## The RESTful Interface
Interactions with the registry API are centered around the endpoint: 

`{context-path}/metadata/{required-type}/{optional-source}/{optional-id}`

Where:
* The `context-path` is [explicitly set](/docs/deployment/project-artifacts.md#config) at time of deployment (otherwise `localhost:8080`)
* The `type` is one of the enum values for the [RecordType](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/recordType.avsc) object: 
  * `collection` 
  * `granule`
* The `source` is one of the following:
  * For `collection` types: `comet` or `unknown` (default)
  * For `granule` types: `common-ingest`, `class`, or `unknown`
* The `id` is a UUID value that can be either auto-generated or manually created.

This API abides by [JSON API specifications](https://jsonapi.org/format/). 


### Creating And Replacing Documents
Create and replace documents using `PUT` and `POST` requests. 
* The `type` must be specified
* Omitting the source will result in the source being set as `unknown`
* Omitting the `id` will cause PSI to generate a UUID value as the id to be used.

Successful operations will return a response body with the format:
```json
{
  "id"  : "<idValue>",
  "type": "<typeValue>"
}
```

Unsuccessful operations will return a response body with the format:
```json
{
  "errors": []
}
```

### Retrieving Documents
Retrieve stored documents using `GET` and `HEAD` requests. Requests sent to the above base URL will return the original input metadata. Requests sent to `{baseURL}/parsed` will return the [ParsedRecord](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/parsedRecord.avsc).
* The `type` must be specified
* Omitting the source will result in the source being set as `unknown`
* The `id` must be specified

Found documents will return a response body with the format:
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

If the document isn't found, a response body will be returned with the format:
```json
{
  "links" : {
    "input"     : "<inputUrlValue>",
    "parsed"    : "<parsedUrlValue>",
    "self"      : "<selfReferencingUrlValue>"
  },
  "errors": [
    {
      "status": 404,
      "title": "NOT_FOUND",
      "detail": "No input exists for <typeValue> with id [<idValue>] from source [<sourceValue>]"
    }
  ]
}
```
**NOTE**: If the request is received at the `{baseURL}/parsed` endpoint, the `links` object will contain both `self` and `input` URLs, under the assumption the parsed record may not yet be available. However, a request received at the `{baseURL}` endpoint will only contain the `self` value.


### Updating Existing Documents -- Section WIP
If the original input metadata format is JSON, `PATCH` requests can be used to append or modify subsections of the record. However, XML `PATCH` requests will 
* The `type` must be specified
* Omitting the source will result in the source being set as `unknown`
* The `id` must be specified


### Deleting Documents -- Section WIP
Removing a document requires a `DELETE` request, where the `type` and `id` parameters are required. If `source` is not specified, the record will be assumed as having the default source value ('unknown'). 