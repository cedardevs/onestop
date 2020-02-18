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
            val successfulDelete = response.statusCode == 204
            if(successfulDelete) successes.add(tagToDelete) else failures.add(tagToDelete)
        }
        // logout to prevent this token from being used again
        this.logout(jwt)

        println("Cleaning DockerHub -> Successfully deleted tags:${successes.joinToString(prefix = "\n- ", separator = "\n- ", postfix = "\n")}")
        println("Cleaning DockerHub -> Failed to delete tags:${failures.joinToString(prefix = "\n- ", separator = "\n- ", postfix = "\n")}")
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

        val jsonTags: JSONArray = jsonResponse.getJSONArray("results")
        val jsonTagsRefined = this.tagNamesFromResponseArray(jsonTags, "name")

        // recurse using the "next" page url of tags, concatenating tags as long as next page exists
        val combinedTags = this.concat(tags, jsonTagsRefined)
        return if (urlNext.isBlank()) combinedTags else this.requestPagedTags(publish, urlNext, combinedTags)
    }

    // invalidate the requested token obtained the user credentials in the `token` function, as good practice
    private fun logout(jwt: String?) {
        val urlLogout = "${this.url}/logout"
        val response: Response = khttp.post(
                url = urlLogout,
                headers = mapOf(Pair("Authorization", "JWT ${jwt ?: ""}"))
        )
        println("${if (response.statusCode == 200) "Successful" else "Failed"} logout from DockerHub using JWT")
    }
}