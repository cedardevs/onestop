package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.manager.util.ISOParser
import org.cedar.psi.common.avro.ErrorEvent

@Slf4j
class MetadataParsingService {

  static ParsedRecord parseToInternalFormat(Map msgMap) {
    String contentType = msgMap.contentType
    String content = msgMap.content

    try {
      if (!content) {
        def error = ErrorEvent.newBuilder().setTitle("No content provided").build()
        return ParsedRecord.newBuilder().setErrors([error]).build()
      }
      if (contentType != 'application/xml') {
        def error = ErrorEvent.newBuilder()
            .setTitle("Unsupported content type")
            .setDetail("Content type [${contentType}] is not supported")
            .build()
        return ParsedRecord.newBuilder().setErrors([error]).build()
      }

      // TODO - add discovery info once parseXMLMetadataToMap returns a Discovery instance
      return ParsedRecord.newBuilder()
//          .setDiscovery(ISOParser.parseXMLMetadataToMap())
          .build()
    }
    catch (Exception e) {
      def error = ErrorEvent.newBuilder()
          .setTitle("Unable to parse malformed content")
          .setDetail(ExceptionUtils.getRootCauseMessage(e).trim())
          .build()
      log.error "${error.title}: ${error.detail}"
      return ParsedRecord.newBuilder().setErrors([error]).build()
    }
  }

}
