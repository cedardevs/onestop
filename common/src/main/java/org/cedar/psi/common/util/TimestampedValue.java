package org.cedar.psi.common.util;

/**
 * A simple struct to hold some kind of a thing plus a timestamp
 * @param <T> The type of thing being held
 */
public class TimestampedValue<T> {
  public final long timestampMs;
  public final T data;

  TimestampedValue(long timestampMs, T data) {
    this.timestampMs = timestampMs;
    this.data = data;
  }
}
