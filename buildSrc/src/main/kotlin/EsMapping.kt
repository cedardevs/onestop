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
import java.io.File
import java.net.URL
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray

open class ESMappingTask : DefaultTask() {

  fun keyIndicatesArray(key:String) :Boolean {
    return key == "errors" || key == "links" || key == "checksums"  || key == "dataFormats" || key == "serviceLinks" || key == "legalConstraints" || key == "citeAsStatements" || key == "largerWorks" || key == "crossReferences"
  }

  fun keyIndicatesSet(key:String) :Boolean {
    return key == "individualNames" || key == "organizationNames" || key == "dataFormat" || key == "linkProtocol" || key == "serviceLinkProtocol" || key == "keywords" || key.startsWith("gcmd")
  }

  fun buildJsonSchemaProperties(mappingObject: JsonObject, isGranule:Boolean, overrideNoArrays:Boolean): JsonObject {
    val properties = JsonObject()
    for (key in mappingObject.keySet()) {
      val prop = mappingObject.get(key).getAsJsonObject()
      if (prop.get("type") == null || prop.get("type").getAsString() == "nested") {
        val desc = JsonObject()
        val childOverrideNoArrays : Boolean
        desc.addProperty("type", "object")
        // automatically makes java class in the correct package and name
        if(prop.get("properties").getAsJsonObject().keySet().contains("linkName")) {
          // hack it to only produce one link class instead of several
          desc.addProperty("javaType", "org.cedar.onestop.mapping.Link")
          childOverrideNoArrays = true
        } else {
          childOverrideNoArrays = overrideNoArrays
        }
        if(key == "crossReferences" || key == "largerWorks") {
          desc.addProperty("javaType", "org.cedar.onestop.mapping.search.Reference")
        }
        if(key == "identification") { // note this is the only place where the 2 analysisError indices differ
          if (isGranule) {
            desc.addProperty("javaType", "org.cedar.onestop.mapping.analysis.GranuleIdentification")

          } else {
            desc.addProperty("javaType", "org.cedar.onestop.mapping.analysis.CollectionIdentification")

          }
        }
        desc.add("properties", buildJsonSchemaProperties(prop.get("properties").getAsJsonObject(), isGranule, childOverrideNoArrays)) // TODO overrideNoArrays hack to prevent Link object getting called with linkProtocol as a list...
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "text" || prop.get("type").getAsString() == "keyword") {
        val desc = JsonObject()
        desc.addProperty("type", "string")
        properties.add(key, desc)

        if (key == "dataFormat" || key == "linkProtocol" || key == "serviceLinkProtocol") {
          desc.addProperty("description", "DEPRECATED (see OpenAPI for details)")
        }

        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "long" ) {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("existingJavaType", "java.lang.Long")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "byte") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("existingJavaType", "java.lang.Byte")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "short") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("existingJavaType", "java.lang.Short")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      } else if (prop.get("type").getAsString() == "float") {
        val desc = JsonObject()
        desc.addProperty("type", "object") // using "object" instead of "integer" because I can specify javaType which is more specific than making everything a long
        desc.addProperty("existingJavaType", "java.lang.Float")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "date") {
        val desc = JsonObject()
        desc.addProperty("type", "object")
        desc.addProperty("existingJavaType", "java.lang.String")
        if(key == "stagedDate") {
          desc.addProperty("existingJavaType", "java.lang.Long")
        }
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      else if (prop.get("type").getAsString() == "boolean") {
        val desc = JsonObject()
        desc.addProperty("type", "boolean")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }
      /* else if (prop.get("type").getAsString() == "half_float") {
        // TODO figure out what type to use!!!
        // impacts description and titles reading scores
      } */
      else if (prop.get("type").getAsString() == "geo_shape") {
        val desc = JsonObject()
        desc.addProperty("type", "object")
        // TODO add mapbox Geometry to schemas instead of Object? desc.addProperty("existingJavaType", "com.mapbox.geojson.Geometry")
        // TODO note that having it directly use the avro generated pojos isn't working
        desc.addProperty("existingJavaType", "java.lang.Object")
        properties.add(key, desc)
        if (!overrideNoArrays && keyIndicatesArray(key)) {
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.add("items", desc)
          properties.add(key, arr)
        }
        if (!overrideNoArrays && keyIndicatesSet(key)) { // TODO rename overrideNoArrays to overrideNoCollections
          val arr = JsonObject()
          arr.addProperty("type", "array")
          arr.addProperty("uniqueItems", true) // turns array into set
          arr.add("items", desc)
          properties.add(key, arr)
        }
      }  else {
        logger.lifecycle("WARNING: Unhandled property: ${key}:${prop}")
      }

    }
    return properties
  }

  fun generateClasses(filename:String, mapper:SchemaMapper, dest: File) {
    var source: String
    source = Files.readString(Path.of(filename))
    // create Gson instance for deserializing/serializing
    val gson = Gson()
    // deserialize JSON file into JsonObject
    val mappingObject = gson.fromJson(source, JsonObject::class.java)
    val schemaObject = JsonObject()
    schemaObject.addProperty("type", "object")
    val index = filename.replace(Regex(".*/"),"").replace("Index.json","").replace("_"," ")
    schemaObject.addProperty("description", "Mapping for ${index} index.")
    val classname = filename.replace(Regex(".*/"),"").replace("Index.json","").splitToSequence("_").map { it.capitalize() }.joinToString("")
    val packagename = filename.replace(Regex(".*/"),"").replace("Index.json","").split("_")[0] // use the naming convention to loosely package related index code - analysis has a different SpatialBounding than search, but all the search indices *should* share a SpatialBounding, as should all the analysis indices.
    if (packagename == "search") {
      val interfaces = JsonArray()

      /* interfaces.add("org.cedar.onestop.mapping.search.SearchObject" as String) */
      interfaces.add("org.cedar.onestop.mapping.search.SearchObjectWithDates" as String)
      interfaces.add("org.cedar.onestop.mapping.search.SearchObjectWithKeywords" as String)
      if (classname != "SearchGranule"){
        interfaces.add("org.cedar.onestop.mapping.search.SearchObjectWithResponsibleParties" as String)
      }


        /* interfaces.add("org.cedar.onestop.mapping.search.SearchObjectExpanded" as String) */
      schemaObject.add("javaInterfaces", interfaces)

    }


    logger.lifecycle("Generating $classname")
    /* schemaObject.addProperty("javaType", "org.cedar.onestop.mapping.${classname}") */
    schemaObject.add("properties", buildJsonSchemaProperties(mappingObject.getAsJsonObject("mappings").getAsJsonObject("properties"), filename.contains("granule"), false))
    val codeModel = JCodeModel()

    mapper.generate(codeModel, classname, "org.cedar.onestop.mapping.${packagename}", gson.newBuilder().setPrettyPrinting().create().toJson(schemaObject))
    codeModel.build(dest) // TODO multiple mappings to dest means last-one-in clobbers other objects (in theory this is fine because they should be the same across mappings but.... who knows)

  }

  @TaskAction
  fun update() {
    logger.lifecycle("Running 'updateESMapping' task on ${project.name}")

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

    /* logger.lifecycle("trying to make dir ${project.projectDir.absolutePath + "/build/esGenerated"}") */
    val dest = Files.createDirectories(Paths.get(project.projectDir.absolutePath + "/build/esGenerated")).toFile()
    /* logger.lifecycle("writing generated files to ${dest}") */
    var dir = File(project.projectDir.absolutePath + "/src/main/resources/mappings/")
    for (f in dir.listFiles()) {
      /* logger.lifecycle("mappings: ${f} ${f.getAbsolutePath​()}") */
      /* logger.lifecycle("??? ${f.getPath​()}") */
      val filename : String
      filename = f.getPath()
      logger.lifecycle(filename)
      generateClasses(filename, mapper, dest)
      /* val tmp = f.getAbsolutePath(::)
      logger.lifecycle("???: ${tmp}")
      generateClasses("asdf", mapper, dest) */

    }

  }
}
