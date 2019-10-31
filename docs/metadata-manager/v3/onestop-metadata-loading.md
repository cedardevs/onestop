## Loading Metadata Into PSI
Metadata can be published into the OneStop system in two ways: An HTTP requests with the registry application and by  
connecting an upstream source to the underlying Kafka system with kafka connect or by introducing a kafka producer api 
to the upstream application. 

The two options are: 

    1. HTTP requests with the registry application.
    2. Connecting an upstream source to the underlying Kafka system.
    
### Registry Overview
See the [user docs about the REST API](../../registry-api.md) for more details.

### Using HTTP
An HTTP upload request can be done to a registry application `/registry/metadata/${type}/${source}/${UUID}` resource  
endpoint using `POST` for creating/replacing, `PUT` for creating/updating and `PATCH` for creating/updating a record.    

Note: The registry is equipped with a RESTful interface that allows full CRUD control of metadata records stored by PSI. 

HTTP Method | Endpoint                                       | Body              | Action
------------|------------------------------------------------|-------------------|--------------------------
POST        | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create a metadata record 
PUT         | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 
PATCH       | /registry/metadata/${type}/${source}/${id}     | ISO-XML or JSON   | create/update a metadata record 
GET         | /registry/metadata/${type}/${source}/${id}     | (none)            | Retrieve a metadata record 
DELETE      | /registry/metadata/${type}/${source}/${id}     | (none)            | delete uploaded record 
            | /metadata/${type}/${source}/${id}/resurrection | (none)            | Retrieve a metadata record 

See the [OpenAPI Specification documentation](https://sciapps.colorado.edu/registry/openapi.yaml) for more details.

NOTE: 
- Types: The type of record it could be either collection or granule. 
- Source: The name of an external system which produced this record, example: comet for collection and common-ingest for granules.   
- Id: a valid universally unique identifier (UUID) which is a 128-bit number that identifies unique record.

#### Uploading xml record: 

#### Uploading Json record: 

### Using kafka 

// kafka connect?
// kafka producer api? 
