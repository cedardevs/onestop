package org.cedar.psi.api.service

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
@CompileStatic
class Publisher {

  @Value('${kafka.granule.topic}')
  String GRANULE_TOPIC

  @Value('${kafka.collection.topic}')
  String COLLECTION_TOPIC

  private Producer<String, String> kafkaProducer

  @Autowired
  Publisher(Producer<String, String> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  void publishGranule(String data) {
    def slurper = new JsonSlurper()
    def slurpedKey = slurper.parseText(data) as Map

    if (!slurpedKey.trackingId) {
      log.warn("Not publishing message due to no trackingId: ${data}")
      return
    }

    String key = slurpedKey.trackingId.toString()
    def record = new ProducerRecord<String, String>(GRANULE_TOPIC, key, data)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

  void publishCollection(String data, String id = null) {
    def key = id ?: UUID.randomUUID().toString() // TODO - discuss w/ CoMET team and determine what to really do for IDs
    def record = new ProducerRecord<String, String>(COLLECTION_TOPIC, key, data)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }
}
