package io.forgo.plugins.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SSLCertTask extends DefaultTask {

  @Override
  @Internal
  public String getDescription() {
    return "Builds an SSL public cert file for the Keystore Gradle Plugin";
  }

  @Override
  @Internal
  public String getGroup() {
    return "Keystore Gradle Plugin";
  }

  private String outputDir;
  private String keyFile;
  private String keyPassword;
  private String certFile;

  @TaskAction
  void generateCert() {

    String pathCertFile = this.outputDir + File.separatorChar + this.certFile;
    String pathKeyFile = this.outputDir + File.separatorChar + this.keyFile;

    // execute openssl cmd to create public cert
    getProject().exec(execSpec -> {
      execSpec.setIgnoreExitValue(true);
      execSpec.workingDir(".");
      execSpec.setExecutable("openssl");
      List<String> args = Arrays.asList(
        "req",
        "-new",
        "-x509",
        "-key", pathKeyFile,
        "-out", pathCertFile,
        "-passin", "pass:"+this.keyPassword,
        "-subj", "/C=US"
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

  public void setCertFile(String certFile) {
    this.certFile = certFile;
  }
}