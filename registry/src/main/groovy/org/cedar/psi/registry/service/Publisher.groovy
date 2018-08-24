package org.cedar.psi.registry.service

import groovy.json.JsonSlurper
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

  private Producer<String, Map> kafkaProducer
  final String COLLECTION_PATH_VAR = 'collection'
  final String GRANULE_PATH_VAR = 'granule'

  @Autowired
  Publisher(Producer<String, Map> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  void publishMetadata(HttpServletRequest request, String type, String source = null, String id = null, String data) {
    String topic
    if(type.equalsIgnoreCase(COLLECTION_PATH_VAR)){
      topic = RAW_COLLECTION_TOPIC
    }else if (type.equalsIgnoreCase(GRANULE_PATH_VAR)){
      topic = RAW_GRANULE_TOPIC
    }else{return}
    Map message = buildInputTopicMessage(request, id, source, data)
    def record = new ProducerRecord<String, Map>(topic, id, message)
    log.debug("Sending: ${record}")
    kafkaProducer.send(record)
  }

  Map buildInputTopicMessage(HttpServletRequest request, String source = null, String id = null, String data){
    [
        id: id ?: UUID.randomUUID(),
        method: request?.method,
        host: request?.remoteHost,
        requestUrl: request?.requestURL,
        protocol: request?.protocol,
        content: data,
        contentType: request?.contentType,
        source: source ?: ''
    ]
  }

}
