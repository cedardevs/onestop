tasks.getByName("clean") {
    dependsOn("npm_run_clean")
}

tasks.getByName("jibDockerBuild") {
    dependsOn("npm_install")
}

tasks.getByName("assemble"){
    dependsOn("npm_install")
}

task<Sync>("sync") {
    val jibExtraDir: String by project.extra

    into(jibExtraDir)

    into("/src") {
        from(file("${projectDir}/src"))
    }
    into("/node_modules") {
        from(file("${projectDir}/node_modules"))
    }
}

tasks
        .matching { task -> task.name.startsWith("jib") }
        .configureEach { dependsOn("sync") }

jib {
    val publish: Publish by project.extra
    val jibExtraDir: String by project.extra

//    copy the entire js project until we use webpack
    extraDirectories.setPaths(File(jibExtraDir))

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