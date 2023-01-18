package org.cedar.onestop.kafka.common.util;

import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.apache.kafka.streams.errors.ProductionExceptionHandler.ProductionExceptionHandlerResponse;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnoreRecordTooLargeHandler implements ProductionExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(IgnoreRecordTooLargeHandler.class);

  @Override
  public ProductionExceptionHandlerResponse handle(final ProducerRecord<byte[], byte[]> record,
                                                   final Exception exception) {
    if (exception instanceof RecordTooLargeException) {
      log.warn("Record too large to publish, " +
             "topic: {}, partition: {}",
             record.topic(), record.partition(),
             exception);
      return ProductionExceptionHandlerResponse.CONTINUE;
    } else {
      log.error("Exception caught while publishing record, " +
             "topic: {}, partition: {}",
             record.topic(), record.partition(),
             exception);
      return ProductionExceptionHandlerResponse.FAIL;
    }
  }

  @Override
  public void configure(final Map<String, ?> configs) {
    // ignore
  }
}