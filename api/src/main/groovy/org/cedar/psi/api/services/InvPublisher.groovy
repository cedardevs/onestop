package org.cedar.psi.api.services

import groovy.json.JsonSlurper
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Service
@Configuration
class InvPublisher {
    private static final Logger log = LoggerFactory.getLogger(InvPublisher.class)

    @Autowired
    private Producer<String, GenericRecord> createProducer

    @Value('${kafka.granule.topic}')
    String GRANULETOPIC

    void publishGranule(String data) {

        String GRANULE_SCHEMA = "{" +
                "   \"namespace\": \"org.cedar.psi.api\"," +
                "   \"type\": \"record\", " +
                "   \"name\": \"Granule\"," +
                "   \"fields\": [" +
                "       {\"name\": \"trackingId\", \"type\": \"string\"}," +
                "       {\"name\": \"dataStream\", \"type\": \"string\"}," +
                "       {\"name\": \"checksum\", \"type\": \"string\"}," +
                "       {\"name\": \"relativePath\", \"type\": \"string\"}," +
                "       {\"name\": \"path\", \"type\": \"string\"}," +
                "       {\"name\": \"fileSize\", \"type\": [\"null\",\"int\"], \"default\":\"null\"}," +
                "       {\"name\": \"lastUpdated\", \"type\": [\"null\",\"string\"], \"default\":\"null\" }" +
                    " ]" +
                "}"

        Producer<String, GenericRecord> producer = createProducer
        Schema.Parser parser = new Schema.Parser()
        Schema schema = parser.parse(GRANULE_SCHEMA)

        def slurper = new JsonSlurper()
        def slurpedKey = slurper.parseText(data)

        if(!slurpedKey.trackingId){
            log.debug("missing trackingid from ='{}", data)
        } else {
            String key = (slurpedKey.trackingId).toString()
            GenericRecord granule = new GenericData.Record(schema)
            log.info("sending data ='{}'", data)
            granule.put("trackingId", slurpedKey.trackingId)
            granule.put("dataStream", slurpedKey.dataStream)
            granule.put("checksum", slurpedKey.checksum)
            granule.put("relativePath", slurpedKey.relativePath)
            granule.put("path", slurpedKey.path)
            granule.put("fileSize", slurpedKey.fileSize)
            granule.put("lastUpdated", slurpedKey.lastUpdated)

            ProducerRecord<String, GenericRecord> record = new ProducerRecord<String, GenericRecord>(GRANULETOPIC, key, granule)
            log.info("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n",
                    record.topic(), record.partition(),
                    record.key(), record.value())
            producer.send(record)

        }
    }
}