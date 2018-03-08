package org.cedar.psi.wrapper.stream

import groovy.util.logging.Slf4j

@Slf4j
class ScriptWrapperFunctions {
    static scriptCaller(Object msg, String command, long timeout){
        List<String> commandList = command.split(' ') .toList()
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
        catch(Exception e){
            log.error("Caught exception $e: ${e.message}")
            return 'ERROR: ' + e.message
        }
    }
}
