### Overview

This is a an spring rest and kafka api which takes an http post and publish it to a kafka topic.

### Usage

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```yml
kafka:
  bootstrap.servers: "localhost:9092"
```

#### Simulate a granule POST from CI:

```bash
curl -X PUT -H "Content-Type: application/json" localhost:8080/metadata/granule --data-binary @test_granule.json
```
