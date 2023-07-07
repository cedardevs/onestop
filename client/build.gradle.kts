tasks.getByName("clean") {
    dependsOn("npm_run_clean")
}

task<com.moowork.gradle.node.npm.NpmTask>("formatCheck") {
    setArgs(mutableListOf("run", "formatCheck"))
}

task<com.moowork.gradle.node.npm.NpmTask>("retire") {
    if (gitLabCICD) {
      setArgs(mutableListOf("run", "retireGitLabCICD"))
    } else {
      setArgs(mutableListOf("run", "retire"))
    }
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
}

tasks.getByName("npm_run_build") {
    inputs.dir("${projectDir}/src")
}

tasks.getByName("jar") {
    // even though we use the `java` plugin because of the `jib` plugin
    // the node project does not need to generate a jar file (it creates a tar)
    enabled = false
}

task<Tar>("tar") {
    val publish: Publish by project.extra

    dependsOn("npm_run_build")

    from(file("${buildDir}/webpack"))
    fileMode = 484 // u+rwx,go+r,o+r   = 0744 hex = 484 decimal = 0b111100100
    dirMode =  493 // u+rwx,go+rx,o+rx = 0755 hex = 493 decimal = 0b111101101
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

    dependsOn("tar")

    into(jibExtraDir)

    // untar and sync the client src
    into("/usr/share/nginx/html") {
        dependsOn("tar") // can't untar without tar
        from(tarTree(file("${buildDir}/libs/${publish.title}.tar.gz")))
    }
    into("/etc/nginx/conf.d") {
        from(file("${projectDir}/conf.d"))
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
        image = "nginx:1.12-alpine"
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
        ports = listOf("80")
        entrypoint = listOf("nginx", "-g", "daemon off;")
    }
}