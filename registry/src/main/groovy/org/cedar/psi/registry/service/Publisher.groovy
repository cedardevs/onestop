package org.cedar.psi.registry.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.cedar.psi.registry.service.MetadataStreamService.RAW_GRANULE_TOPIC
import static org.cedar.psi.registry.service.MetadataStreamService.RAW_COLLECTION_TOPIC

@Slf4j
@Service
@CompileStatic
class Publisher {

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
    def record = new ProducerRecord<String, String>(RAW_GRANULE_TOPIC, key, data)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

  void publishCollection(String data, String id = null) {
    def key = id ?: UUID.randomUUID().toString() // TODO - discuss w/ CoMET team and determine what to really do for IDs
    def message = [id: id, isoXml: data]
    def record = new ProducerRecord<String, String>(RAW_COLLECTION_TOPIC, key, JsonOutput.toJson(message))
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

}
