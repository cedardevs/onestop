package org.cedar.onestop.indexer.stream;

import java.time.Duration;
import java.util.Objects;

public class SitemapConfig {

  private final Boolean enabled;
  private final String storeName;
  private final Duration interval;

  public SitemapConfig(Boolean enabled, String storeName, Duration duration) {
    this.enabled = enabled != null ? enabled : true; // enabled by default
    this.storeName = storeName;
    this.interval = duration;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public String getStoreName() {
    return storeName;
  }

  public Duration getInterval() {
    return interval;
  }

  public static SitemapConfigBuilder newBuilder() {
    return new SitemapConfigBuilder();
  }

  public static class SitemapConfigBuilder {
    private Boolean enabled;
    private String storeName;
    private Duration interval;

    public SitemapConfigBuilder() {}

    public SitemapConfigBuilder withEnabled(Boolean bool) {
      enabled = bool;
      return this;
    }

    public SitemapConfigBuilder withStoreName(String name) {
      storeName = name;
      return this;
    }

    public SitemapConfigBuilder withInterval(Duration duration) {
      interval = duration;
      return this;
    }

    public SitemapConfig build() {
      Objects.requireNonNull(storeName, "storeName is required");
      Objects.requireNonNull(interval, "interval is required");
      return new SitemapConfig(enabled, storeName, interval);
    }
  }

}
