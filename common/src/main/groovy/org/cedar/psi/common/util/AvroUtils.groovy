package org.cedar.psi.common.util

import org.apache.avro.generic.GenericRecord


class AvroUtils {

  static Map<String, Object> avroToMap(GenericRecord record) {
    avroToMap(record, false)
  }

  static Map<String, Object> avroToMap(GenericRecord record, Boolean recurse) {
    def fieldNames = record.schema.fields*.name()
    def collector = new LinkedHashMap<String, Object>(fieldNames.size())
    return fieldNames.inject(collector, { result, field ->
      def fieldValue = record.get(field)
      if (recurse && fieldValue instanceof GenericRecord) {
        result.put(field, avroToMap(fieldValue, recurse))
      }
      else if (recurse && fieldValue instanceof Collection) {
        result.put(field, avroCollectionToList(fieldValue, recurse))
      }
      else {
        result.put(field, fieldValue)
      }
      return result
    })
  }

  static List<Map> avroCollectionToList(Collection collection) {
    avroCollectionToList(collection, false)
  }

  static List<Map> avroCollectionToList(Collection collection, Boolean recurse) {
    return collection.collect {
      it instanceof GenericRecord ? avroToMap(it, recurse) : it
    }
  }

}
