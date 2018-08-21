package org.cedar.psi.registry.service

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

  private Producer<String, Map> kafkaProducer

  @Autowired
  Publisher(Producer<String, Map> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  void publishGranule(String data) {
    def slurper = new JsonSlurper()
    def slurpedData = slurper.parseText(data) as Map

    if (!slurpedData.trackingId) {
      log.warn("Not publishing message due to missing trackingId: ${data}")
      return
    }

    String key = slurpedData.trackingId.toString()
    def record = new ProducerRecord<String, Map>(RAW_GRANULE_TOPIC, key, slurpedData)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

  void publishGranuleIso(String data, String id = null) {
    def key = id ?: UUID.randomUUID().toString() // TODO - discuss w/ team and determine what to really do for IDs
    def message = [id: id, rawFormat: "isoXml", rawMetadata: data]
    def record = new ProducerRecord<String, Map>(RAW_GRANULE_TOPIC, key, message)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

  void publishCollection(String data, String id = null) {
    def key = id ?: UUID.randomUUID().toString() // TODO - discuss w/ CoMET team and determine what to really do for IDs
    def message = [id: id, rawFormat: "isoXml", rawMetadata: data]
    def record = new ProducerRecord<String, Map>(RAW_COLLECTION_TOPIC, key, message)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

}
