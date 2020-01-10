package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.ValueAndTimestamp;

/**
 * A Kafka Streams {@link ValueTransformer} which takes any value, extracts its timestamp
 * via {@link ProcessorContext#timestamp}, and returns value wrapped in a {@link ValueAndTimestamp}
 * @param <T> The type of the values being transformed
 */
public class Timestamper<T> implements ValueTransformer<T, ValueAndTimestamp<T>> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ValueAndTimestamp<T> transform(T value) {
    return ValueAndTimestamp.make(value, context.timestamp());
  }

  @Override
  public void close() {}

}
