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
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cedar.onestop.data.util.JsonUtils;
import org.cedar.onestop.data.util.ListUtils;

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
          requests.add(buildSearchWriteRequest(searchIndex, operation, input, recordType));
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
    return ListUtils.pruneEmptyElements(requests);
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

  public static DocWriteRequest<?> buildSearchWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input, RecordType recordType) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      Object formattedRecord = null;
      switch (recordType) {
        case collection:
        formattedRecord = TransformationUtils.reformatCollectionForSearch(input.getValue().timestamp(), input.getValue().value());
        break;
        case granule:
        if(indexName.contains("flattened")) {
          formattedRecord = TransformationUtils.reformatFlattenedGranuleForSearch(input.getValue().timestamp(), input.getValue().value());
        } else {
          formattedRecord = TransformationUtils.reformatGranuleForSearch(input.getValue().timestamp(), input.getValue().value());
        break;
        }
      }
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(JsonUtils.toJson(formattedRecord).getBytes(), XContentType.JSON);

    }
  }

  public static DocWriteRequest<?> buildAnalysisAndErrorWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input, RecordType recordType) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      Object formattedRecord = null;
      switch (recordType) {
        case collection:
        formattedRecord = TransformationUtils.reformatCollectionForAnalysis(input.getValue().timestamp(), input.getValue().value());
        break;
        case granule:
        formattedRecord = TransformationUtils.reformatGranuleForAnalysis(input.getValue().timestamp(), input.getValue().value());

      }
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(JsonUtils.toJson(formattedRecord).getBytes(), XContentType.JSON);


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
