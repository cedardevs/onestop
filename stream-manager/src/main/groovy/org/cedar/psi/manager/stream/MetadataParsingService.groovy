package org.cedar.psi.manager.stream

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.manager.util.ISOParser
import org.xml.sax.SAXException

@Slf4j
class MetadataParsingService {

  static Map parseToInternalFormat(Map msgMap) {
    String format = msgMap.contentType
    String rawMetadata = msgMap.content
    log.info "Parsing message with id: ${msgMap?.id} and conentType: $format "

    Map result = [:]

    try {
      if (format == 'application/xml') {
        result = [discovery: ISOParser.parseXMLMetadataToMap(rawMetadata)]
      }
      else {
        result = [error: 'Unknown raw format of metadata']
      }
    }
    catch(SAXException e) {
      result = [error: "Malformed XML encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e).trim()}"]
    }
    catch(Exception e) {      
      log.error "Unable to parse message with id: ${msgMap?.id}"
      log.error "Caught exception: $e"
      result = [error: "Malformed data encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e).trim()}"]
    }

    return result
  }
}
