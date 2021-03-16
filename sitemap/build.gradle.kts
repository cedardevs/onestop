tasks.getByName("jibDockerBuild") {
    dependsOn("npm_install")
}

tasks.getByName("build"){
    dependsOn("jibDockerBuild")
}

jib {
    val publish: Publish by project.extra

    extraDirectories.setPaths(mutableListOf(File("../sitemap")))

    from {
        // base image
        //distroless didnt have node
        image = "node"
    }
    to {
        image = publish.repository()
        auth {
            username = publish.username
            password = publish.password
        }
    }
    container {
        creationTime = publish.created
        labels = publish.ociAnnotations()
        //fire up express server (and maybe also kick off generator.js)
//        entrypoint = listOf("node", "src/generator.js")
        entrypoint = listOf("tail", "-f", "/dev/null")

    }
}