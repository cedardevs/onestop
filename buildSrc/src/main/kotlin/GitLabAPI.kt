import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject

// TODO: We currently aren't using GitLab as a container registry, but for our publishing rules and cleanup to work
//       properly, we will need to work out the details here and conditionally call these methods instead of DockerHub.
//       We would also need to ensure the same `git` commands (to retrieve active branches, for example) are also working

class GitLabAPI : ContainerRegistryInterface {
    override val url: String
        get() = "https://gitlab.com/api/v4"

    override fun tags(publish: Publish): JSONArray {
        val projectId = this.projectId(publish) ?: return JSONArray()
        val urlRepositories = "${this.url}/projects/${projectId}/registry/repositories?tags=true"
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

    override fun token(publish: Publish): String? {
        TODO("not implemented")
    }

    override fun deleteTags(publish: Publish, tagsToDelete: List<String>): Boolean {
        TODO("not implemented")
    }

    private fun projectId(publish: Publish): Int? {
        val urlProject = "${this.url}/users/${publish.vendor}/projects?search=${publish.project}"
        val responseProject: Response = khttp.get(urlProject)
        val jsonProjects: JSONArray = responseProject.jsonArray
        if (jsonProjects.length() > 0) {
            val jsonProject: JSONObject = jsonProjects.getJSONObject(0)
            return jsonProject.getInt("id")
        }
        return null
    }
}