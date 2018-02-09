### Overview

This is a Kafka Streams app which listens for incoming raw metadata events and parse it 
### Usage

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```yml
kafka:
  bootstrap.servers: "localhost:9092"
```
