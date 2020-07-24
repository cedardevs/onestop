import com.sun.codemodel.JCodeModel

import org.gradle.api.DefaultTask
/* import org.gradle.api.tasks.Input */
import org.gradle.api.tasks.TaskAction

import org.jsonschema2pojo.DefaultGenerationConfig
import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.SchemaMapper
import org.jsonschema2pojo.rules.RuleFactory
import org.jsonschema2pojo.Jackson2Annotator
import org.jsonschema2pojo.SchemaStore
import org.jsonschema2pojo.SchemaGenerator
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.net.URL

import com.google.gson.Gson
import com.google.gson.JsonObject

open class ESMappingTask : DefaultTask() {

  fun buildJsonSchemaProperties(mappingObject: JsonObject): JsonObject {
    val properties = JsonObject()
    for (key in mappingObject.keySet()) {
      val prop = mappingObject.get(key).getAsJsonObject()
      if (prop.get("type").getAsString() == "text" || prop.get("type").getAsString() == "keyword") {
        val desc = JsonObject()
        desc.addProperty("type", "string")
        properties.add(key, desc)

        if (key == "dataFormat" || key == "linkProtocol" || key == "serviceLinkProtocol") {
          desc.addProperty("description", "DEPRECATED (see OpenAPI for details)")
        }

        if (key.endsWith("s") || key.startsWith("gcmd")) { // hack to make gcmdScience a list
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "long" ) {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("javaType", "java.lang.Long")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "byte") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("javaType", "java.lang.Byte")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "short") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("javaType", "java.lang.Short")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "float") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("javaType", "java.lang.Float")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "date") {
        val desc = JsonObject()
        desc.addProperty("type", "object")
        desc.addProperty("javaType", "java.time.ZonedDateTime")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "nested") {
        val desc = JsonObject()
        desc.addProperty("type", "object")
        // automatically makes java class in the correct package and name
        if(prop.get("properties").getAsJsonObject().keySet().contains("linkName")) {
          // hack it to only produce one link class instead of several
          desc.addProperty("javaType", "org.cedar.onestop.mapping.Link")
        }
        desc.add("properties", buildJsonSchemaProperties(prop.get("properties").getAsJsonObject()))
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "boolean") {
        val desc = JsonObject()
        desc.addProperty("type", "boolean")
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "geo_shape") {
        val desc = JsonObject()
        desc.addProperty("type", "object")
        /* // automatically makes java class in the correct package and name
        if(prop.get("properties").getAsJsonObject().keySet().contains("linkName")) {
          // hack it to only produce one link class instead of several
          desc.addProperty("javaType", "org.cedar.onestop.mapping.Link")
        }
        MAJOR MAJOR TODO GEOSHAPE. Existing POJO to use? Craft one??? depends on translating pojo back to doc to post to index, I think?
        desc.add("properties", buildJsonSchemaProperties(prop.get("properties").getAsJsonObject())) */
        properties.add(key, desc)
        if (key.endsWith("s")) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }  else {
        logger.lifecycle("WARNING: Unhandled property: ${key}:${prop}")
      }

    }
    return properties
  }

  @TaskAction
  fun update() {
    logger.lifecycle("Running 'updateESMapping' task on ${project.name}")
    val codeModel = JCodeModel()
    // create Gson instance for deserializing/serializing
    val gson = Gson()

    var source: String
    source = Files.readString(Path.of(project.projectDir.absolutePath + "/src/main/resources/mappings/search_collectionIndex.json"))

    // deserialize JSON file into JsonObject
    val mappingObject = gson.fromJson(source, JsonObject::class.java)
    val schemaObject = JsonObject()
    // TODO add property description - "mapping for index XYZ" ?
    schemaObject.addProperty("type", "object")
    schemaObject.addProperty("javaType", "org.cedar.onestop.mapping.SearchCollection")
    schemaObject.add("properties", buildJsonSchemaProperties(mappingObject.getAsJsonObject("mappings").getAsJsonObject("properties")))

    open class ESGenerationConfig() : DefaultGenerationConfig() {
      override fun isGenerateBuilders(): Boolean {
        return true
      }
      override fun isIncludeAdditionalProperties(): Boolean {
        return false
      }
      override fun isUseTitleAsClassname(): Boolean {
        return true
      }
      override fun isUseLongIntegers(): Boolean {
        return true
      }
      //isIncludeDynamicBuilders()
      //isIncludeDynamicAccessors()
      //isIncludeGetters()
      //isIncludeSetters()
      //	isUseInnerClassBuilders()

    }
    val config = ESGenerationConfig()

    val mapper = SchemaMapper(RuleFactory(config, Jackson2Annotator(config), SchemaStore()), SchemaGenerator())
    mapper.generate(codeModel, "ClassName", "com.example", gson.newBuilder().setPrettyPrinting().create().toJson(schemaObject))

    /* logger.lifecycle("trying to make dir ${project.projectDir.absolutePath + "/build/esGenerated"}") */
    val dest = Files.createDirectories(Paths.get(project.projectDir.absolutePath + "/build/esGenerated")).toFile()

    /* logger.lifecycle("writing generated files to ${dest}") */
    codeModel.build(dest)
  }
}
