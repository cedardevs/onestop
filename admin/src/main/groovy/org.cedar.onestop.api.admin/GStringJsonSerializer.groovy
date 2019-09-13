package org.cedar.onestop.api.admin

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GStringJsonSerializer extends StdSerializer<GString> {

  @Autowired
  GStringJsonSerializer(ObjectMapper objectMapper) {
    super(GString)

    def module = new SimpleModule()
    module.addSerializer(GString, this)
    objectMapper.registerModule(module)
  }

  @Override
  void serialize(GString value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException, JsonProcessingException {
    gen.writeString(value.toString())
  }

}
