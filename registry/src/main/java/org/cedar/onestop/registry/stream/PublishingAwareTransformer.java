package org.cedar.onestop.registry.stream;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.cedar.schemas.avro.psi.ParsedRecord;

public class PublishingAwareTransformer implements ValueTransformer<ParsedRecord, ParsedRecord> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ParsedRecord transform(ParsedRecord value) {
    if (value == null) { return null; }
    var publishing = value.getPublishing();
    var markedPrivate = publishing != null ? publishing.getIsPrivate() : false;
    var untilDate = publishing.getUntil();
    var untilDateHasPassed = untilDate != null && untilDate < context.timestamp();
    var isPrivate = (markedPrivate && !untilDateHasPassed) || (!markedPrivate && untilDateHasPassed);
    return isPrivate ? null : value;
  }

  @Override
  public void close() {
    // nothing to do
  }

}
