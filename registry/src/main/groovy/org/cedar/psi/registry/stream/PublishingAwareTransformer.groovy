package org.cedar.psi.registry.stream

import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.cedar.psi.registry.util.TimeFormatUtils


class PublishingAwareTransformer implements ValueTransformer<Map, Map> {

  private ProcessorContext context

  @Override
  void init(ProcessorContext context) {
    this.context = context
  }

  @Override
  Map transform(Map value) {
    if (!value) { return null }
    def markedPrivate = value.publishing?.private as Boolean
    def untilDate = TimeFormatUtils.parseTimestamp(value.publishing?.until as String)
    def untilDateHasPassed = untilDate && untilDate < context.timestamp()
    def isPrivate = (markedPrivate && !untilDateHasPassed) || (!markedPrivate && untilDateHasPassed)
    return isPrivate ? null : value
  }

  @Override
  void close() {
    // nothing to do
  }

}
