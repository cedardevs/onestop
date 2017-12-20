import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class PublishDockerImageTask extends DefaultTask {

  @Override
  String getDescription() {
    return 'Publishes the docker image(s) for the project'
  }

  @Override
  String getGroup() {
    return 'publish'
  }

  @TaskAction
  def publish() {
    def isRelease = project.isRelease as Boolean // from root project
    def tags = DockerTagUtils.getDockerTags(project, isRelease)
    def commands = ["docker login -u \$DOCKER_USER -p \$DOCKER_PASSWORD"] +
        tags.collect({"docker push ${it}"}) +
        ["docker logout"]
    def joinedCommand = commands.join(' && ')

    project.exec {
      executable = 'bash'
      args = ['-c', joinedCommand]
    }
  }

}
