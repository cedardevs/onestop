import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Defines a new task type which creates the docker image.
 * The advantage of making it a gradle task is that it will
 * track the inputs and outputs to determine if the task is
 * up to date or not
 */
class BuildDockerImageTask extends DefaultTask {

  // TODO make compiled jar and Dockerfile input that will be used for detecting changes
  // TODO make output a file with the hashid of the built image for tracking changes to output

  // TODO description: 'Creates a docker image with the current jar.', group: 'docker'

  @Optional
  @Input Map<String, String> additionalBuildArgs

  @Input String rootProjectName

  def getDateTime() {
    return new Date().format("YYYY-MM-DD'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone('UTC'))
  }

  @TaskAction download() {
    project.exec {
      // workingDir etlScriptsRoo
      executable = "bash"
      args = ["-c", "docker build --no-cache " +
          "--build-arg VCS_REF=\$(git rev-parse --short HEAD) " +
          "--build-arg VERSION=${project.version} " +
          "--build-arg DATE=${getDateTime()} " +
          additionalBuildArgs.collect {
            "--build-arg ${it.key}=${it.value} "
            }.join(" ") +
            "-t cedardevs/${rootProject.name}-${project.name}:${project.version} " +
            "-t cedardevs/${rootProject.name}-${project.name}:latest " +
            "-t cedardevs/${rootProject.name}-${project.name}:latest-SNAPSHOT " +
            "."
          ]
    }
  }

}
