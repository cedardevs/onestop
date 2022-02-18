package io.forgo.plugins.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PKCS12Task extends DefaultTask {

  @Override
  @Internal
  public String getDescription() {
    return "Builds a PKCS12 keystore file for the Keystore Gradle Plugin";
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
  private String pkcs12File;
  private String pkcs12Password;
  private String keystoreAlias;

  @TaskAction
  void generatePKCS12() {

    String pathPKCS12File = this.outputDir + File.separatorChar + this.pkcs12File;
    String pathKeyFile = this.outputDir + File.separatorChar + this.keyFile;
    String pathCertFile = this.outputDir + File.separatorChar + this.certFile;

    // execute openssl cmd to create pkcs12 keystore
    getProject().exec(execSpec -> {
      execSpec.setIgnoreExitValue(true);
      execSpec.workingDir(".");
      execSpec.setExecutable("openssl");
      List<String> args = Arrays.asList(
        "pkcs12",
        "-inkey", pathKeyFile,
        "-in", pathCertFile,
        "-export",
        "-out", pathPKCS12File,
        "-passin", "pass:"+this.keyPassword,
        "-password", "pass:"+this.pkcs12Password,
        "-name", this.keystoreAlias
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

  public void setPkcs12File(String pkcs12File) {
    this.pkcs12File = pkcs12File;
  }

  public void setPkcs12Password(String pkcs12Password) {
    this.pkcs12Password = pkcs12Password;
  }

  public void setKeystoreAlias(String keystoreAlias) {
    this.keystoreAlias = keystoreAlias;
  }
}