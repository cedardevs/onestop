package org.cedar.onestop.kafka.common.util;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

/**
 * A Kafka Streams {@link ValueTransformer} which takes any value, extracts its topic
 * via {@link ProcessorContext#topic()}, and returns value wrapped in a {@link ValueWithTopic}
 * @param <T> The type of the values being transformed
 */
public class TopicIdentifier<T> implements ValueTransformer<T, ValueWithTopic<T>> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ValueWithTopic<T> transform(T value) {
    return new ValueWithTopic<>(value, context.topic());
  }

  @Override
  public void close() {

  }
}
