package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
  Exception handler that will replace the thread if an uncaught exception occurs. Many
  uncaught exceptions within a short period of time will cause the application to shutdown
  as this indicates a larger issue.

  See https://developer.confluent.io/tutorials/error-handling/confluent.html
*/
public class UncaughtExceptionHandler implements StreamsUncaughtExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);
  static int maxFailures;
  static int maxTimeIntervalMillis;
  private Instant previousErrorTime;
  private int currentFailureCount;


  public UncaughtExceptionHandler(final int maxFailures, final int maxTimeIntervalMillis) {
      log.info("Initializing streams uncaught exception handler with max failures "+maxFailures+" and interval "+maxTimeIntervalMillis);
      this.maxFailures = maxFailures;
      this.maxTimeIntervalMillis = maxTimeIntervalMillis;
  }

  @Override
  public StreamThreadExceptionResponse handle(final Throwable e) {
      currentFailureCount++;
      Instant currentErrorTime = Instant.now();

      if (previousErrorTime == null) {
          previousErrorTime = currentErrorTime;
      }

      long millisBetweenFailure = ChronoUnit.MILLIS.between(previousErrorTime, currentErrorTime);

      if (currentFailureCount >= maxFailures) {
        log.error("Caught exception: "+e);
        if (millisBetweenFailure <= maxTimeIntervalMillis) {
          log.error("Exception rate threshold exceeded (" + currentFailureCount +
            " exceptions within " + millisBetweenFailure + " ms)");
          return SHUTDOWN_APPLICATION;
        } else {
          log.error(currentFailureCount + " exceptions thrown within " +
            millisBetweenFailure + " ms which is within the threhold. Resetting failure count and start time.");
          currentFailureCount = 0;
          previousErrorTime = null;
        }
      }
      log.error("Caught exception and replacing thread. Exception: "+e);
      return REPLACE_THREAD;
  }
}
