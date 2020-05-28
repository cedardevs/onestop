package org.cedar.onestop.registry.service

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.cedar.onestop.registry.util.UUIDValidator
import org.cedar.schemas.avro.psi.Input
import org.cedar.schemas.avro.psi.Method
import org.cedar.schemas.avro.psi.OperationType
import org.cedar.schemas.avro.psi.RecordType
import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import javax.servlet.http.HttpServletRequest
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import static org.cedar.onestop.kafka.common.constants.Topics.inputTopic


@Slf4j
@Service
@CompileStatic
class Publisher {

  private static final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance()
  private Producer<String, Input> kafkaProducer

  @Autowired
  Publisher(Producer<String, Input> kafkaProducer) {
    this.kafkaProducer = kafkaProducer
  }

  Map publishMetadata(HttpServletRequest request, RecordType type, String data, String source, String id = null, String op) {
    // check for valid UUID String
    if(id != null && !UUIDValidator.isValid(id)){
      return UUIDValidator.uuidErrorMsg(id)
    }
    //check for malformed content
    Map isValidContent = isContentValid(data, request.contentType)
    if (!isValidContent.isValid) {
      return isValidContent
    }

    String topic = inputTopic(type, source)
    if (!topic) {
      return [
          status : 404,
          content: [errors: [[title: "Unsupported entity type: ${type}"]]]
      ]
    }

    String key = id ?: UUID.randomUUID().toString()
    try {
      def message = buildInputTopicMessage(request, type, data, source, op)
      def record = new ProducerRecord<String, Input>(topic, key, message)
      log.debug("Publishing $type with id: ${id} from source: $source and method: $message.method")
      kafkaProducer.send(record)?.get()
      return [
          status : 200,
          content: [id: key, type: type]
      ]
    }
    catch (IllegalArgumentException e) {
      return [
          status: 400,
          content: [errors: [[title: "Bad op for PATCH request", detail: e.getMessage()]]]
      ]
    }
  }

  Map isContentValid(String content, String contentType) {
    if (content == null) {
      // request has no content
      return [isValid: true]
    }
    MediaType mediaType = contentType != null ? MediaType.valueOf(contentType) : null
    // check for valid JSON based on MediaType
    try {
      if (mediaType == MediaType.APPLICATION_JSON) {
        isValidJson(content)
        return [isValid: true]
      }
      // check for valid XML based on MediaType
      else if (mediaType == MediaType.APPLICATION_XML) {
        isValidXml(content)
        return [isValid: true]
      }
      // somehow someone was able to publish an unsupported MediaType (check `consumes =` in controller)
      else {
        return unknownContentTypeError(mediaType)
      }
    }
    catch (JSONException | SAXException ex) {
      return invalidContentError(ex.message, mediaType)
    }
  }

  boolean isValidXml(String content) {
    SAXParser saxParser = saxParserFactory.newSAXParser()
    DefaultHandler handler = new DefaultHandler()
    saxParser.parse(new InputSource(new StringReader(content)), handler)
    return true
  }

  boolean isValidJson(String content) {
    new JSONObject(content) && content.startsWith("{") && content.endsWith("}")
    return true
  }

  Map invalidContentError(String message, MediaType contentType) {
    return [
        status : 400,
        content: [errors: [[title: "Malformed ${contentType.toString()} input: with error ${message} "]]]
    ]
  }

  Map unknownContentTypeError(MediaType contentType) {
    return [
        status : 400,
        content: [errors: [[title: "Content-Type of \"${contentType.toString()}\" is not supported. Use JSON or XML."]]]
    ]
  }

  Input buildInputTopicMessage(HttpServletRequest request, RecordType type, String data, String source, String op) {
    OperationType operation;
    if (op == null) {
      operation = OperationType.NO_OP
    }
    else {
      try {
        operation = OperationType.valueOf(op.toUpperCase().strip())
      }
      catch(IllegalArgumentException e) {
        throw new IllegalArgumentException("Received PATCH request with invalid operation type value of [ " + op + " ]. " +
            "Permitted options are (case-insensitive): " + EnumSet.allOf(OperationType.class))
      }
    }
    def builder = Input.newBuilder()
    builder.type = type
    builder.method = Method.valueOf(request?.method?.toUpperCase())
    builder.content = data
    builder.contentType = request?.contentType
    builder.source = source
    builder.operation = operation
    return builder.build()
  }

}
