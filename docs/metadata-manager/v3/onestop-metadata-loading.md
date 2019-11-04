## Loading Metadata Into PSI
Metadata can be published into the OneStop system in two ways: An HTTP requests with the registry application and by  
connecting an upstream source to the underlying Kafka system with kafka connect or by introducing a kafka producer api  
to the upstream application. 
    
### Registry Overview
See the [user docs about the REST API](../../registry-api.md) for more details.

### Using HTTP
An HTTP upload request can be done to a registry application `/registry/metadata/${type}/${source}/${UUID}` resource  
endpoint using `POST` for creating/replacing, `PUT` for creating/updating and `PATCH` for creating/updating a record.    

Note: The registry is equipped with a RESTful interface that allows full CRUD control of metadata records stored by the system. 

HTTP Method | Endpoint                                       | Body              | Action
------------|------------------------------------------------|-------------------|--------------------------
POST        | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create a metadata record 
PUT         | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 
PATCH       | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 
GET         | /registry/metadata/${type}/${source}/${id}     | (none)            | Retrieve a metadata record 
DELETE      | /registry/metadata/${type}/${source}/${id}     | (none)            | delete uploaded record 
 .          | /metadata/${type}/${source}/${id}/resurrection | (none)            | Retrieve a metadata record 

See the [OpenAPI Specification documentation](https://sciapps.colorado.edu/registry/openapi.yaml) for more details.

Endpoint key words: 
- Types: The type of record it could be either collection or granule. 
- Source: The name of an external system which produced this record, example: comet for collection and common-ingest for granules.   
- Id: a valid universally unique identifier (UUID) which is a 128-bit number that identifies unique record.

#### Uploading xml record: 
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

#### Uploading Json record: 
All metadata json documents could have a full blown Discovery information or  

Input json example: 
```
{
  "trackingId": "11111111-1111-2222-3333-44444444",
  "dataStream": "dscovr",
  "fileInformation": {
    "name": "oe_fc1_dscovr_s20131218000000_e20131218235951_p20161215145159_pub.nc.gz",
    "size": 42,
    "checksums": [
      {
        "algorithm": "MD5",
        "value": "fd297fcceb94fdbec5297938c99cc7b5"
      }
    ],
    "format": "NetCDF",
    "headers": null,
    "optionalAttributes": {
      "test": "very yes"
    }
  },
  "fileLocations": {
    "http://www.google.com": {
      "uri": "http://www.google.com",
      "type": "ACCESS",
      "deleted": false,
      "restricted": false,
      "asynchronous": false,
      "locality": null,
      "lastModified": "1560881307486",
      "serviceType": "Amazon:AWS:S3",
      "optionalAttributes": {
        "not_real": "that_is_correct"
      }
    }
  },
  "relationships": [
    {
      "type": "COLLECTION",
      "id": "11111111-1111-1111-1111-111111111111"
    }
  ],
  "discovery": {
    "fileIdentifier": "22222222-2222-2222-2222-222222222222",
    "parentIdentifier": "11111111-1111-1111-1111-111111111111",
    "links": [
      {
        "linkName": "oe_fc1_dscovr_s20131218000000_e20131218235951_p20161215145159_pub.nc.gz",
        "linkUrl": "http://www.google.com",
        "linkProtocol": "S3",
        "linkFunction": "fileAccess"
      }
    ]
  }
}
```

Example: Uploading a GRANULE type JSON file from a source COMMON-INGEST with trackingId/UUID 11111111-1111-2222-1111-111111111: 
```
curl -iu <username:password> -v -X PUT -H "content-type: application/xml" http://data-dev.ncei.noaa.gov/psi-registry/ 
metadata/granule/common-ingest/11111111-1111-2222-3333-44444444 --data-binary @path/to/json-file.json
```
### Using kafka 

// kafka connect?

// kafka producer api? 
