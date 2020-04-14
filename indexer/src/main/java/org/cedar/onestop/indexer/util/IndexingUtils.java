package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.action.DocWriteRequest.OpType.*;

public class IndexingUtils {
  static final private Logger log = LoggerFactory.getLogger(IndexingUtils.class);

  public static List<DocWriteRequest<?>> mapRecordToRequests(IndexingInput input) {
    List<DocWriteRequest<?>> requests = new ArrayList<>();

    if (input != null) {
      var record = ValueAndTimestamp.getValueOrNull(input.getValue());
      var operation = (isTombstone(record) || isPrivate(record)) ? DELETE : INDEX;

      var searchIndices = input.getTargetSearchIndices(operation);
      var aeIndex = input.getTargetAnalysisAndErrorsIndex();

      try {
        searchIndices.forEach(i -> requests.add(buildSearchWriteRequest(i, operation, input)));
        requests.add(buildAnalysisAndErrorWriteRequest(aeIndex, operation, input));

      } catch (ElasticsearchGenerationException e) {
        log.error("Failed to serialize record with key [" + input.getKey() + "] to json", e);
        // FIXME should we return whatever managed to be added to requests or actually an empty list?
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

  public static DocWriteRequest<?> buildSearchWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      var formattedRecord = new HashMap<String, Object>();
      formattedRecord.putAll(TransformationUtils.reformatMessageForSearch(input.getValue().value(), input.getTargetSearchIndexFields()));
      formattedRecord.put("stagedDate", input.getValue().timestamp());
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
  }

  public static DocWriteRequest<?> buildAnalysisAndErrorWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }
    else {
      var formattedRecord = new HashMap<String, Object>();
      formattedRecord.putAll(TransformationUtils.reformatMessageForAnalysisAndErrors(input.getValue().value(), input.getTargetAnalysisAndErrorsIndexFields()));
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
  }
}
