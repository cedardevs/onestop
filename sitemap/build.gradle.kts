tasks.getByName("jibDockerBuild") {
    dependsOn("npm_install")
}

tasks.getByName("build"){
    dependsOn("jibDockerBuild")
}

jib {
    val publish: Publish by project.extra
    val jibExtraDir: String by project.extra
    //copy src
    //copy node_modules
    extraDirectories.setPaths(File(jibExtraDir))

    from {
        // base image
        image = "gcr.io/distroless/nodejs:14"
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
        entrypoint = listOf("node", "server.js")
    }
}