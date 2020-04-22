package org.cedar.onestop.api.search.controller

import groovy.util.logging.Slf4j
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

@Slf4j
class SchemaParser {

  private def schema
  public final String name

  public SchemaParser(String schemaName) {
      this.name = schemaName
  }

  void loadSpec() {
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
    JsonNode apiSpec = yamlMapper.readTree(this.getClass().classLoader.getResource('static/openapi.yaml').text)
    JsonNode schemaJson = apiSpec.get('components').get('schemas').get(this.name)
    ObjectNode schemaJsonObj = (ObjectNode)schemaJson

    JsonNode ref = schemaJson.findValue('$ref')
    while(ref != null) {
      def paths = ref.textValue().replace('#/', '').split('/')
      JsonNode deref = apiSpec
      paths.each{
        deref = deref.get(it)
      }
      ObjectNode parent = schemaJsonObj.findParent('$ref')
      parent.remove('$ref')
      parent.setAll(deref)
      ref = schemaJson.findValue('$ref')
    }

    final factory = JsonSchemaFactory.byDefault()
    log.info("Creating schema for ${this.name}")
    this.schema = factory.getJsonSchema(schemaJsonObj)
  }

  void checkSpec() {
    if (this.schema == null) {
      loadSpec()
    }
  }

  public def validate(def request) {
    checkSpec()
    return this.schema.validate(request)
  }
}
