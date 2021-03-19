tasks.getByName("clean") {
    dependsOn("npm_run_clean")
}

tasks.getByName("jibDockerBuild") {
    dependsOn("npm_install")
}

tasks.getByName("assemble"){
    dependsOn("npm_install")
}

jib {
    val publish: Publish by project.extra

//    copy the entire js project until we use webpack
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
        ports = listOf("3000")
        entrypoint = listOf("node", "src/app.js")

    }
}