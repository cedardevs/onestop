package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Publishing
import org.cedar.schemas.avro.psi.RecordType

@Slf4j
class RecordParser {

  static ParsedRecord parse(Map msgMap, RecordType type) {
    String contentType = msgMap.contentType
    String content = msgMap.content
    String method = msgMap.method
    try {
      // update parsed record to default values
      if (method == 'DELETE') {
        def error = ErrorEvent.newBuilder()
            .setTitle("record deleted")
            .setDetail("Record for this $type is Updated")
            .build()
        return ParsedRecord.newBuilder().setType(type).setErrors([error]).build()
      }
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
