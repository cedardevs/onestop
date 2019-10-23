package org.cedar.onestop.registry.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.avro.generic.GenericRecord
import org.cedar.schemas.avro.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AvroRecordJsonSerializer extends StdSerializer<GenericRecord> {

  @Autowired
  AvroRecordJsonSerializer(ObjectMapper objectMapper) {
    super(GenericRecord)

    def module = new SimpleModule()
    module.addSerializer(GenericRecord, this)
    objectMapper.registerModule(module)
  }

  @Override
  void serialize(GenericRecord value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeObject(AvroUtils.avroToMap(value))
  }
}
