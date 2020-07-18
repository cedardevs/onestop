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
  private final ElasticsearchConfig esConfig;
  private final RecordType recordType;

  public IndexingInput(String key, ValueAndTimestamp<ParsedRecord> value, String topic, ElasticsearchConfig esConfig) {
    this.key = key;
    this.value = value;
    this.esConfig = esConfig;
    this.recordType = IndexingUtils.determineTypeFromTopic(topic);
  }

  public String getKey() {
    return key;
  }

  public ValueAndTimestamp<ParsedRecord> getValue() {
    return value;
  }

  public boolean isIndexable() {
    return recordType != null;
  }

  public List<String> getTargetSearchIndices(DocWriteRequest.OpType opType, boolean recordIsValid) {
    var indices = new ArrayList<String>();

    if(recordIsValid) {
      indices.add(esConfig.searchAliasFromType(recordType.toString()));

      if(opType == DocWriteRequest.OpType.DELETE && recordType == RecordType.granule) {
        indices.add(esConfig.searchAliasFromType(ElasticsearchConfig.TYPE_FLATTENED_GRANULE));
      }
    }

    return indices;
  }

  public String getTargetAnalysisAndErrorsIndex() {
    return esConfig.analysisAndErrorsAliasFromType(recordType.toString());
  }

  public Set<String> getTargetSearchIndexFields() {
    var searchAlias = esConfig.searchAliasFromType(recordType.toString());
    if(searchAlias != null) {
      return esConfig.indexedProperties(searchAlias).keySet();
    }
    else {
      return new HashSet<>();
    }
  }

  public Set<String> getTargetAnalysisAndErrorsIndexFields() {
    var aeAlias = esConfig.analysisAndErrorsAliasFromType(recordType.toString());
    if(aeAlias != null) {
      return esConfig.indexedProperties(aeAlias).keySet();
    }
    else {
      return new HashSet<>();
    }
  }

  public RecordType getRecordType() {
    return recordType;
  }

  @Override
  public String toString() {
    return "IndexingInput {" +
        ", recordType='" + recordType + "'" +
        ", key='" + key + "'" +
        ", value=" + value +
        '}';
  }

}
