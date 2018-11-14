package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.manager.util.ISOParser
import org.cedar.psi.common.avro.ErrorEvent

@Slf4j
class MetadataParsingService {

  static Object parseToInternalFormat(Map msgMap) {
    String contentType = msgMap.contentType
    String content = msgMap.content

    try {
      if (!content) {
        return new ErrorEvent(title: "No content provided")
      }
      if (contentType != 'application/xml') {
        return new ErrorEvent(title: "Unsupported content type", detail: "Content type [${contentType}] is not supported")
      }

      return [discovery: ISOParser.parseXMLMetadataToMap(content)]
    }
    catch(Exception e) {
      log.error "Caught exception: $e"
      return new ErrorEvent(title: "Unable to parse malformed content", detail: ExceptionUtils.getRootCauseMessage(e).trim())
    }
  }

}
