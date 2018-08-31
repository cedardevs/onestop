package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static org.cedar.psi.registry.service.MetadataStreamService.RAW_GRANULE_TOPIC
import static org.cedar.psi.registry.service.MetadataStreamService.RAW_COLLECTION_TOPIC

@Slf4j
@Service
@CompileStatic
class Publisher {

  private final Map topicsByType = [
      collection: RAW_COLLECTION_TOPIC,
      granule: RAW_GRANULE_TOPIC
  ]

  private Producer<String, Map> kafkaProducer

  @Autowired
  Publisher(Producer<String, Map> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  Map publishMetadata(HttpServletRequest request, String type, String data, String id = null, String source = null) {
    String topic = topicsByType[type]
    if (!topic) {
      return [
          status: 404,
          content: [errors:[[title: "Unsupported entity type: ${type}"]]]
      ]
    }
    String key = buildMessageKey(source, id)
    Map value = buildInputTopicMessage(request, data, id, source)
    def record = new ProducerRecord<String, Map>(topic, key, value)
    log.info("Publishing: ${record}")
    kafkaProducer.send(record)
    return [
        status: 200,
        content: [id: key, type: type, attributes: value.subMap(['identifiers'])]
    ]
  }

  String buildMessageKey(String source, String id) {
    if (id && !source) { // is a reference to one of our uuids
      return id
    }
    return UUID.randomUUID()
  }

  Map buildInputTopicMessage(HttpServletRequest request, String data, String id = null, String source = null) {
    def input = [
        method: request?.method,
        host: request?.remoteHost,
        requestUrl: request?.requestURL as String,
        protocol: request?.protocol,
        content: data,
        contentType: request?.contentType,
        source: source ?: null
    ]
    def identifiers = source && id ? [(source): id] : [:]
    return [input: input, identifiers: identifiers]
  }

}
