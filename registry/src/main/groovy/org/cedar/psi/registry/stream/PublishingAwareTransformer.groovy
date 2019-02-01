package org.cedar.psi.registry.stream

import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.cedar.schemas.avro.psi.ParsedRecord

class PublishingAwareTransformer implements ValueTransformer<ParsedRecord, ParsedRecord> {

  private ProcessorContext context

  @Override
  void init(ProcessorContext context) {
    this.context = context
  }

  @Override
  ParsedRecord transform(ParsedRecord value) {
    if (!value) { return null }
    def markedPrivate = value.publishing?.isPrivate
    def untilDate = value.publishing?.until
    def untilDateHasPassed = untilDate && untilDate < context.timestamp()
    def isPrivate = (markedPrivate && !untilDateHasPassed) || (!markedPrivate && untilDateHasPassed)
    return isPrivate ? null : value
  }

  @Override
  void close() {
    // nothing to do
  }

}
