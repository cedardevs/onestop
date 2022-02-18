package io.forgo.plugins.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SSLKeyTask extends DefaultTask {

  @Internal
  @Override
  public String getDescription() {
    return "Builds an SSL private key file for the Keystore Gradle Plugin";
  }

  @Internal
  @Override
  public String getGroup() {
    return "Keystore Gradle Plugin";
  }

  private String outputDir;
  private String keyFile;
  private String keyPassword;

  @TaskAction
  void generateKey() throws IOException {

    String pathKeyFile = this.outputDir + File.separatorChar + this.keyFile;

    // execute openssl cmd to create private key
    getProject().exec(execSpec -> {
      execSpec.setIgnoreExitValue(true);
      execSpec.workingDir(".");
      execSpec.setExecutable("openssl");
      List<String> args = Arrays.asList(
        "genrsa",
        "-des3",
        "-out", pathKeyFile,
        "-passout", "pass:"+this.keyPassword
      );
      execSpec.setArgs(args);
    });
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  public void setKeyFile(String keyFile) {
    this.keyFile = keyFile;
  }


  public void setKeyPassword(String keyPassword) {
    this.keyPassword = keyPassword;
  }
}