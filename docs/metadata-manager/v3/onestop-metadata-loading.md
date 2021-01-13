<div align="center"><a href="/onestop/metadata-manager">Metadata Manager Documentation Home</a></div>
<hr>

**Estimated Reading Time: 15 minutes**

## Loading Metadata Into Inventory Manager
Metadata can be published into the OneStop Inventory Manager system using the Registry application REST API. Use the Registry API `/registry/metadata/${type}/${source}/${UUID}` resource endpoint to upload metadata records. The application is equipped with a RESTful interface that allows full CRUD control of metadata records stored by the system.   
NOTE: The REST API is also secured via CAS authentication. For more detail see [OneStop Registry Security documentation](../../operator/security/registry-security.md). 
    
### Available Methods
The Registry application has various endpoints that are also described in the [user docs about the REST API](../../api/registry-api.md) and
[OpenAPI Specification documentation](https://sciapps.colorado.edu/registry/openapi.yaml) in detail.

#### URL Parameter Notes: 
- Types: The type of record. This is currently either `collection` or `granule`. **This URL parameter is REQUIRED.**
- Source: The name of an external system which produced this record, example: `comet` for collection and `common-ingest` for granules. This can be omitted when POSTing and is set to `unknown` in this case.
- Id: A valid universally unique identifier (UUID) which is a 128-bit number that identifies a unique record. Not including this value with POSTing will result in an automatically generated UUID for the received record. 

#### HTTP Methods:

- Create a new Collection/Granule record 

HTTP Method | Endpoint                                           | Body              | Function
------------|----------------------------------------------------|-------------------|--------------------------
POST        | /registry/metadata/${type}/${source}/${id}         | ISO-XML or JSON   | create/replace a metadata record 

- Create or replace a Collection/Granule record 

HTTP Method | Endpoint                                           | Body              | Function
------------|----------------------------------------------------|-------------------|--------------------------
PUT         | /registry/metadata/${type}/${source}/${id}         | ISO-XML or JSON   | create/replace a metadata record 

- Update a subset of a Collection/Granule record
 
HTTP Method | Endpoint                                           | Body              | Function
------------|----------------------------------------------------|-------------------|--------------------------
PATCH       | /registry/metadata/${type}/${source}/${id}?${op}   | JSON              | update a portion of a metadata record 

- Read a Collection/Granule record 

HTTP Method | Endpoint                                           | Body              | Function
------------|----------------------------------------------------|-------------------|--------------------------
GET         | /registry/metadata/${type}/${source}/${id}         | (none)            | Retrieve a metadata record 

- Delete a Collection/Granule record 

HTTP Method | Endpoint                                           | Body              | Function
------------|----------------------------------------------------|-------------------|--------------------------
DELETE      | /registry/metadata/${type}/${source}/${id}         | (none)            | delete uploaded record 

- Resurrect a deleted Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
GET         | /metadata/${type}/${source}/${id}/resurrection | (none)            | resurrect a metadata record 
 

#### Uploading an XML Collection record: 
Example: Uploading a COLLECTION type XML file from a source COMET with uuid 11111111-1111-2222-3333-44444444: 
```
curl -iu <username:password> -v -X PUT -H "content-type: application/xml" http://data-dev.ncei.noaa.gov/psi-registry/ 
metadata/collection/comet/11111111-1111-2222-3333-44444444 --data-binary @path/to/the/xml-file.xml
```
Successful operations will return a response body:
```json
{
  "id"  : "11111111-1111-2222-3333-44444444",
  "type": "collection"
}
```

Unsuccessful operations will return a response body with an error message formatted as:
```json
{
  "errors": []
}
```

#### Uploading an XML Granule record: 
When uploading granules in XML format, it is imperative to follow-up with a **PATCH** request containing the JSON `Relationships` indicating the associated collection UUID so that the granule is correctly linked to its collection downstream in the OneStop Search API/UI:
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

#### Uploading a JSON record: 
All metadata input in JSON format should contain `FileLocation`. If the input metadata is a granule, `Relationships` are required to indicate the UUID of the associated collection. Optionally, to ensure optimal discoverabilty and access in the OneStop Search API and UI, `FileInformation` and `Discovery` should be included as well.   

**NOTE**: When submitting content to OneStop Inventory Manager via the Registry REST API, the request body can contain any or all of the following content (links direct to the associated Avro schemas describing the accepted content):

1. [FileLocation](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/fileLocation.avsc): A map of URIs to location objects describing where the file is located
1. [FileInformation](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/fileInformation.avsc): Details about the file that this input object is in reference to
1. [Relationships](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/relationship.avsc): A record of this objects relationships to other objects in the inventory
1. [Publishing](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/publishing.avsc): Information pertaining to whether a file is private and for how long if so
1. [Discovery](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/discovery.avsc): Metadata about the file contents that is meant for discoverability/search/access

The complete set of Avro schemas used by OneStop Inventory Manager can be found in the [schemas-core module of the Schemas repository](https://github.com/cedardevs/schemas/tree/master/schemas-core/src/main/resources/avro). Many fields in the above schemas reference other schemas contained within this repository.

**When loading metadata via writing directly to the underlying Kafka topic, content must follow the [Input schema](https://github.com/cedardevs/schemas/blob/master/schemas-core/src/main/resources/avro/psi/input.avsc), where the aforementioned elements are contained in the `content` field as a string. This is true for both JSON and XML content submitted.**

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

#### OneStop Required Fields
For a record to be indexed and searchable by the downstream OneStop Search API/UI, the following Discovery fields must be populated: 

Required Collection fields:
- fileIdentifier
- title
    
Required Granule fields:
- fileIdentifier
- parentIdentifier
- hierarchyLevelName (must be case-insensitive 'granule')
- title

In the event that only `FileInformation` is received, the default parser may be able to extract rudimentary content for the `fileIdentifier` and `title` fields from filename information. Hierarchy level name will be auto-populated in this case. However, the granule must still have the corresponding collection identified in the `Relationships` JSON block. 
    
<hr>
<div align="center"><a href="#">Top of Page</a></div>
