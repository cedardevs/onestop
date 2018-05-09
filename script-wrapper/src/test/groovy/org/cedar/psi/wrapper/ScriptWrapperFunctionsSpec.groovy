package org.cedar.psi.wrapper

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions
import spock.lang.Specification

class ScriptWrapperFunctionsSpec extends Specification {
  def command_timeout = 10
  def msg = '{"trackingId":"ABC","message":"this is a test","answer": 42}'
  def testIso = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-metadata.xml').text

  def 'Script Publishs stdout'() {
    String command = "echo stdout"
    def expected = 'stdout\n'

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Script wrapper exit with value 0: command not provided"() {
    String command = " "
    def expected = JsonOutput.toJson([ error : '0' ])

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Script wrapper exit with value non-zero value: command Cannot run program"() {
    String command = "py wrong command"
    def expected = JsonOutput.toJson([error: 'Cannot run program "py": error=2, No such file or directory'])

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Check for identity function (mainly for collection)"() {
    String command = "echo"

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == msg
  }

  def 'xml output is parsed into discovery info'() {
    when:
    def result = ScriptWrapperFunctions.parseOutput(testIso)
    def parsedResult = new JsonSlurper().parseText(result)

    then:
    result instanceof String
    parsedResult instanceof Map
    parsedResult.discovery instanceof Map
    parsedResult.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
  }

  def 'json output with no xml is returns as discovery info'() {
    def input = '{"hello":"world"}'

    expect:
    ScriptWrapperFunctions.parseOutput(input) == "{\"discovery\":$input}"
  }

  def 'json output with xml within it results in returned json plus parsed discovery info'() {
    def input = JsonOutput.toJson([
        publishing: [private: true],
        isoXml: testIso
    ])

    when:
    def result = ScriptWrapperFunctions.parseOutput(input)
    def parsedResult = new JsonSlurper().parseText(result)

    then:
    result instanceof String
    parsedResult instanceof Map
    parsedResult.isoXml == null
    parsedResult.publishing instanceof Map
    parsedResult.publishing.private == true
    parsedResult.discovery instanceof Map
    parsedResult.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
  }

  def 'returns an error if neither xml nor json output is provided'() {
    expect:
    new JsonSlurper().parseText(ScriptWrapperFunctions.parseOutput('not a good response')).containsKey('error')
  }

}
