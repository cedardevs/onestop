package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static org.cedar.psi.common.constants.Topics.inputTopic


@Slf4j
@Service
@CompileStatic
class Publisher {

  private Producer<String, Map> kafkaProducer

  @Autowired
  Publisher(Producer<String, Map> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  Map publishMetadata(HttpServletRequest request, String type, String data, String source, String id = null) {
    String topic = inputTopic(type, source)
    if (!topic) {
      return [
          status: 404,
          content: [errors:[[title: "Unsupported entity type: ${type}"]]]
      ]
    }
    String key = id ?: UUID.randomUUID().toString()
    Map value = buildInputTopicMessage(request, data, source, key)
    def record = new ProducerRecord<String, Map>(topic, key, value)
    log.info ("Publishing $type with id: ${id} and source: $source")
    log.debug("Publishing: ${record}")
    kafkaProducer.send(record)?.get()
    return [
        status: 200,
        content: [id: key, type: type, attributes: value.subMap(['identifiers'])]
    ]
  }

  Map buildInputTopicMessage(HttpServletRequest request, String data, String source, String id) {
    def input = [
        method: request?.method,
        host: request?.remoteHost,
        requestUrl: request?.requestURL as String,
        protocol: request?.protocol,
        content: data,
        contentType: request?.contentType,
        source: source
    ]
    return [input: input, identifiers: [(source): id]]
  }

}
