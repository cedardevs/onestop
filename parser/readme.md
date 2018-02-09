### Overview

This is a Kafka Streams app which listens for incoming raw metadata events and parses it.

Currently this only supports parsing dscovr raw metadata as scraped from ingest-manager. 


### Usage
Given this config - 
```
kafka:
  bootstrap:
    servers: localhost:9092
  topics:
    input: raw-granule-aggregator-raw-granules-changelog
    output: parsed-granules

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
kafka:
  bootstrap:
    servers: localhost:9092
  topics:
    input: raw-granule-aggregator-raw-granules-changelog
    output: parsed-granules

```