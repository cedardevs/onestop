package org.cedar.psi.registry.stream

import groovy.json.JsonSlurper
import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.processor.ProcessorContext


class PublishingAwareTransformer implements ValueTransformer<String, String> {

  @Override
  void init(ProcessorContext context) {
    // nothing to do
  }

  @Override
  String transform(String value) {
    if (!value) { return null }
    def valueMap = new JsonSlurper().parseText(value) as Map
    def isPrivate = valueMap.publishing?.private as Boolean
    return isPrivate ? null : value
  }

  @Override
  @Deprecated
  String punctuate(long timestamp) {
    return null // do nothing
  }

  @Override
  void close() {
    // nothing to do
  }

}
