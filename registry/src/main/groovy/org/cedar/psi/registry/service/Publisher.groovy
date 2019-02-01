package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.Method
import org.cedar.schemas.avro.psi.RecordType
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

  Map publishMetadata(HttpServletRequest request, RecordType type, String data, String source, String id = null) {
    String topic = inputTopic(type, source)
    if (!topic) {
      return [
          status: 404,
          content: [errors:[[title: "Unsupported entity type: ${type}"]]]
      ]
    }
    String key = id ?: UUID.randomUUID().toString()
    def message = buildInputTopicMessage(request, type, data, source, key)
    def record = new ProducerRecord<String, Input>(topic, key, message)
    log.info ("Publishing $type with id: ${id}, source: $source and method: $message.method")
    log.debug("Publishing: ${record}")
    kafkaProducer.send(record)?.get()
    return [
        status: 200,
        content: [id: key, type: type]
    ]
  }

  Input buildInputTopicMessage(HttpServletRequest request, RecordType type, String data, String source, String id) {
    def builder = Input.newBuilder()
    builder.type = type
    builder.method = Method.valueOf(request?.method?.toUpperCase())
    builder.content = data
    builder.contentType = request?.contentType
    builder.source = source
    return builder.build()
  }

}
