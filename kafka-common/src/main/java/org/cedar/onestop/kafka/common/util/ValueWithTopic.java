package org.cedar.onestop.kafka.common.util;

public class ValueWithTopic<T> {

  private final T value;
  private final String topic;

  public ValueWithTopic(T value, String topic) {
    this.value = value;
    this.topic = topic;
  }

  public T getValue() { return value; }
  public String getTopic() { return topic; }
  public boolean isEmpty() { return value == null && (topic == null || topic.isBlank()); }
}
