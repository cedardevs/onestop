package org.cedar.psi.registry.stream;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;

public class PublishingAwareTransformer implements ValueTransformer<ParsedRecord, ParsedRecord> {

  private ProcessorContext context;

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ParsedRecord transform(ParsedRecord value) {
    if (value == null) { return null; }
    Publishing publishing = value.getPublishing();
    boolean markedPrivate = publishing != null ? publishing.getIsPrivate() : false;
    Long untilDate = publishing.getUntil();
    boolean untilDateHasPassed = untilDate != null && untilDate < context.timestamp();
    boolean isPrivate = (markedPrivate && !untilDateHasPassed) || (!markedPrivate && untilDateHasPassed);
    return isPrivate ? null : value;
  }

  @Override
  public void close() {
    // nothing to do
  }

}
