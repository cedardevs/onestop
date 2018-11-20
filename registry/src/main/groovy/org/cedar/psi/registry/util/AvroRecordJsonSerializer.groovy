package org.cedar.psi.registry.util


import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.avro.generic.GenericContainer
import org.cedar.psi.common.util.AvroUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AvroRecordJsonSerializer extends StdSerializer<GenericContainer> {

  @Autowired
  AvroRecordJsonSerializer(ObjectMapper objectMapper) {
    super(GenericContainer)

    def module = new SimpleModule()
    module.addSerializer(GenericContainer, this)
    objectMapper.registerModule(module)
  }

  @Override
  void serialize(GenericContainer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeObject(AvroUtils.avroToMap(value))
  }
}
