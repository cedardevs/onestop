### Overview

This is a Kafka Streams app which listens for incoming raw metadata events, merges them together with previous events
for the same key, and exposes a KTable of the current metadata by key.

### Usage

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```yml
kafka:
  bootstrap.servers: "localhost:9092"
```
