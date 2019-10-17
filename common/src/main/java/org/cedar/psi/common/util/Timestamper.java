package org.cedar.psi.common.util;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * A Kafka Streams {@link ValueTransformer} which takes any value, extracts its timestamp
 * via {@link ProcessorContext#timestamp}, and returns value wrapped in a {@link TimestampedValue}
 * @param <T> The type of the values being transformed
 */
public class Timestamper<T> implements ValueTransformer<T, TimestampedValue<T>> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public TimestampedValue<T> transform(T value) {
    return new TimestampedValue<T>(context.timestamp(), value);
  }

  @Override
  public void close() {}

}
