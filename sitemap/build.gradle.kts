tasks.getByName("jibDockerBuild") {
    dependsOn("npm_install")
}

tasks.getByName("clean") {
    dependsOn("npm_run_clean")
}

tasks.getByName("assemble"){
    dependsOn("jibDockerBuild")
}

tasks.getByName("npm_run_build") {
    inputs.dir("${projectDir}/src")
}

jib {
    val publish: Publish by project.extra

    extraDirectories.setPaths(mutableListOf(File("../sitemap")))

    from {
        // base image
        //distroless didnt have node
        image = "node:15.11.0-alpine3.10"
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
        entrypoint = listOf("node", "src/app.js")

    }
}