package org.cedar.psi.common.util

import org.apache.avro.generic.GenericRecord


class AvroUtils {

  static Map<String, Object> avroToMap(GenericRecord record) {
    def fieldNames = record.schema.fields*.name()
    def collector = new LinkedHashMap<String, Object>(fieldNames.size())
    return fieldNames.inject(collector, { result, field ->
      result.put(field, record.get(field))
      result
    })
  }

}
