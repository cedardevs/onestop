package org.cedar.psi.wrapper.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.cedar.psi.wrapper.util.IsoConversionUtil

@Slf4j
@CompileStatic
class ScriptWrapperFunctions {
  static String scriptCaller(Object msg, String command, long timeout) {
    List<String> commandList = command.split(' ').toList()
    log.info "Running : $commandList"
    try {
      def stdout = new StringBuilder()
      def stderr = new StringBuilder()
      def cmd = commandList.execute()
      cmd.consumeProcessOutput(stdout, stderr)
      cmd.withOutputStream { OutputStream os ->
        os << msg
      }
      cmd.waitForOrKill(timeout)
      if (cmd.exitValue()) {
        log.error "Process exited with non-zero exit code"
        log.error "Process stdout: ${stdout}"
        log.error "Process stderr: ${stderr}"
        return 'ERROR: ' + stderr
      }
      else {
        log.info "Publishing : $stdout"
        return stdout.toString()
      }
    }
    catch (Exception e) {
      log.error("Caught exception $e: ${e.message}")
      return 'ERROR: ' + e.message
    }
  }

  static String parseOutput(String message) {
    message = message.trim()
    log.debug("parsing message: ${message.take(100)}")
    def isJson = message.startsWith('{')
    if (isJson) {
      log.debug("message is json")
      def messageMap = new JsonSlurper().parseText(message) as Map
      def iso = messageMap.isoXml as String
      if (iso) { // contains iso --> parse it, drop xml, merge parsed attrs back in
        log.debug("message contains isoXml, parsing")
        def parsedIso = IsoConversionUtil.parseXMLMetadataToMap(iso)
        messageMap.remove('isoXml')
        return JsonOutput.toJson(messageMap + parsedIso)
      }
      else { // does not contain iso --> do nothing
        return message
      }
    }

    def isXml = message.startsWith('<')
    if (isXml) {
      log.debug("message is xml, parsing")
      return IsoConversionUtil.parseXMLMetadata(message)
    }

    // is neither xml nor json --> is error
    return 'ERROR: Output is neither xml nor json: ' + message
  }
}
