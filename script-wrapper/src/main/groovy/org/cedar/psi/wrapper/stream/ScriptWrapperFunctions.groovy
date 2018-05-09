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
    if (command == "echo") {
      return msg
    }
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
        return JsonOutput.toJson([error: "$stderr"])
      } else {
        log.info "Publishing : $stdout"
        return stdout.toString()
      }
    }
    catch (Exception e) {
      log.error("Caught exception $e: ${e.message}")
      return JsonOutput.toJson([error: "${e.message}"])
    }
  }

  static String parseOutput(String message) {
    message = message.trim()
    log.debug("parsing message: ${message.take(100)}")
    try {
      def isJson = message.startsWith('{')
      if (isJson) {
        log.debug("message is json")
        def messageMap = new JsonSlurper().parseText(message) as Map
        def iso = messageMap.remove('isoXml') as String
        if (iso) { // contains iso --> parse it, drop xml, add parsed attrs back in
          log.debug("message contains isoXml, parsing")
          messageMap.discovery = IsoConversionUtil.parseXMLMetadataToMap(iso)
          return JsonOutput.toJson(messageMap)
        } else { // does not contain iso --> do nothing
          return JsonOutput.toJson([discovery: messageMap])
        }
      }

      def isXml = message.startsWith('<')
      if (isXml) {
        log.debug("message is xml, parsing")
        return JsonOutput.toJson([discovery: IsoConversionUtil.parseXMLMetadataToMap(message)])
      }

    } catch (Exception e) {
      return JsonOutput.toJson([error:"${e.message}"])
    }
    // is neither xml nor json --> is error
    return JsonOutput.toJson([error: 'Output is neither xml nor json: ' + message])
  }
}
