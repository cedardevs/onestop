package org.cedar.psi.common.util;

/**
 * A simple struct to hold some kind of a thing plus a timestamp
 * @param <T> The type of thing being held
 */
public class TimestampedValue<T> {
  public final long timestampMillis;
  public final T data;

  TimestampedValue(long timestampMillis, T data) {
    this.timestampMillis = timestampMillis;
    this.data = data;
  }
}
