package org.cedar.psi.api.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class InvPublisher {
    private static final Logger log = LoggerFactory.getLogger(InvPublisher.class)

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate

    @Value('${kafka.granule.topic}')
    String kafkaTopic

    void publishGranule(String data) {
        log.info("sending data ='{}'", data)

        kafkaTemplate.send(kafkaTopic, data)

    }
}