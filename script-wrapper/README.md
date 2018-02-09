### Overview

This is a Kafka Streams app which listens to a configured stream, passes the message as a string to the configured script, and puts the oputput on the configured topic. 

### Usage

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```yml
kafka:
  bootstrap.servers: "localhost:9092"
```
