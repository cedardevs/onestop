package org.cedar.onestop.parsalyzer.util

import org.cedar.schemas.avro.psi.AggregatedInput
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.RecordType
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RoutingUtilsSpec extends Specification {

  def "identifies inputs that need extraction"() {
    def key = 'ABC'

    expect:
    RoutingUtils.requiresExtraction(key, input) == expected

    where:
    expected | input
    false    | null
    false    | AggregatedInput.newBuilder().build() //  no type set
    false    | AggregatedInput.newBuilder().setType(RecordType.granule).setInitialSource("unknown").build()
    true     | AggregatedInput.newBuilder().setType(RecordType.granule).setInitialSource("common-ingest").build()
  }

  def "identifies if inputs have errors"() {
    def key = 'ABC'

    expect:
    RoutingUtils.hasErrors(key, input) == expected

    where:
    expected | input
    false    | null
    false    | AggregatedInput.newBuilder().build() //  no error set
    true     | AggregatedInput.newBuilder().setErrors([ErrorEvent.newBuilder().setTitle("test").build()]).build()
  }

  def "identifies... nulls"() {
    def key = 'ABC'

    expect:
    RoutingUtils.isNull(key, input) == expected

    where:
    expected | input
    true     | null
    false    | 42
  }

}
