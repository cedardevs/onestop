import org.gradle.api.Project

class DockerTagUtils {

  private static final DOCKER_ORG = 'cedardevs'

  /**
   * Breaks a version number down by truncating it at each of it's levels
   * e.g. '1.2.3' becomes ['1', '1.2', '1.2.3']
   * @param number The version number
   * @return A list of truncated version parts
   */
  private static List<String> truncatedVersionParts(String number) {
    def result = [number]
    def lastDot = number.lastIndexOf('.')
    return lastDot == -1 ? result : result + truncatedVersionParts(number.substring(0, lastDot))
  }

  /**
   * Pulls any suffix off the version string, uses truncatedVersionParts
   * to break the number down, then re-applies the suffix to each part.
   * e.g. '1.2.3-X' becomes ['1-X', '1.2-X', '1.2.3-X']
   * @param version The full version string
   * @return A list of truncated, but still suffixed, version strings
   */
  private static getVersionVariants(String version) {\
    def matcher = version =~ /([^-]+)(-.+)?/
    def number = matcher[0][1] as String
    def suffix = matcher[0][2] ?: ''
    return truncatedVersionParts(number).collect({it + suffix})
  }

  static getDockerTags(Project project, Boolean includeLatest = false) {
    def repositoryName = "${DOCKER_ORG}/${project.rootProject.name}-${project.name}"
    def latestVersions = includeLatest ? ['latest'] : []
    def allVersions = latestVersions + getVersionVariants(project.version as String)
    return allVersions.collect({"${repositoryName}:${it}"})
  }

}
