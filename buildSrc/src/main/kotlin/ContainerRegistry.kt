import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*


object RegistryAPI {
    const val DOCKER_HUB = "https://hub.docker.com/v2"
    const val GITLAB = "https://gitlab.com/api/v4"
}

fun concat(vararg arrays: JSONArray): JSONArray {
    val result = JSONArray()
    for (array in arrays) {
        array.forEachIndexed { index, _ ->
            result.put(array.get(index))
        }
    }
    return result
}

fun requestDockerHubToken(publish: Publish): String? {
    val urlToken = "${RegistryAPI.DOCKER_HUB}/users/login"
    val payload = mapOf(Pair("username", publish.username), Pair("password", publish.password))
    val response: Response = khttp.post(
            url = urlToken,
            headers = mapOf(Pair("Content-Type", "application/json")),
            data = JSONObject(payload)
    )
    if (response.statusCode == 200) {
        val jsonResponse: JSONObject = response.jsonObject
        return jsonResponse.getString("token")
    }
    return null
}

fun parseDateISO(date: String): Date {
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    val accessor: TemporalAccessor = timeFormatter.parse(date)
    return Date.from(Instant.from(accessor))
}

fun requestDockerHubTags(publish: Publish, url: String? = null, tags: JSONArray = JSONArray()): JSONArray {
    val urlTags = url ?: "${RegistryAPI.DOCKER_HUB}/repositories/${publish.vendor}/${publish.title}/tags"
    val response: Response = khttp.get(urlTags)
    val jsonResponse: JSONObject = response.jsonObject
    val urlNext: String = try {
        jsonResponse.getString("next")
    } catch (e: JSONException) {
        ""
    } as String

    if (urlNext.isBlank()) return tags

    val jsonTags: JSONArray = jsonResponse.getJSONArray("results")
    val jsonTagsRefined = JSONArray()
    jsonTags.forEach { t ->
        val tag: JSONObject = t as JSONObject

        val name = tag.getString("name")
        val lastUpdated = tag.getString("last_updated")
        val refinedTag = JSONObject(mapOf(Pair("name", name), Pair("last_updated", lastUpdated)))
        jsonTagsRefined.put(refinedTag)
    }
    return requestDockerHubTags(publish, urlNext, concat(tags, jsonTagsRefined))
}

data class TagGroups(val releaseTags: JSONArray, val snapshotsInactive: JSONArray, val nonSemantic: JSONArray)

fun tagGroups(tags: JSONArray, numMostRecent: Int = 20, activeBranches: List<String>): TagGroups {

    val snapshotSuffix: String = "-SNAPSHOT"
    val semVerPattern: Regex = "[1-9]\\d*\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()
    val semVerMinor: Regex = "[1-9]\\d*\\.\\d+(?:-[a-zA-Z0-9]+)?".toRegex()
    val semVerMajor: Regex = "[1-9]\\d*(?:-[a-zA-Z0-9]+)?".toRegex()


    val releaseTags = JSONArray()
    val snapshotInactiveTags = JSONArray()
    val nonSemanticTags = JSONArray()
//    val obsoleteTags = JSONArray()

    val releases = tags.filter { t ->
        val tag: JSONObject = t as JSONObject
        val name = tag.getString("name")
        val isSemantic = semVerPattern.matches(name)
        val isSemanticMinor = semVerMinor.matches(name)
        val isSemanticMajor = semVerMajor.matches(name)
        val isSnapshot = name.endsWith(snapshotSuffix)


        (isSemantic || isSemanticMinor || isSemanticMajor) && !isSnapshot
    }

    // get `-SNAPSHOT` tags for branches which are no longer active
    // if there are no active branches to begin with, then we'll assume there's nothing inactive to clean
    val snapshotsInactive =
            if (activeBranches.isEmpty()) listOf()
            else tags.filter { t ->
                val tag: JSONObject = t as JSONObject
                val name = tag.getString("name")

                val isSnapshot = name.endsWith(snapshotSuffix)
                val isActive = activeBranches.contains(name)
                val isRelease = releases.contains(tag)

                isSnapshot && !isActive && !isRelease
            }

    // get semantically versioned tags (corresponding to official releases)
    val nonSemantic = tags.filter { t ->
        val tag: JSONObject = t as JSONObject
        val name = tag.getString("name")
//        val isSemantic = semVerPattern.matches(name)
        // be sure not to include things we've already identified as inactive snapshots
        val isInactiveSnapshot = snapshotsInactive.contains(tag)
        val isRelease = releases.contains(tag)
        !isRelease && !isInactiveSnapshot
    }

//    // sort tags by descending update date
//    val sorted = tags.sortedByDescending { t ->
//        val tag: JSONObject = t as JSONObject
//        val lastUpdated = tag.getString("last_updated")
//        val dateLastUpdated = parseDateISO(lastUpdated)
//        dateLastUpdated
//    }

//    // take the N most recent tags
//    val noneOld: Boolean = sorted.size <= numMostRecent
//    val new = sorted.subList(0, if (noneOld) sorted.size else numMostRecent)
//    val old = if (noneOld) listOf() else sorted.subList(numMostRecent, sorted.size)

//    // collect semantic and non semantic tags
//    val semantic = mutableListOf<Any>()
//    val nonSemantic = mutableListOf<Any>()
//    sorted.forEach { t ->
//        val tag: JSONObject = t as JSONObject
//        val name = tag.getString("name")
//        if (semVerPattern.matches(name)) semantic.add(tag) else nonSemantic.add(tag)
//    }

    // combine unique set of tags which are inactive snapshots, non-semantic versions, or both
//    val obsolete = old.union(nonSemantic)
//
//    // combine unique set of tags which are new, semantic, or both  -> but remove anything obsolete
//    val preserved = new.union(semantic)
//    val preservedNonObsolete = preserved.toMutableList()
//    preservedNonObsolete.removeAll(obsolete)

    // convert releases back into a JSONArray
    releases.forEach { t ->
        val tag: JSONObject = t as JSONObject
        releaseTags.put(tag)
    }

    // convert snapshots back into a JSONArray
    snapshotsInactive.forEach { t ->
        val tag: JSONObject = t as JSONObject
//        val remoteBranchExists: Boolean = remoteBranchExists(tag.getString("name"))
//        tag.put("remoteBranchExists", remoteBranchExists)
        snapshotInactiveTags.put(tag)
    }

    // convert results back into a JSONArray
    nonSemantic.forEach { t ->
        val tag: JSONObject = t as JSONObject
        nonSemanticTags.put(tag)
    }


    return TagGroups(releaseTags, snapshotInactiveTags, nonSemanticTags)
}

fun requestDockerHubRemoveTag(publish: Publish, tag: String): Boolean {
    val jwt: String? = requestDockerHubToken(publish)
    val urlDelete = "${RegistryAPI.DOCKER_HUB}/repositories/${publish.vendor}/${publish.title}/tags/${tag}"
    val response: Response = khttp.delete(
            url = urlDelete,
            headers = mapOf(Pair("Authorization", "JWT ${jwt ?: ""}"))
    )
    println("DELETE STATUS CODE = " + response.statusCode)
    return response.statusCode == 202
}

fun requestGitLabProjectId(publish: Publish): Int? {
    val urlProject = "https://gitlab.com/api/v4/users/${publish.vendor}/projects?search=${publish.project}"
    val responseProject: Response = khttp.get(urlProject)
    val jsonProjects: JSONArray = responseProject.jsonArray
    if (jsonProjects.length() > 0) {
        val jsonProject: JSONObject = jsonProjects.getJSONObject(0)
        return jsonProject.getInt("id")
    }
    return null
}

fun requestGitLabTags(publish: Publish): JSONArray {
    val projectId = requestGitLabProjectId(publish) ?: return JSONArray()
    val urlRepositories = "${RegistryAPI.GITLAB}/projects/${projectId}/registry/repositories?tags=true"
    val responseRepositories: Response = khttp.get(urlRepositories)
    val jsonRepositories: JSONArray = responseRepositories.jsonArray

    val jsonRepository: JSONObject? = jsonRepositories.find { r ->
        val repo: JSONObject = r as JSONObject
        repo.get("name") == publish.title
    } as JSONObject

    if (jsonRepository !== null) {
        return jsonRepository.getJSONArray("tags")
    }
    return JSONArray()
}

// utilities to help clean up the container registry we publish to
fun cleanContainerRegistry(publish: Publish, numMostRecent: Int = 20) {

    // retrieve the remote non-merged branches
    val activeBranches: List<String> = activeBranches()

    // if there are no remote non-merged branches, or we failed to retrieve them,
    // it is not safe to assume we can delete `-SNAPSHOT` images of those branches, so return
    if (activeBranches.isEmpty()) {
        return // if we can't determine
    }

    if (publish.registryUrl == Registries.DOCKER_HUB) {

        // retrieve all tags from docker hub for this project
        val tags: JSONArray = requestDockerHubTags(publish)

        // collect lists of tags matching certain predicates
        val (releases, snapshotsInactive, nonSemantic) = tagGroups(tags, numMostRecent, activeBranches)

        println("\n\nALL TAGS [${tags.length()}]:::\n\n" + tags.toString(2))

        println("\n\nRELEASES [${releases.length()}]:::\n\n" + releases.toString(2))
        println("\n\nSNAPSHOTS INACTIVE [${snapshotsInactive.length()}]:::\n\n" + snapshotsInactive.toString(2))
        println("\n\nNON-SEMANTIC [${nonSemantic.length()}]:::\n\n" + nonSemantic.toString(2))

//        println("\n\nINTERSECTS [${preserved.intersect(obsolete).size}]:::\n\n" + preserved.intersect(obsolete))

        val token: String? = requestDockerHubToken(publish)

        println("\n\nTOKEN:::\n\n$token")
    }

}