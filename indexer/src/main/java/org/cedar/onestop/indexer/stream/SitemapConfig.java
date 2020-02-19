package org.cedar.onestop.indexer.stream;

import java.time.Duration;
import java.util.Objects;

public class SitemapConfig {

  private final String storeName;
  private final Duration interval;

  public SitemapConfig(String storeName, Duration duration) {
    this.storeName = storeName;
    this.interval = duration;
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
    private String storeName;
    private Duration interval;

    public SitemapConfigBuilder() {}

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
      return new SitemapConfig(storeName, interval);
    }
  }

}
