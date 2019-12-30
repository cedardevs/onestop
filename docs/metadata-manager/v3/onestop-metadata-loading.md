## Loading Metadata Into PSI
Metadata can be published into the OneStop system using Registry application REST API. Use the Registry API `/registry/metadata/${type}/${source}/${UUID}` resource endpoint to upload Metadata records. The application is also 
equipped with a RESTful interface that allows full CRUD control of metadata records stored by the system.   
NOTE: The REST API is also secured via CAS authentication, for more detail see [OneStop Registry Security documentation](../../operator/security/registry-security.md). 
    
### Available methods
Registry resource has various endpoints that are also described in the [user docs about the REST API](../registry-api.md) and
[OpenAPI Specification documentation](https://sciapps.colorado.edu/registry/openapi.yaml) in detail.

- Create a new Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
POST        | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create a metadata record 

- Create or update a new Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
PUT         | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 

- Create or edit a new Collection/Granule record
 
HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
PATCH       | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 

- Read a Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
GET         | /registry/metadata/${type}/${source}/${id}     | (none)            | Retrieve a metadata record 

- Delete a Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
DELETE      | /registry/metadata/${type}/${source}/${id}     | (none)            | delete uploaded record 

- resurrect a deleted Collection/Granule record 

HTTP Method | Endpoint                                       | Body              | Function
------------|------------------------------------------------|-------------------|--------------------------
GET         | /metadata/${type}/${source}/${id}/resurrection | (none)            | resurrect a metadata record 
 
#### Endpoint key words: 
- Types: The type of record it could be either collection or granule. 
- Source: The name of an external system which produced this record, example: comet for collection and common-ingest for granules.   
- Id: A valid universally unique identifier (UUID) which is a 128-bit number that identifies unique record.

#### Uploading an xml Collection record: 
All metadata xml documents can have a `<gmd:fileIdentifier>` tag containing either a
`<gco:CharacterString>` or a `<gmx:Anchor>` tag with the identifier. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  ...
</gmi:MI_Metadata>  
```

Optionally, the record can also have a `<gmd:parentIdentifier>` tag (also containing
either a `<gco:CharacterString>` or a `<gmx:Anchor>` tag) to indicate that the record is
a child of another. In this case, the `parentIdentifier` of the child record must match
the `fileIdentifier` OR the `doi` of the parent record verbatim. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<gmi:MI_Metadata xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi">
  ...
  <gmd:fileIdentifier>
    <gco:CharacterString>[CHILD'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:fileIdentifier>
  <gmd:parentIdentifier>
    <gco:CharacterString>[PARENT'S FILE IDENTIFIER]</gco:CharacterString>
  </gmd:parentIdentifier>
  ...
</gmi:MI_Metadata>  
```

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

#### Uploading Json record: 
All metadata input in json format should contain `FileLocation`, `relationships (if the input metadata is granule)`,   
`FileInformation (optinal)` and `discovery (optinal)` information in order to be searchable and discoverable by the OneStop client: 

1. FileLocation information: A map of URIs to location objects describing where the file is located
1. FileInformation information: Details about the file that this input object is in reference to
1. relationships information: A record of this objects relationships to other objects in the inventory
1. publishing information: Information pertaining to whether a file is private and for how long if so
1. discovery information: A key/value metadata information.


Input json Template: 
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
Note: Refer to supported discovery metadata [fields](https://sciapps.colorado.edu/registry/openapi.yaml).

Example: Uploading a GRANULE type JSON file from a source COMMON-INGEST with trackingId/UUID 11111111-1111-1111-1111-111111111: 
```
curl -iu <username:password> -X PUT -H "Content-Type: application/json" http://localhost/registry/metadata/granule/
common-ingest/11111111-1111-1111-1111-111111111 --data-binary @/path/to/test-granule.json
```
GRANULE Input json example: test-granule.json
```
{
  "fileInformation": {
    "name": "Demo granule file one (NOTE: this is not a REAL GRANULE file)",
    "size": 42,
    "checksums": [
      {
        "algorithm": "MD5",
        "value": "fd297fcceb94fdbec5297938c99cc7f6"
      }
    ],
    "format": "",
    "headers": null,
    "optionalAttributes": {
      "test": "very yes"
    }
  },
  "fileLocations": {
      "https://not-real-access.s3.us-west-1.amazonaws.com/688e0838-991f-47f9-a76a-da044e068863": {
        "uri": "https://not-real-access.s3.us-west-1.amazonaws.com/688e0838-991f-47f9-a76a-da044e068863",
        "type": "ACCESS",
        "deleted": false,
        "restricted": false,
        "asynchronous": false,
        "locality": null,
        "lastModified": null,
        "serviceType": {
          "string": "cloud"
        },
        "optionalAttributes": {}
      }
    },
  "relationships": [
    {
      "type": "COLLECTION",
      "id": "22222222-1111-1111-1111-22222222"
    }
  ],
  "discovery": {
    "fileIdentifier": "gov.noaa.ncdc:C00811",
    "parentIdentifier": null,
    "hierarchyLevelName": null,
    "doi": "doi:10.7289/V5TM782M",
    "status": "onGoing",
    "credit": null,
    "title": "NOAA Climate Data Record (CDR) of AVHRR Surface Reflectance, Version 4",
    "alternateTitle": "AVHRR Surface Reflectance",
    "dsmmProductionSustainability": 4,
    "dsmmTransparencyTraceability": 3,
    "dsmmUsability": 4,
    "dsmmAverage": 3.2222223,
    "updateFrequency": "daily",
    "services": [ ]
  }
``` 
Successful operations will return a response body:
```json
{
  "id"  : "11111111-1111-1111-1111-111111111",
  "type": "granule"
}
```

Unsuccessful operations will return a response body with an error message formatted as:
```json
{
  "errors": []
}
```

#### OneStop Required fields
For a records to be indexed and searchable by the downstream OneStop client, a record must include the follow discovery fields: 

Required Collection fields:
- fileIdentifier
- title
    
Required Granule fields:
- fileIdentifier
- parentIdentifier
- hierarchyLevelName
- title
    
<hr>
<div align="center"><a href="/onestop/metadata-manager/#Metadata-Manager-Navigation-Guide">Previous</a> | <a href="#loading-metadata-into-psi">Top of Page</a> | <a href="upstream-kafka-connect">Next</a></div>

