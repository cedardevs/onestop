package org.cedar.onestop.data.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

  public static String textFromClasspathFile(String filename) throws IOException {
    // return file as JSON string
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream fileStream = classLoader.getResourceAsStream(filename);
    if (fileStream != null) {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = fileStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }
      return result.toString(StandardCharsets.UTF_8.name());
    }
    return null;
  }
}
