package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Deserialization handler that logs a deserialization exception and then
 * signals the processing pipeline to continue processing more records.
 */
public class LogAndContinueExceptionHandler implements DeserializationExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(LogAndContinueExceptionHandler.class);

  @Override
  public DeserializationHandlerResponse handle(final ProcessorContext context,
                                               final ConsumerRecord<byte[], byte[]> record,
                                               final Exception exception) {

    log.warn("Exception caught during Deserialization, " +
             "taskId: {}, topic: {}, partition: {}, offset: {}",
             context.taskId(), record.topic(), record.partition(), record.offset(),
             exception);

    return DeserializationHandlerResponse.CONTINUE;
  }

  @Override
  public void configure(final Map<String, ?> configs) {
    // ignore
  }
}