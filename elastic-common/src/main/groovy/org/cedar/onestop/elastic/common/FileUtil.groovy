package org.cedar.onestop.elastic.common

import groovy.util.logging.Slf4j

@Slf4j
class FileUtil {

  static String textFromFile(String filename) {
    // return file as JSON string
    ClassLoader classLoader = Thread.currentThread().contextClassLoader
    InputStream fileStream = classLoader.getResourceAsStream(filename)
    if (fileStream) {
      return fileStream.text
    }
    return null
  }

}
