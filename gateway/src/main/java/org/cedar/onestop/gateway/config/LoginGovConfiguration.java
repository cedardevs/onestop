package org.cedar.onestop.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("login-gov")
class LoginGovConfiguration {

  static class Keystore {
    String alias;
    String file;
    String password;
    String type;

    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    public String getFile() {
      return file;
    }

    public void setFile(String file) {
      this.file = file;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  Keystore keystore;

  public Keystore getKeystore() {
    return keystore;
  }

  public void setKeystore(Keystore keystore) {
    this.keystore = keystore;
  }
}
