import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.util.FileSize

import static ch.qos.logback.classic.Level.*

def APP_NAME = 'dscovr-processor'
def LOG_PATH = 'data/logs/'
def LOG_LEVEL = DEBUG

appender("STDOUT", ConsoleAppender) {
    filter(ThresholdFilter) {
        level = INFO
    }
    encoder(PatternLayoutEncoder) {
        pattern =  "%d{yy-MM-dd HH:mm:ss.SSS} %5p - [%t] %-40.40logger{39} : %m%n%ex"
    }
    // Redirect output to the System.err.
    target = 'System.err'
}

appender("FILE", RollingFileAppender) {
    file = "${LOG_PATH}/${APP_NAME}.log"
    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_PATH}/${APP_NAME}.%d{yyyy-MM-dd}.log.%i"
        maxFileSize= FileSize.valueOf("10MB")
        maxHistory = 7
        totalSizeCap = FileSize.valueOf("100MB")
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yy-MM-dd HH:mm:ss.SSS} %5p - [%t] %-40.40logger{39} : %m%n%ex"
    }
}

logger('org.gradle', INFO)
logger('org.cedar.psi.wrapper', DEBUG, ["STDOUT"])
logger('org.apache.kafka', DEBUG, ["STDOUT"])
logger('org.codehaus.groovy', DEBUG, ["STDOUT"])
logger("org.gradle", WARN)

root(LOG_LEVEL, ["FILE"])