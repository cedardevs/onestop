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

curl -X PUT -H "Content-Type: application/json" http://localhost:8080/metadata/granule -d  '{
                                                                                  "dataStream": "dscover",
                                                                                  "trackingId": "2",
                                                                                  "checksum": "fd297fcceb94fdbec5297938c99cc7b5",
                                                                                  "relativePath": "it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
                                                                                  "path": "/dscovr/valid/it_fc1_dscovr_s20131218000000_e20131218235951_p20161215345159_pub.nc.gz",
                                                                                  "fileSize": 6526,
                                                                                  "lastUpdated":"2017124"
                                                                                  }'