package org.cedar.psi.manager.stream

import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.manager.util.ISOParser
import org.xml.sax.SAXException

class MetadataParsingService {

  static Map parseToInternalFormat(Map msgMap) {
    String format = msgMap.contentType
    String rawMetadata = msgMap.content

    try {
      if (format == 'application/xml') {
        return [discovery: ISOParser.parseXMLMetadataToMap(rawMetadata)]
      }
      else {
        return [error: 'Unknown raw format of metadata']
      }
    }
    catch(SAXException e) {
      return [error: "Malformed XML encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e).trim()}"]
    }
    catch(Exception e) {
      return [error: "Malformed data encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e).trim()}"]
    }
  }

}
