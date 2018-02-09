### Overview

This is a Kafka Streams app which listens to a configured stream, passes the message as a string to the configured script, and puts the oputput on the configured topic. 


The example.py script does exactly the same thing that dscovr parser currently does. 


### Usage
Given this config - 
```
server.port: 4444

kafka:
  bootstrap:
    servers: localhost:9092
  topics:
    input: <input_topic>
    output: <output_topic>


alg:
  absolutePath: ${PWD}/script-wrapper/exampleScript.py
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

#### Required config:

You must provide Spring configuration values for the app to connect to Kafka. e.g. Put an `application.yml` file in this
directory with the following:

```
server.port: 4444

kafka:
  bootstrap:
    servers: localhost:9092
  topics:
    input: <input_topic>
    output: <output_topic>


alg:
  absolutePath: ${PWD}/script-wrapper/exampleScript.py
  lang: python
```