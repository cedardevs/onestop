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
        val (projectId, repositoryId) = this.projectAndRepositoryIds(publish)
        if(projectId > -1 && repositoryId > -1) {
            val urlTags = "${this.url}/projects/$projectId/registry/repositories/${repositoryId}/tags"
            val responseTags: Response = khttp.get(urlTags)
            val jsonResponse: JSONArray = responseTags.jsonArray
            return this.tagNamesFromResponseArray(jsonResponse, "name")
        }
        return JSONArray()
    }

    override fun token(publish: Publish): String? {
        return System.getenv("GITLAB_ACCESS_TOKEN")
    }

    override fun deleteTags(publish: Publish, tagsToDelete: List<String>): Boolean {
        val successes = mutableListOf<String>()
        val failures = mutableListOf<String>()
        val (projectId, repositoryId) = this.projectAndRepositoryIds(publish)
        if(projectId > -1 && repositoryId > -1) {
            val accessToken: String? = this.token(publish)
            tagsToDelete.forEach { tagToDelete ->
                val urlDelete = "${this.url}/projects/${projectId}/registry/repositories/${repositoryId}/tags/${tagToDelete}"
                val response: Response = khttp.delete(
                        url = urlDelete,
                        headers = mapOf(Pair("PRIVATE-TOKEN", "${accessToken ?: ""}"))
                )
                val successfulDelete = response.statusCode == 200
                if (successfulDelete) successes.add(tagToDelete) else failures.add(tagToDelete)
            }
        }
        else {
            println("Failed to retrieve project ID and repository ID from GitLab API!")
        }
        println("Cleaning GitLab -> Successfully deleted tags:${successes.joinToString(prefix = "\n- ", separator = "\n- ", postfix = "\n")}")
        println("Cleaning GitLab -> Failed to delete tags:${failures.joinToString(prefix = "\n- ", separator = "\n- ", postfix = "\n")}")
        return failures.size == 0
    }

    data class GitLabIds(val projectId: Int, val repositoryId: Int)
    private fun projectAndRepositoryIds(publish: Publish): GitLabIds {

        // retrieve project ID by project name
        var projectId: Int = -1
        val urlProject = "${this.url}/users/${publish.vendor}/projects?search=${publish.project}"
        val responseProject: Response = khttp.get(urlProject)
        val jsonProjects: JSONArray = responseProject.jsonArray
        if (jsonProjects.length() > 0) {
            val jsonProject: JSONObject = jsonProjects.getJSONObject(0)
            projectId = jsonProject.getInt("id")
        }

        // retrieve repository ID with knowledge of project ID
        var repositoryId: Int = -1
        if(projectId > -1) {
            val urlRepositories = "${this.url}/projects/${projectId}/registry/repositories"
            val responseRepositories: Response = khttp.get(urlRepositories)
            val jsonRepositories: JSONArray = responseRepositories.jsonArray

            val jsonRepository: JSONObject? = jsonRepositories.find { r ->
                val repo: JSONObject = r as JSONObject
                repo.get("name") == publish.title
            } as JSONObject

            if (jsonRepository !== null) {
                repositoryId = jsonRepository.getInt("id")
            }
        }
        return GitLabIds(projectId, repositoryId)
    }
}