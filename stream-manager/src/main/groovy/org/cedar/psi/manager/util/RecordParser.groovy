package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.ErrorEvent
import org.cedar.psi.common.avro.Publishing
import org.cedar.psi.common.avro.RecordType

@Slf4j
class RecordParser {

  static ParsedRecord parse(Map msgMap, RecordType type) {
    String contentType = msgMap.contentType
    String content = msgMap.content
    String method = msgMap.method
    // update parsed record to default values
    if (method == 'DELETE'){
      return ParsedRecord.newBuilder().setType(type).build()
    }

    try {
      if (!content) {
        def error = ErrorEvent.newBuilder().setTitle("No content provided").build()
        return ParsedRecord.newBuilder().setType(type).setErrors([error]).build()
      }
      if (contentType != 'application/xml') {
        def error = ErrorEvent.newBuilder()
            .setTitle("Unsupported content type")
            .setDetail("Content type [${contentType}] is not supported")
            .build()
        return ParsedRecord.newBuilder().setType(type).setErrors([error]).build()
      }

      def builder = ParsedRecord.newBuilder()
      builder.setType(type)
      builder.setDiscovery(ISOParser.parseXMLMetadataToDiscovery(content))
      builder.setPublishing(parsePublishing(msgMap?.publishing))
      return builder.build()
    }
    catch (Exception e) {
      def error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse malformed content")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .build()
      log.error "${error.title}: ${error.detail}"
      return ParsedRecord.newBuilder().setType(type).setErrors([error]).build()
    }
  }

  static Publishing parsePublishing(Map input) {
    def builder = Publishing.newBuilder()
    if (input?.containsKey('isPrivate')) {
      builder.isPrivate = input.isPrivate as Boolean
    }
    if (input?.containsKey('until')) {
      builder.until = input.until as Long
    }
    return builder.build()
  }

}
