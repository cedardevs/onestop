import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

appender("STDOUT", ConsoleAppender) {
  filter(ThresholdFilter) {
    def threshold = System.getenv("LOGGING_THRESHOLD") ?: System.getProperty("logging.threshold")
    level = toLevel(threshold, DEBUG)
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %5p - [%t] %-40.40logger{39} : %m%n%ex"
  }
  // Redirect output to the System.err.
  target = 'System.err'
}

logger('org.apache.kafka', INFO)
logger('org.cedar', DEBUG)
logger('org.cedar.schemas', WARN)
logger('org.codehaus.groovy', INFO)
logger("org.gradle", WARN)

root(INFO, ["STDOUT"])
