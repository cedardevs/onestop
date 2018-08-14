package org.cedar.psi.registry.stream

import groovy.json.JsonSlurper
import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.cedar.psi.registry.util.TimeFormatUtils


class PublishingAwareTransformer implements ValueTransformer<String, String> {

  private ProcessorContext context

  @Override
  void init(ProcessorContext context) {
    this.context = context
  }

  @Override
  String transform(String value) {
    if (!value) { return null }
    def valueMap = new JsonSlurper().parseText(value) as Map
    def markedPrivate = valueMap.publishing?.private as Boolean
    def untilDate = TimeFormatUtils.parseTimestamp(valueMap.publishing?.until as String)
    def untilDateHasPassed = untilDate && untilDate < context.timestamp()
    def isPrivate = (markedPrivate && !untilDateHasPassed) || (!markedPrivate && untilDateHasPassed)
    return isPrivate ? null : value
  }

  @Override
  void close() {
    // nothing to do
  }

}
