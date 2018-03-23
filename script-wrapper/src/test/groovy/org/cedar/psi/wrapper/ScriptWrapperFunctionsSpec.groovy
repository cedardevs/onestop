package org.cedar.psi.wrapper

import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions
import spock.lang.Specification

class ScriptWrapperFunctionsSpec extends Specification {
    def 'Script Publishs stdout'() {
        def msg = '{"trackingId":"ABC","message":"this is a test","answer": 42}'
        def command_timeout =  10
        String command = "echo stdout"
        def expected = 'stdout\n'

        expect:
        ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
    }

    void "Script wrapper exit with value 0: command not provided"() {
        def msg = '{"trackingId":"ABC","message":"this is a test","answer": 42}'
        def command_timeout = 10
        String command = " "
        def expected = 'ERROR: ' + 0

        expect:
        ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
    }

    void "Script wrapper exit with value non-zero value: command Cannot run program"() {
        def msg = '{"trackingId":"ABC","message":"this is a test","answer": 42}'
        def command_timeout = 33
        String command = "py wrong command"

        def expected ='ERROR: Cannot run program "py": error=2, No such file or directory'

        expect:
        ScriptWrapperFunctions.scriptCaller(msg, command, command_timeout) == expected
    }

}