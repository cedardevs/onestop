import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

def LOG_LEVEL = DEBUG

appender("STDOUT", ConsoleAppender) {
  filter(ThresholdFilter) {
    level = INFO
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yy-MM-dd HH:mm:ss.SSS} %5p - [%t] %-40.40logger{39} : %m%n%ex"
  }
  // Redirect output to the System.err.
  target = 'System.err'
}

logger('org.gradle', INFO)
logger('org.cedar.psi.manager', DEBUG, ["STDOUT"])
logger('org.apache.kafka', DEBUG, ["STDOUT"])
logger('org.codehaus.groovy', DEBUG, ["STDOUT"])
logger("org.gradle", WARN)

root(LOG_LEVEL, ["STDOUT"])
