package io.forgo.plugins.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JKSTask extends DefaultTask {

  @Override
  @Internal
  public String getDescription() {
    return "Builds a JKS keystore file for the Keystore Gradle Plugin";
  }

  @Override
  @Internal
  public String getGroup() {
    return "Keystore Gradle Plugin";
  }

  private String outputDir;
  private String pkcs12File;
  private String pkcs12Password;
  private String jksFile;
  private String jksPassword;

  @TaskAction
  void generateJKS() {

    String pathJksFile = this.outputDir + File.separatorChar + this.jksFile;
    String pathPkcs12File = this.outputDir + File.separatorChar + this.pkcs12File;

    getProject().exec(execSpec -> {
      execSpec.setIgnoreExitValue(true);
      execSpec.workingDir(".");
      execSpec.setExecutable("keytool");
      List<String> args = Arrays.asList(
        "-importkeystore",
        "-srcstoretype", "PKCS12",
        "-srckeystore", pathPkcs12File,
        "-srcstorepass", this.pkcs12Password,
        "-destkeystore", pathJksFile,
        "-storepass", this.jksPassword
      );
      execSpec.setArgs(args);
    });
  }

  public void setOutputDir(String outputDir) {
    this.outputDir = outputDir;
  }

  public void setPkcs12File(String pkcs12File) {
    this.pkcs12File = pkcs12File;
  }

  public void setPkcs12Password(String pkcs12Password) {
    this.pkcs12Password = pkcs12Password;
  }

  public void setJksFile(String jksFile) {
    this.jksFile = jksFile;
  }

  public void setJksPassword(String jksPassword) {
    this.jksPassword = jksPassword;
  }
}