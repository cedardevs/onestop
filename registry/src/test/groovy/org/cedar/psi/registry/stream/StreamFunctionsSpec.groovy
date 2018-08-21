package org.cedar.psi.registry.stream

import spock.lang.Specification


class StreamFunctionsSpec extends Specification {

  def 'merge function merges json strings'() {
    def currentAggregate = ["trackingId":"ABC","message":"this is a test","answer": 42]
    def newValue = ["trackingId":"ABC", "message":"this is only a test","greeting": "hello, world!"]
    def mergedAggregate = ["trackingId":"ABC","message":"this is only a test","answer":42,"greeting":"hello, world!"]

    expect:
    StreamFunctions.mergeMaps.apply(currentAggregate, newValue) == mergedAggregate
  }

}
