import org.json.JSONArray
import org.json.JSONObject

interface ContainerRegistryInterface {

    // URL to the container registry API being used
    val url: String

    // JSON array of objects with form:
    // [{ "name": "${tag}", "last_updated": "${last_updated}" }, ...]
    fun tags(publish: Publish): JSONArray

    // Optional string describing the token used to make API calls
    fun token(publish: Publish): String?

    // Using the publish data about this project and a given tag, delete that tag from the container registry
    fun deleteTags(publish: Publish, tagsToDelete: List<String>): Boolean

    // if we abstract away how to retrieve and delete tags by name
    // then the clean can be implemented generically at the interface level
    fun clean(publish: Publish): Boolean {

        // retrieve the remote origin branches
        val activeBranches: Set<String> = this.activeBranches()

        // retrieve all tags from container registry for this project
        // [{ "name": "${tag}", "last_updated": "${last_updated}" }, ...]
        val tags: JSONArray = this.tags(publish)

        // collect lists of tags matching certain predicates
        val (releases, snapshotsActive, snapshotsInactive, local, dangling) = this.tagGroups(tags, activeBranches)

        println("\nALL TAGS    [${tags.length()}]:" + this.printTagNames(tags))
        println("\nRELEASES    [${releases.length()}]:" + this.printTagNames(releases))
        println("\nSS ACTIVE   [${snapshotsActive.length()}]:" + this.printTagNames(snapshotsActive))
        println("\nSS INACTIVE [${snapshotsInactive.length()}]:" + this.printTagNames(snapshotsInactive))
        println("\nLOCAL       [${local.length()}]:" + this.printTagNames(local))
        println("\nDANGLING    [${dangling.length()}]:" + this.printTagNames(dangling))

        // delete all the inactive snapshots and dangling tags
        val delete = this.concat(snapshotsInactive, dangling)
        println("\nDELETE      [${delete.length()}]:" + this.printTagNames(delete))

        return this.deleteTags(publish, this.tagNames(delete))
    }

    // Retrieve active branches of the project git repo (private: shouldn't be impl differently between git repos)
    private fun activeBranches(): Set<String> {
        return "for branch in `git branch -r | grep -v HEAD`; do printf  \"%s\\n\" \"\${branch#\"origin/\"}\"; done | sort -r"
                .runShell()?.lines()?.toSet() ?: setOf()
    }

    // group all the tags published on Docker Hub in a way that we can retain or purge tags for cleanup
    private data class TagGroups(val releases: JSONArray, val snapshotsActive: JSONArray, val snapshotsInactive: JSONArray, val local: JSONArray, val dangling: JSONArray)
    private fun tagGroups(tags: JSONArray, activeBranches: Set<String>): TagGroups {

        val releases = JSONArray()
        val snapshotsActive = JSONArray()
        val snapshotsInactive = JSONArray()
        val local = JSONArray()
        val dangling = JSONArray()

        tags.forEach {t ->
            val tag: JSONObject = t as JSONObject
            val name = tag.getString("name")

            val isRelease = isSemanticNonSnapshot(name)
            val isSnapshot = isSnapshot(name)

            // if `activeBranches` is empty, it indicates that something went wrong retrieving the remote branches
            // it should at the very least contain the `master` branch, so we keep all snapshots to be safe
            val shouldKeepSnapshot = activeBranches.isEmpty() || activeBranches.contains(name.removeSuffix(SUFFIX_SNAPSHOT))

            val isSnapshotActive = isSnapshot && shouldKeepSnapshot
            val isSnapshotInactive = isSnapshot && !shouldKeepSnapshot

            val isLocal = isLocalNonSnapshot(name)

            if(isRelease) {
                releases.put(tag)
                return@forEach
            }
            if(isSnapshotActive) {
                snapshotsActive.put(tag)
                return@forEach
            }
            if(isSnapshotInactive) {
                snapshotsInactive.put(tag)
                return@forEach
            }
            if(isLocal) {
                local.put(tag)
                return@forEach
            }
            dangling.put(tag)
        }

        return TagGroups(releases, snapshotsActive, snapshotsInactive, local, dangling)
    }

    fun tagNamesFromResponseArray(jsonResponse: JSONArray, key: String): JSONArray {
        val jsonTags = JSONArray()
        jsonResponse.forEach { t ->
            val tag: JSONObject = t as JSONObject
            val name = tag.getString(key)
            val refinedTag = JSONObject(mapOf(Pair("name", name)))
            jsonTags.put(refinedTag)
        }
        return jsonTags
    }

    fun tagNames(tags: JSONArray): List<String> {
        return tags.map { t ->
            val tag: JSONObject = t as JSONObject
            tag.getString("name")
        }
    }

    fun printTagNames(tags: JSONArray): String {
        return this.tagNames(tags).joinToString(prefix = "\n- ", separator = "\n- ", postfix = "\n")
    }

    // used to concatenate JSONArray objects in the recursive calls paging through Docker Hub tags
    fun concat(vararg arrays: JSONArray): JSONArray {
        val result = JSONArray()
        for (array in arrays) {
            array.forEachIndexed { index, _ ->
                result.put(array.get(index))
            }
        }
        return result
    }
}