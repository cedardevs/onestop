package org.cedar.psi.wrapper

import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions
import spock.lang.Specification

class ScriptWrapperFunctionsSpec extends Specification {
  def command_timeout = 10
  def msg = '{"trackingId":"ABC","message":"this is a test","answer": 42}'

  def 'Script Publishs stdout'() {
    String command = "echo stdout"
    def expected = 'stdout\n'

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Script wrapper exit with value 0: command not provided"() {
    String command = " "
    def expected = 'ERROR: ' + 0

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Script wrapper exit with value non-zero value: command Cannot run program"() {
    String command = "py wrong command"
    def expected = 'ERROR: Cannot run program "py": error=2, No such file or directory'

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
  }

  void "Check for identity function (mainly for collection)"() {
    String command = "echo"

    expect:
    ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == msg
  }

}