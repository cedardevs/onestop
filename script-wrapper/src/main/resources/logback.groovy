// always a good idea to add an on console status listener
import ch.qos.logback.core.*
import ch.qos.logback.core.rolling.*


def appenderList = ["ROLLING"]
def consoleAppender = true
def USER_HOME = System.getProperty("user.home")
def HOSTNAME=hostname
def PROJECTNAME= "script-wrapper"
if (consoleAppender) {
    appender("CONSOLE", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        }
    }
}

appender("ROLLING", RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        Pattern = "%d %level %thread %mdc %logger - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "${USER_HOME}/log/${PROJECTNAME}-%d{yyyy-MM}-${HOSTNAME}.log"
    }
}

root(INFo, appenderList)
