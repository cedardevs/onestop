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

  public static Map<String, Object> getUnmappedAnalysisAndErrorsIndexFields() {
    // this method is just to prevent us from logging warnings about fields in the analysis schema that we know and choose not to map
    Map<String, Object> knownUnmappedTemporalFields = new HashMap<String, Object>();
    knownUnmappedTemporalFields.put("beginYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("beginDayOfYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("beginDayOfMonth", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("beginMonth", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("endYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("endDayOfYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("endDayOfMonth", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("endMonth", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("instantYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("instantDayOfYear", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("instantDayOfMonth", new HashMap<String, Object>());
    knownUnmappedTemporalFields.put("instantMonth", new HashMap<String, Object>());
    Map<String, Object> knownUnmappedFields = new HashMap<String, Object>();
    knownUnmappedFields.put("temporalBounding", knownUnmappedTemporalFields);
    return knownUnmappedFields;
  }

  public Map<String, Map> getTargetSearchIndexFields() {
    var searchAlias = esConfig.searchAliasFromType(recordType.toString());
    if(searchAlias != null) {
      return esConfig.indexedProperties(searchAlias);
    }
    else {
      return new HashMap<>();
    }
  }

  public Map<String, Map> getTargetAnalysisAndErrorsIndexFields() {
    var aeAlias = esConfig.analysisAndErrorsAliasFromType(recordType.toString());
    if(aeAlias != null) {
      return esConfig.indexedProperties(aeAlias);
    }
    else {
      return new HashMap<>();
    }
  }

  // public Map<String, Object> getTargetAnalysisAndErrorsIndexMapping() {
  //   var aeAlias = esConfig.analysisAndErrorsAliasFromType(recordType.toString());
  //   if(aeAlias != null) {
  //     return esConfig.indexedProperties(aeAlias);
  //   }
  //   else {
  //     return new HashMap<>();
  //   }
  // }

  // public static Map<String, Object> getNestedKeys(Map<String, Object> originalMap) {
  //   if (keysToKeep == null || keysToKeep.size() == 0) {
  //     return new HashMap<>();
  //   }
  //   return originalMap.entrySet().stream()
  //       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  // }

  @Override
  public String toString() {
    return "IndexingInput {" +
        ", recordType='" + recordType + "'" +
        ", key='" + key + "'" +
        ", value=" + value +
        '}';
  }

}
