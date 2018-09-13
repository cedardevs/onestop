package org.cedar.psi.manager.stream

import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.manager.util.ISOParser
import org.xml.sax.SAXException

class MetadataParsingService {

  static Map parseToInternalFormat(Map msgMap) {
    String format = msgMap.contentType
    String rawMetadata = msgMap.content

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
      result = [error: "Malformed data encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e).trim()}"]
    }

    return result
  }
}
