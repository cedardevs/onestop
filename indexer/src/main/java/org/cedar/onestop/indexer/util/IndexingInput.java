package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.stream.BulkIndexingConfig;
import org.cedar.schemas.avro.psi.ParsedRecord;

public class IndexingInput {

  private final String topic;
  private final String key;
  private final ValueAndTimestamp<ParsedRecord> value;
  private final BulkIndexingConfig indexingConfig;
  private final ElasticsearchConfig esConfig;

  public IndexingInput(String topic, String key, ValueAndTimestamp<ParsedRecord> value, BulkIndexingConfig indexingConfig, ElasticsearchConfig esConfig) {
    this.topic = topic;
    this.key = key;
    this.value = value;
    this.indexingConfig = indexingConfig;
    this.esConfig = esConfig;
  }

  public String getTopic() {
    return topic;
  }

  public String getKey() {
    return key;
  }

  public ValueAndTimestamp<ParsedRecord> getValue() {
    return value;
  }

  public BulkIndexingConfig getIndexingConfig() {
    return indexingConfig;
  }

  public ElasticsearchConfig getEsConfig() {
    return esConfig;
  }

  @Override
  public String toString() {
    return "IndexingInput{" +
        "topic='" + topic + '\'' +
        ", key='" + key + '\'' +
        ", value=" + value +
        '}';
  }

}
