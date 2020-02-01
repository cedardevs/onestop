package org.cedar.onestop.elastic.common;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticsearchVersion {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchVersion.class);

  public static final Byte MINIMUM_COMPATIBLE_MAJOR_VERSION = 7;

  private Byte majorVersion;
  private Byte minorVersion;
  private Byte patchVersion;

  public ElasticsearchVersion(RestHighLevelClient restHighLevelClient) throws IOException {
    MainResponse.Version version = restHighLevelClient.info(RequestOptions.DEFAULT).getVersion();
    String versionNumber = version.getNumber();
    initAndCheck(versionNumber);
  }

  // this constructor is used for units tests where a real elasticsearch cannot be used to retrieve the running version
  public ElasticsearchVersion(String versionNumber) {
    initAndCheck(versionNumber);
  }

  private void initAndCheck(String versionNumber) {
    byte[] semVer = from(versionNumber);
    this.majorVersion = semVer[0];
    this.minorVersion = semVer[1];
    this.patchVersion = semVer[2];

    if (this.majorVersion < MINIMUM_COMPATIBLE_MAJOR_VERSION) {
      throw new IllegalStateException("Service does not work against Elasticsearch versions < " + MINIMUM_COMPATIBLE_MAJOR_VERSION.toString());
    }
    log.info("Found running Elasticsearch version: " + versionNumber);
  }

  public static byte[] from(String versionNumber) {
    String[] parts = versionNumber.split("[.-]");
    // allow for optional snapshot and qualifier
    if(parts.length < 3 || parts.length > 5) {
      throw new IllegalArgumentException("Invalid version: " + versionNumber);
    }
    return new byte[] { Byte.parseByte(parts[0]), Byte.parseByte(parts[1]), Byte.parseByte(parts[2]) };
  }

  public boolean isMajorVersion(int majorVersion) {
    return (byte)majorVersion == this.majorVersion;
  }

  public boolean isMinorVersion(int minorVersion) {
    return (byte)minorVersion == this.minorVersion;
  }

  public boolean isPatchVersion(int patchVersion) {
    return (byte)patchVersion == this.patchVersion;
  }

  public boolean onOrAfter(String versionNumber) {

    byte[] semVer = from(versionNumber);

    if(this.majorVersion < semVer[0]) {
      return false;
    }
    if(this.majorVersion == semVer[0] && this.minorVersion < semVer[1]) {
      return false;
    }
    return this.majorVersion != semVer[0] || this.minorVersion != semVer[1] || this.patchVersion >= semVer[2];
  }

}
