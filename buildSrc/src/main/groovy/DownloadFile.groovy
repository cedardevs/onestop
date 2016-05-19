import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Defines a new task type which simply downloads a file.
 * The advantage of making it a gradle task is that it will
 * track the inputs and outputs to determine if the task is
 * up to date or not
 */
class DownloadTask extends DefaultTask {

  @Input String url
  @Input String targetDir
  @OutputFile File getTargetFile() {
    new File("${targetDir}/${url.tokenize('/')[-1]}")
  }

  @TaskAction download() {
    targetFile.parentFile.mkdirs()
    new URL(url).withInputStream { is ->
      targetFile.withOutputStream { os ->
        os << is
      }
    }
  }

}