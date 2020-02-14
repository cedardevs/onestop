import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DockerHubAPI : ContainerRegistryInterface {
    override val url: String
        get() = "https://hub.docker.com/v2"

    override fun tags(publish: Publish): JSONArray {
        return requestPagedTags(publish)
    }

    override fun token(publish: Publish): String? {
        val urlToken = "${this.url}/users/login"
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

    override fun deleteTags(publish: Publish, tagsToDelete: List<String>): Boolean {
        val jwt: String? = this.token(publish)
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        tagsToDelete.forEach { tagToDelete ->
            val urlDelete = "${this.url}/repositories/${publish.vendor}/${publish.title}/tags/${tagToDelete}"
            val response: Response = khttp.delete(
                    url = urlDelete,
                    headers = mapOf(Pair("Authorization", "JWT ${jwt ?: ""}"))
            )
            println("DELETE STATUS CODE = " + response.statusCode)
            if(response.statusCode == 202) successes.add(tagToDelete) else failures.add(tagToDelete)
            return response.statusCode == 202
        }
        println("Cleaning DockerHub -> Successfully deleted tags:${successes.joinToString(prefix = "\n-", separator = "\n- ", postfix = "\n")}")
        println("Cleaning DockerHub -> Failed to delete tags:${failures.joinToString(prefix = "\n-", separator = "\n- ", postfix = "\n")}")
        return failures.size == 0
    }

    // request all public tags from Docker Hub (no credentials or token needed)
    private fun requestPagedTags(publish: Publish, url: String? = null, tags: JSONArray = JSONArray()): JSONArray {
        val urlTags = url ?: "${this.url}/repositories/${publish.vendor}/${publish.title}/tags"
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
        // recurse using the "next" page url of tags, concatenating tags as long as next page exists
        return this.requestPagedTags(publish, urlNext, this.concat(tags, jsonTagsRefined))
    }
}