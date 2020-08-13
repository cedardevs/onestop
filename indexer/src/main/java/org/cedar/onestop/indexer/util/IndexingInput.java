package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.RecordType;
import org.elasticsearch.action.DocWriteRequest;

import java.util.*;

public class IndexingInput {

  private final String key;
  private final ValueAndTimestamp<ParsedRecord> value;
  private final String topic;
  private final ElasticsearchConfig esConfig;

  public IndexingInput(String key, ValueAndTimestamp<ParsedRecord> value, String topic, ElasticsearchConfig esConfig) {
    this.key = key;
    this.value = value;
    this.topic = topic;
    this.esConfig = esConfig;
  }

  public String getKey() {
    return key;
  }

  public ValueAndTimestamp<ParsedRecord> getValue() {
    return value;
  }

  public String getTopic() {
    return topic;
  }

  public ElasticsearchConfig getConfig() {
    return esConfig;
  }

  @Override
  public String toString() {
    return "IndexingInput {" +
        "key='" + key + "'" +
        ", value='" + value + "'" +
        ", topic='" + topic + "'" +
        '}';
  }

}
