package org.cedar.onestop.kafka.common.util;

import org.cedar.schemas.avro.psi.ErrorEvent;

import java.util.List;

public class ValueWithErrors<T> {

  public final T value;
  public final List<ErrorEvent> errors;

  public ValueWithErrors(T value, List<ErrorEvent> errors) {
    this.value = value;
    this.errors = errors;
  }

  public T getValue() { return value; }
  public List<ErrorEvent> getErrors() { return errors; }
  public boolean isEmpty() { return value == null && (errors == null || errors.size() == 0); }

}
