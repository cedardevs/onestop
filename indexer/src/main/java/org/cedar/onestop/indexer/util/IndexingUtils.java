package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.constants.Topics;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;
import org.cedar.schemas.avro.psi.RecordType;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.elasticsearch.action.DocWriteRequest.OpType.*;

public class IndexingUtils {
  static final private Logger log = LoggerFactory.getLogger(IndexingUtils.class);

  public static List<DocWriteRequest<?>> mapRecordToRequests(IndexingInput input) {
    List<DocWriteRequest<?>> requests = new ArrayList<>();
    if (input == null) {
      return requests;
    }

    var recordType = determineRecordTypeFromTopic(input.getTopic());
    if (recordType != null) {
      var record = ValueAndTimestamp.getValueOrNull(input.getValue());
      var isValid = isValid(record);
      var operation = (isTombstone(record) || isPrivate(record)) ? DELETE : INDEX;
      var indexType = determineIndexTypeFromTopic(input.getTopic());

      try {
        if (isValid) {
          var searchIndex = input.getConfig().searchAliasFromType(indexType);
          requests.add(buildSearchWriteRequest(searchIndex, operation, input));
        }
        if (!indexType.equals(ElasticsearchConfig.TYPE_FLATTENED_GRANULE)) {
          var aeIndex = input.getConfig().analysisAndErrorsAliasFromType(indexType);
          requests.add(buildAnalysisAndErrorWriteRequest(aeIndex, operation, input, recordType));
        }

      } catch (ElasticsearchGenerationException e) {
        log.error("Failed to serialize record with key [" + input.getKey() + "] to json", e);
        return new ArrayList<>();
      }
    }
    return requests;
  }

  public static boolean isTombstone(ParsedRecord value) {
    return value == null;
  }

  public static boolean isPrivate(ParsedRecord value) {
    var optionalPublishing = Optional.of(value).map(ParsedRecord::getPublishing);
    var isPrivate = optionalPublishing.map(Publishing::getIsPrivate).orElse(false);
    var until = optionalPublishing.map(Publishing::getUntil).orElse(null);
    return (until == null || until > System.currentTimeMillis()) ? isPrivate : !isPrivate;
  }

  public static boolean isValid(ParsedRecord value) {
    return value == null || value.getErrors().isEmpty();
  }

  public static DocWriteRequest<?> buildSearchWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      var targetFields = input.getConfig().indexedProperties(indexName).keySet();
      var formattedRecord = new HashMap<>(TransformationUtils.reformatMessageForSearch(input.getValue().value(), targetFields));
      formattedRecord.put("stagedDate", input.getValue().timestamp());
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
  }

  public static DocWriteRequest<?> buildAnalysisAndErrorWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input, RecordType recordType) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      var targetFields = input.getConfig().indexedProperties(indexName).keySet();
      var formattedRecord = new HashMap<>(TransformationUtils.reformatMessageForAnalysis(input.getValue().value(), targetFields, recordType));
      formattedRecord.put("stagedDate", input.getValue().timestamp());
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
  }

  public static RecordType determineRecordTypeFromTopic(String topic) {
    if (topic == null) {
      return null;
    }
    if (topic.equals(Topics.flattenedGranuleTopic())) {
      return RecordType.granule;
    }
    for(RecordType record : RecordType.values()) {
      if(topic.equals(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, record))) {
        return record;
      }
    }
    return null;
  }

  public static String determineIndexTypeFromTopic(String topic) {
    if (topic == null) {
      return null;
    }
    if (topic.equals(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule))) {
      return ElasticsearchConfig.TYPE_GRANULE;
    }
    else if (topic.equals(Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection))) {
      return ElasticsearchConfig.TYPE_COLLECTION;
    }
    else if (topic.equals(Topics.flattenedGranuleTopic())) {
      return ElasticsearchConfig.TYPE_FLATTENED_GRANULE;
    }
    else {
      return null;
    }
  }

}
