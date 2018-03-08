### Overview

The registry provides a horizontally-scalable API and storage for granule- and collection-level metadata backed by Kafka. 

It publishes metadata updates to Kafka, then uses a Kafka Streams app to aggregate those raw metadata events,
merging them with previous events to provide a full picture of the metadata for each granule and collection. 

### Usage

#### Config:

Config can be provided to the app at runtime using any of the [Spring Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
techniques.

See the embedded [application.yml](src/main/resources/application.yml) for the full set of default config values you can override. 

#### Example requests

*Simulate a granule POST from CI:*

```bash
curl -X PUT -H "Content-Type: application/json" localhost:8080/metadata/granule --data-binary @src/test/resources/test_granule.json
```

*Get the saved metadata for the granule:*

```bash
curl localhost:8080/metadata/granule/4d989197-d4a9-4a2b-a579-5eb67b44c3c5
```

*Simulate a collection POST from CoMET:*

```bash
curl -X PUT -H "Content-Type: application/xml" localhost:8080/metadata/collection/123 --data-binary @src/test/resources/dscovr_fc1.xml
```

*Get the saved metadata for the collection:*

```bash
curl localhost:8080/metadata/collection/123
```
