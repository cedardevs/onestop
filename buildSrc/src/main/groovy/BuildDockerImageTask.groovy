import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Defines a new task type which creates the docker image.
 * The advantage of making it a gradle task is that it will
 * track the inputs and outputs to determine if the task is
 * up to date or not
 */
class BuildDockerImageTask extends DefaultTask {

  @Override
  String getDescription() {
    return 'Builds a docker image from the project directory'
  }

  @Override
  String getGroup() {
    return 'docker'
  }

  // TODO make compiled jar and Dockerfile input that will be used for detecting changes
  // TODO make output a file with the hashid of the built image for tracking changes to output

  @Optional @Input
  Map<String, String> additionalBuildArgs

  @TaskAction
  def buildImage() {
    def defaultArgs = [
        "docker build --no-cache",
        "--build-arg VCS_REF=\$(git rev-parse --short HEAD) ",
        "--build-arg VERSION=${project.version} ",
        "--build-arg DATE=${getDateTime()} ",
    ]
    def buildArgs = additionalBuildArgs.collect({"--build-arg ${it.key}=${it.value}"})
    def tagArgs = DockerTagUtils.getDockerTags(project).collect({"-t ${it}"})
    def finalArgs = ['.']
    def allArgs = defaultArgs + buildArgs + tagArgs + finalArgs
    def command = allArgs.join(' ')

    project.exec {
      executable = 'bash'
      args = ['-c', command]
    }
  }

  static getDateTime() {
    return new Date().format("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone('UTC'))
  }

}
