tasks.getByName("clean") {
    dependsOn("npm_run_clean")
}

task<com.moowork.gradle.node.npm.NpmTask>("formatCheck") {
    setArgs(mutableListOf("run", "formatCheck"))
}

task<com.moowork.gradle.node.npm.NpmTask>("retire") {
    setArgs(mutableListOf("run", "retire"))
}

tasks.getByName("check") {
    dependsOn("npm_run_test")
    dependsOn("formatCheck")
    dependsOn("retire")
}

tasks.getByName("test") {
    dependsOn("npm_run_test")
}

tasks.getByName("assemble") {
    dependsOn("npm_run_build")
    finalizedBy("tar")
}

tasks.getByName("jar") {
    // even though we use the `java` plugin because of the `jib` plugin
    // the node project does not need to generate a jar file (it creates a tar)
    enabled = false
}

task<Tar>("tar") {
    val publish: Publish by project.extra

    from(file("${buildDir}/webpack"))
    compression = Compression.GZIP
    archiveBaseName.set(publish.title)
    archiveExtension.set("tar.gz")
    archiveVersion.set("")
    archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
    destinationDirectory.set(file("${buildDir}/libs"))
}

// sync files to the client container jib staging directory
task<Sync>("sync") {

    val publish: Publish by project.extra
    val jibExtraDir: String by project.extra

    into(jibExtraDir)

    // untar and sync the client src
    into("/srv/www/${publish.title}") {
        dependsOn("tar") // can't untar without tar
        from(tarTree(file("${buildDir}/libs/${publish.title}.tar.gz")))
    }

    // copy the apache config
    into("/usr/local/apache2/conf") {
        from(file("docker/httpd.conf"))
    }

    // copy the entrypoint script
    into("/") {
        from(file("docker/entrypoint.sh"))
    }
}

// jib & jibDockerBuild rely on synced files in staging directory
tasks
        .matching { task -> task.name.startsWith("jib") }
        .configureEach { dependsOn("sync") }

jib {
    val publish: Publish by project.extra
    val jibExtraDir: String by project.extra

    extraDirectories.setPaths(File(jibExtraDir))

    from {
        // base image
        image = "httpd:latest"
    }
    to {
        image = repository(publish)
        auth {
            username = publish.username
            password = publish.password
        }
    }
    container {
        creationTime = publish.created
        labels = ociAnnotations(publish)
        ports = listOf("80")
        entrypoint = listOf("sh", "/entrypoint.sh")
    }
}