package org.cedar.psi.api.services

import groovy.json.JsonSlurper
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
    private Producer<String, String> createProducer

    @Value('${kafka.granule.topic}')
    String GRANULETOPIC

    void publishGranule(String data) {

        Producer<String, String> producer = createProducer

        def slurper = new JsonSlurper()
        def slurpedKey = slurper.parseText(data)

        if(!slurpedKey.trackingId){
            log.debug("missing trackingid from ='{}", data)
        } else {
            String key = (slurpedKey.trackingId).toString()
            log.info("sending data ='{}'", data)

            ProducerRecord<String, String> record = new ProducerRecord<String, String>(GRANULETOPIC, key, data)
            log.info("topic = $record.topic(), partition = $record.partition(), key = $record.key(), value = $record.value()")
            producer.send(record)

        }
    }
}