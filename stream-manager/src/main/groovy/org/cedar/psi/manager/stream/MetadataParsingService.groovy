package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cedar.psi.manager.util.ISOParser
import org.xml.sax.SAXException

class MetadataParsingService {

  static String parseToInternalFormat(String msg) {
    def msgMap = new JsonSlurper().parseText(msg) as Map
    String format = msgMap.rawFormat
    String rawMetadata = msgMap.rawMetadata
    def responseMap

    try {
      if(format == 'isoXml') {
        responseMap = [discovery: ISOParser.parseXMLMetadataToMap(rawMetadata)]
      }
      else {
        responseMap = [error: 'Unknown raw format of metadata']
      }
    }
    catch(SAXException e) {
      responseMap = [error: "Malformed XML encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e)}"]
    }
    catch(Exception e) {
      responseMap = [error: "Malformed data encountered; unable to parse. " +
          "Root cause: ${ExceptionUtils.getRootCauseMessage(e)}"]
    }

    return JsonOutput.toJson(responseMap)
  }
}
