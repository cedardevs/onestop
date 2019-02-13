### Overview

The registry provides a horizontally-scalable API and storage for granule- and collection-level metadata backed by Kafka. 

It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events,
merging them with previous events to provide a full picture of the metadata for each granule and collection. 

### Usage

#### Config:

Config can be provided to the app at runtime using any of the [Spring Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
techniques.

See the embedded [application.yml](src/main/resources/application.yml) for the full set of default config values you can override. 
```yaml
server.port: ####
management.endpoints.enabled-by-default: false
kafka:
  bootstrap:
    servers: localhost:####
logging:
  level:
    root: ####
    org.springframework.web: ####

```
#### Example requests

*Simulate a granule POST from CI:*

```bash
curl -X PUT -H "Content-Type: application/json" localhost:8080/metadata/granule --data-binary @src/test/resources/test_granule.json
```

sample test_granule.json
```json
{
 "dataStream": "dscover",
  "trackingId": "3", 
  "checksum": "1234", 
  "relativePath": "test.nc.gz", 
  "path": "/path/to/test.nc.gz", 
  "fileSize": 6526, 
  "lastUpdated":"2017124",
}
```

*Get the saved metadata for the granule:*

```bash
curl localhost:8080/registry/metadata/granule/1234
```

*Simulate a collection POST from CoMET:*

```bash
curl -X PUT -H "Content-Type: application/xml" localhost:8080/metadata/collection/123 --data-binary @src/test/resources/dscovr_fc1.xml
```

*Get the saved metadata for the collection:*

```bash
curl localhost:8080/metadata/collection/123
```
