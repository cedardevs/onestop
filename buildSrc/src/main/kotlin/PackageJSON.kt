import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileWriter

data class PackageJSON(
        val name: String,
        val version: String,
        val description: String,
        val author: String,
        val license: String,
        val homepage: String,
        val repositoryUrl: String,
        val repositoryDirectory: String,
        val bugsUrl: String
)

open class PackageJSONTask : DefaultTask() {

    @Input
    var updates: PackageJSON? = null

    // utilities to help keep values in package.json files up-to-date with gradle values
    private fun updatePackageJSON(filePath: String, packageJSON: PackageJSON?): Boolean {

        // nothing given to update
        if(packageJSON == null) {
            return false
        }

        // create Gson instance for deserializing/serializing
        val gson = Gson()

        try {
            // read the JSON file as a string
            val jsonString: String = File(filePath).readText(Charsets.UTF_8)

            // deserialize JSON file into JsonObject
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

            // update root-level keys
            jsonObject.addProperty("name", packageJSON.name)

            // Node package version must be formatted as strict semantic version,
            // so if it's not valid, we fall back the package.json version to '0.0.0'.
            // This shouldn't be the case for a valid tag/release; however, we don't
            // publish to a public NPM registry during builds, anyway.
            val semVerPattern: Regex = "[1-9]\\d*\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()
            val isSemVer: Boolean = semVerPattern.matches(packageJSON.version)
            val semVer: String = if(isSemVer) packageJSON.version else "0.0.0"

            jsonObject.addProperty("version", semVer)
            jsonObject.addProperty("description", packageJSON.description)
            jsonObject.addProperty("author", packageJSON.author)
            jsonObject.addProperty("license", packageJSON.license)
            jsonObject.addProperty("homepage", packageJSON.homepage)

            // update keys under 'repository' section
            val repository: JsonObject = jsonObject.getAsJsonObject("repository")

            repository.addProperty("url", packageJSON.repositoryUrl)
            repository.addProperty("directory", packageJSON.repositoryDirectory)

            // update keys under 'bugs' section
            jsonObject.getAsJsonObject("bugs").addProperty("url", packageJSON.bugsUrl)

            // re-serialize updated JSON and write back to original file with pretty formatting
            FileWriter(filePath, Charsets.UTF_8).use { writer ->
                gson.newBuilder().setPrettyPrinting().create().toJson(jsonObject, writer)
            }
        }
        catch(e: Exception) {
            // if anything goes wrong in the process, return false
            return false
        }
        return true
    }

    @TaskAction
    fun update() {
        logger.lifecycle("Running 'updatePackageJSON' task on ${project.name}")
        val packageJsonPath = project.projectDir.absolutePath + "/package.json"
        logger.warn("packageJsonPath = $packageJsonPath")
        if(!updatePackageJSON(packageJsonPath, updates)) {
            logger.warn("'WARNING: package.json' was NOT updated in '${project.name}'")
        }
    }
}