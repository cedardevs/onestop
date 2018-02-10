### Overview

This is a Kafka Streams app which listens to a configured input topic, passes the message as a string to the configured script, and puts the oputput on the configured output topic. 

This uses standard out, so whatever the script prints to the console will go to the topic. 

There are several simple example scripts that copy the message from input to output in various languages for proof-of-concept.

There is a dscovr-parser.py script that parses dscovr's filepath and can be used in place of the parser module as it is written now.  

Building images with necessary dependencies makes this a highly flexible component. 

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```
kafka:
  group.id: <group-id>
  bootstrap:
    servers: <kafka_host>:<kafka_port>
  topics:
    input: <input_topic>
    output: <output_topic>

alg:
  absolutePath: <absolute>/<path>/<to>/<script>.py
  lang: <language>
  timeout: <timeout_ms>
```

### Usage
Configure your input, output, the language your script is in, and the absolute path to your script. 

The app simply constructs a command from your configuration. i.e. ``` [alg.lang, alg.absolutePath, msg].execute() ```

Works with a groovy script- 

```
alg:
  absolutePath: ${PWD}/script-wrapper/groovyExample.groovy
  lang: groovy

```

Works with bash- 
```
alg:
  absolutePath: ${PWD}/script-wrapper/bashExample.sh
  lang: bash

```

Works with python - 

```
alg:
  absolutePath: ${PWD}/script-wrapper/pythonExample.py
  lang: bash

```

Works with javascript- 
```
alg:
  absolutePath: ${PWD}/script-wrapper/jsExample.js
  lang: node
  timeout: 5000
```


### Real use case:  dscover-parser.py


```
kafka:
  group.id: dscovr-parser
  bootstrap:
    servers: localhost:9092
  topics:
    input: raw-granule-aggregator-raw-granules-changelog
    output: parsed-granules


alg:
  absolutePath: ${PWD}/script-wrapper/dscovr-parser.py
  lang: python

```
And this message in the input topic- 
```
{
    "dataStream": "dscovr",
    "trackingId": "4d989197-d4a9-4a2b-a579-5eb67b44c3c5",
    "checksum": "fd297fcceb94fdbec5297938c99cc7b5",
    "relativePath": "it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
    "path": "/dscovr/valid/it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
    "fileSize": 6526
}
```

You will get this on the output topic- 
```
{
    "dataStream": "dscovr",
    "trackingId": "4d989197-d4a9-4a2b-a579-5eb67b44c3c5",
    "checksum": "fd297fcceb94fdbec5297938c99cc7b5",
    "relativePath": "it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
    "path": "/dscovr/valid/it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
    "fileSize": 6526,
    "processingEnvironment": "it",
    "dataType": "fc1",
    "satellite": "dscovr",
    "startDate": "20131218000000",
    "endDate": "20131218235951",
    "processDate": "20161215345159",
    "publish": true
}
```

