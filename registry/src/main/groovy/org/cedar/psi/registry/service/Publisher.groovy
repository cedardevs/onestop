package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

import static org.cedar.psi.common.constants.Topics.inputTopic


@Slf4j
@Service
@CompileStatic
class Publisher {

  private Producer<String, Input> kafkaProducer

  @Autowired
  Publisher(Producer<String, Input> kafkaProducer) {
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
    def message = buildInputTopicMessage(request, data, source, key)
    def record = new ProducerRecord<String, Input>(topic, key, message)
    log.info ("Publishing $type with id: ${id} and source: $source")
    log.debug("Publishing: ${record}")
    kafkaProducer.send(record)?.get()
    return [
        status: 200,
        content: [id: key, type: type]
    ]
  }

  Input buildInputTopicMessage(HttpServletRequest request, String data, String source, String id) {
    def builder = Input.newBuilder()
    builder.method = Method.valueOf(request?.method?.toUpperCase())
    builder.host = request?.remoteHost
    builder.requestUrl = request?.requestURL as String
    builder.protocol = request?.protocol
    builder.content = data
    builder.contentType = request?.contentType
    builder.source = source
    return builder.build()
  }

}
