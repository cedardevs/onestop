package org.cedar.onestop.indexer.stream;

import java.time.Duration;

public class BulkIndexingConfig {

  private final String storeName;
  private final Duration maxPublishInterval;
  private final long maxPublishBytes;
  private final int maxPublishActions;

  private BulkIndexingConfig(
      String storeName,
      Duration maxPublishInterval,
      long maxPublishBytes,
      int maxPublishActions) {
    this.storeName = storeName;
    this.maxPublishInterval = maxPublishInterval;
    this.maxPublishBytes = maxPublishBytes;
    this.maxPublishActions = maxPublishActions;
  }

  public String getStoreName() {
    return storeName;
  }

  public Duration getMaxPublishInterval() {
    return maxPublishInterval;
  }

  public long getMaxPublishBytes() {
    return maxPublishBytes;
  }

  public int getMaxPublishActions() {
    return maxPublishActions;
  }

  public static BulkIndexingConfigBuilder newBuilder() {
    return new BulkIndexingConfigBuilder();
  }

  public static class BulkIndexingConfigBuilder {
    private String storeName;
    private Duration maxPublishInterval;
    private Long maxPublishBytes;
    private Integer maxPublishActions;

    // No constructor defined here because everything is set with individual setters below

    public synchronized BulkIndexingConfigBuilder withMaxPublishInterval(Duration duration) {
      maxPublishInterval = duration;
      return this;
    }

    public synchronized BulkIndexingConfigBuilder withMaxPublishBytes(Long bytes) {
      maxPublishBytes = bytes;
      return this;
    }

    public synchronized BulkIndexingConfigBuilder withMaxPublishActions(Integer actions) {
      maxPublishActions = actions;
      return this;
    }

    public synchronized BulkIndexingConfigBuilder withStoreName(String name) {
      storeName = name;
      return this;
    }

    public synchronized BulkIndexingConfig build() {
      if (storeName == null) {
        throw new IllegalArgumentException("storeName is required");
      }
      if (maxPublishBytes == null) {
        throw new IllegalArgumentException("maxPublishBytes is required");
      }
      if (maxPublishInterval == null) {
        throw new IllegalArgumentException("maxPublishInterval is required");
      }
      if (maxPublishActions == null) {
        throw new IllegalArgumentException("maxPublishActions is required");
      }
      return new BulkIndexingConfig(storeName, maxPublishInterval, maxPublishBytes, maxPublishActions);
    }

  }
}
