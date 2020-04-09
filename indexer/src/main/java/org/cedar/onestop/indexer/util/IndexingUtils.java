package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.elasticsearch.action.DocWriteRequest.OpType.*;
import static org.elasticsearch.action.DocWriteRequest.OpType.UPDATE;

public class IndexingUtils {
  static final private Logger log = LoggerFactory.getLogger(IndexingUtils.class);

  public static List<DocWriteRequest<?>> mapRecordToRequests(IndexingInput input) {
    if (input == null) { return null; }
    try {
      var record = ValueAndTimestamp.getValueOrNull(input.getValue());
      var operation = (isTombstone(record) || isPrivate(record)) ? DELETE : INDEX;
      var indices = input.getIndexingConfig().getTargetIndices(input.getTopic(), operation);
      return indices.stream()
          .map(indexName -> buildWriteRequest(indexName, operation, input))
          .collect(Collectors.toList());
    } catch (ElasticsearchGenerationException e) {
      log.error("failed to serialize record with key [" + input.getKey() + "] to json", e);
      return new ArrayList<>();
    }
  }

  public static DocWriteRequest<?> buildWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }

    var esConfig = input.getEsConfig();
    var targetFields = esConfig.indexedProperties(indexName).keySet();

    var formattedRecord = new HashMap<String, Object>();

    var indexNameSansPrefix = indexName.substring(esConfig.PREFIX.length() > 0 ? esConfig.PREFIX.length() - 1 : 0);
    if (indexNameSansPrefix.equals("search")) {
      formattedRecord.putAll(TransformationUtils.reformatMessageForSearch(input.getValue().value(), targetFields));
      formattedRecord.put("stagedDate", input.getValue().timestamp());
    }
    else if (indexNameSansPrefix.contains("analysis")) {
      formattedRecord.putAll(TransformationUtils.reformatMessageForAnalysisAndErrors(input.getValue().value(), targetFields));
    }
    else {
      // FIXME should have some cleaner error handling between here and the unsupported opType below
    }

    if (opType == INDEX || opType == CREATE) {
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
    if (opType == UPDATE) {
      return new UpdateRequest(indexName, input.getKey()).doc(formattedRecord);
    }
    throw new UnsupportedOperationException("unsupported elasticsearch OpType: " + opType);
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
}
