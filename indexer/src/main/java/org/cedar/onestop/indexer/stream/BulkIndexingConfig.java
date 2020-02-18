package org.cedar.onestop.indexer.stream;

import java.time.Duration;
import java.util.*;

import static org.elasticsearch.action.DocWriteRequest.OpType;

public class BulkIndexingConfig {

  private final String storeName;
  private final Duration maxPublishInterval;
  private final long maxPublishBytes;
  private final Map<String, Map<OpType, List<String>>> indexMappings;

  private BulkIndexingConfig(
      String storeName,
      Duration maxPublishInterval,
      long maxPublishBytes,
      Map<String, Map<OpType, List<String>>> indexMappings) {
    this.storeName = storeName;
    this.maxPublishInterval = maxPublishInterval;
    this.maxPublishBytes = maxPublishBytes;
    this.indexMappings = indexMappings;
  }

  public List<String> getTargetIndices(String topic, OpType opType) {
    return Optional.ofNullable(topic)
        .map(t -> indexMappings.getOrDefault(t, null))
        .map(m -> m.getOrDefault(opType, null))
        .orElse(new ArrayList<>());
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

  public static BulkIndexingConfigBuilder newBuilder() {
    return new BulkIndexingConfigBuilder();
  }

  public static class BulkIndexingConfigBuilder {
    private String storeName;
    private Duration maxPublishInterval;
    private Long maxPublishBytes;
    private Map<String, Map<OpType, List<String>>> topicMappings;

    public BulkIndexingConfigBuilder() {
      topicMappings = new HashMap<>();
    }

    public synchronized BulkIndexingConfigBuilder addIndexMapping(String topic, OpType opType, String index) {
      var mapping = topicMappings.getOrDefault(topic, new HashMap<>());
      var indicesForOp = mapping.getOrDefault(opType, new ArrayList<>());
      indicesForOp.add(index);
      mapping.put(opType, indicesForOp);
      topicMappings.put(topic, mapping);
      return this;
    }

    public synchronized BulkIndexingConfigBuilder withMaxPublishInterval(Duration duration) {
      maxPublishInterval = duration;
      return this;
    }

    public synchronized BulkIndexingConfigBuilder withMaxPublishBytes(long bytes) {
      maxPublishBytes = bytes;
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
        throw new IllegalArgumentException("maxPublishingBytes is required");
      }
      if (maxPublishInterval == null) {
        throw new IllegalArgumentException("maxPublishingInterval is required");
      }
      if (topicMappings.size() == 0) {
        throw new IllegalArgumentException("at least one mapping of (topic, opType) -> index is required");
      }
      var unmodifiableMappings = Collections.unmodifiableMap(topicMappings);
      return new BulkIndexingConfig(storeName, maxPublishInterval, maxPublishBytes, unmodifiableMappings);
    }

  }
}
