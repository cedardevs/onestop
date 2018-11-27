package org.cedar.psi.common.util

import org.cedar.psi.common.avro.Input
import org.cedar.psi.common.avro.Method
import org.cedar.psi.common.avro.ParsedRecord
import org.cedar.psi.common.avro.RecordType
import spock.lang.Specification

class AvroUtilsSpec extends Specification {

  def 'transforms an Input into a map'() {
    def builder = Input.newBuilder()
    builder.method = Method.POST
    builder.protocol = 'http'
    builder.host = 'localhost'
    builder.requestUrl = '/test'
    builder.source = 'test'
    builder.contentType = 'application/json'
    builder.content = '{"hello":"world"}'
    def testInput = builder.build()

    expect:
    AvroUtils.avroToMap(testInput) == [
        method: Method.POST,
        protocol: 'http',
        host: 'localhost',
        requestUrl: '/test',
        source: 'test',
        contentType: 'application/json',
        content: '{"hello":"world"}'
    ]
  }

  def 'ParsedRecord has empty array of errors by default'() {
    def record = ParsedRecord.newBuilder().setType(RecordType.collection).build()

    expect:
    record.errors instanceof List
    record.errors.size() == 0
  }


}
