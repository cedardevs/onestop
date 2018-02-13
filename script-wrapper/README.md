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

stream:
  topics:
      input: <input_topic>
      output: <output_topic>
  command: <run a bash command or execute a script>
  command_timeout: <optional_timeout_ms>
```

### Usage
Configure your input topic, output topic, and the command as you would execute it from the command line. The command must accept a string (of json, for now).  


#### Call an external script - 

python example -

```
stream:
  command: python ${PWD}/script-wrapper/pythonExample.py
```

javascript example w/ optional timeout- 
```
stream:
  command: node ${PWD}/script-wrapper/jsExample.js
  command_timeout: 5000
```

#### Or just run a shell command -

*not quite working yet... maybe someone else can give me a good example*
```
stream:
  command: sed 's/ship/platform/g' $1
``

### Real use case:  dscover-parser.py


```
kafka:
  group.id: dscovr-parser
  bootstrap:
    servers: localhost:9092
  topics:
    input: raw-granule-aggregator-raw-granules-changelog
    output: parsed-granules


stream:
  topics:
    input: raw-granule-aggregator-raw-granules-changelog
    output: parsed-granules
  command: python ${PWD}/script-wrapper/dscovr-parser.py
  command_timeout: 5000

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

