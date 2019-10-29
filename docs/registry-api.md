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

Jump to...
* [Create/Replace](#creating-and-replacing-documents)
* [Retrieve](#retrieving-documents)
* [Update](#updating-existing-documents)
* [Delete](#deleting-documents)
* [Resurrect](#resurrecting-deleted-documents) 🧟‍♂️🧟‍♀️

**NOTE:** In addition to these user docs about the API, there is also an [OpenAPI specification](https://github.com/OAI/OpenAPI-Specification)
describing the details of all available endpoints. The specification is hosted by the API itself, at `{context-path}/openapi.yaml`.

---

### Creating And Replacing Documents
Create and replace documents using `PUT` and `POST` requests. 
* The `type` must be specified
* Omitting the source will result in the source being set to `unknown`
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
Retrieve stored documents using `GET` and `HEAD` requests. Requests sent to the above base URL will return the original input metadata in [the Input format](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/input.avsc). Requests sent to `{baseURL}/parsed` will return in the [ParsedRecord format](https://github.com/cedardevs/schemas/blob/master/src/main/resources/avro/psi/parsedRecord.avsc). The returned object is located in the `data.attributes` key of the returned JSON.
* The `type` must be specified
* Omitting the source will result in the source being set to `unknown`
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

If the document isn't found and doesn't exist, a response body will be returned with the format:
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

If the document isn't found but the `id` references a [deleted document](#deleting-documents), the following response body will be returned:
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


### Updating Existing Documents
If the original input metadata format is JSON, `PATCH` requests can be used to modify or add subsections of the record. Currently, `PATCH` requests will fully replace an existing key-value pair or add a new one to the final merged document. JSON lists and objects sent in a `PATCH` request should therefore be the desired _complete_ element. 

XML `PATCH` requests are not supported. 

* The `type` must be specified
* Omitting the source will result in the source being set to `unknown`
* The `id` must be specified

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


### Deleting Documents
Removing a document is possible with a `DELETE` request. This will "tombstone" the record in all downstream topics, which deletes it from any sinks connected to PSI (e.g. OneStop). Since PSI is modeled on the Kappa Architecture paradigm (see our [architectural background page](/docs/design/architectural-background.md) for some more info), the event(s) concerning any given record prior to a `DELETE` are still kept and so it is possible to "undo" a `DELETE` with a [resurrection request](#resurrecting-deleted-documents). But...

**WARNING**: Deleting a record via an intentially empty request body (i.e. `""`) on a `PUT` or `POST` is a non-guaranteed and unclean way to purge a metadata record from downstream sinks that cannot be undone through the Registry API. _Don't do it!_

* The `type` must be specified
* Omitting the source will result in the source being set to `unknown`
* The `id` must be specified

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


### Resurrecting Deleted Documents
A document that has been `DELETE`d can be resurrected with a `GET` request to `{baseUrl}/resurrection`. 

* The `type` must be specified
* Omitting the source will result in the source being set to `unknown`
* The `id` must be specified

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

**NOTE**: This functionality is ONLY available if a record was removed via a `DELETE` request. 
