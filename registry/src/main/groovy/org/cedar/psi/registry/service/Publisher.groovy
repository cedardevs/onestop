package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static org.cedar.psi.common.constants.Topics.RAW_GRANULE_TOPIC
import static org.cedar.psi.common.constants.Topics.RAW_COLLECTION_TOPIC



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

  void publishMetadata(HttpServletRequest request, String type, String data, String id = null, String source = null) {
    String topic = topicsByType[type]
    if (!topic) { return }
    Map message = buildInputTopicMessage(request, data, id, source)
    def record = new ProducerRecord<String, Map>(topic, message.id as String, message)
    log.info("Publishing: ${record}")
    kafkaProducer.send(record)
  }

  Map buildInputTopicMessage(HttpServletRequest request, String data, String id = null, String source = null) {
    [
        id: id ?: UUID.randomUUID(),
        method: request?.method,
        host: request?.remoteHost,
        requestUrl: request?.requestURL,
        protocol: request?.protocol,
        content: data,
        contentType: request?.contentType,
        source: source ?: null
    ]
  }

}
