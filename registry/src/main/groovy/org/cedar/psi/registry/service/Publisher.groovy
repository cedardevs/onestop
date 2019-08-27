package org.cedar.psi.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.Method
import org.cedar.schemas.avro.psi.RecordType
import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import javax.servlet.http.HttpServletRequest
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

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
    def contentType = request.contentType.toString()
    Map isValidContent = (contentType == "application/json") ? isJsonValid(data) : isXmlValid(data)
    
    if (!isValidContent.isValid) {
      return isValidContent
    }
    
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
    log.debug("Publishing $type with id: ${id} from source: $source and method: $message.method")
    kafkaProducer.send(record)?.get()
    return [
        status: 200,
        content: [id: key, type: type]
    ]
  }

  Map isJsonValid(String content) {
    try {
      new JSONObject(content) && content.startsWith("{") && content.endsWith("}")
      return [isValid: true]
    }
    catch (JSONException ex) {
      return [
        status : 400,
        content: [errors: [[title: "Malformed json input: with error ${ex} "]]]
      ]
    }
  }
  
  Map isXmlValid(String content) {
    SAXParserFactory factory = SAXParserFactory.newInstance()
    SAXParser saxParser = factory.newSAXParser()
    DefaultHandler handler = new DefaultHandler()
    try {
      saxParser.parse(new InputSource(new StringReader(content)), handler)
      return [isValid: true]
    }
    catch (SAXException e) {
      return [
        status : 400,
        content: [errors: [[title: "Malformed xml input: with error ${e} "]]]
      ]
    }
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
