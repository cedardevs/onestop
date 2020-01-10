package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.kstream.ValueMapperWithKey;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.kafka.common.util.TimestampedValue;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ElasticsearchRequestMapper implements ValueMapperWithKey<String, TimestampedValue<ParsedRecord>, DocWriteRequest> {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchRequestMapper.class);

  private final String indexName;

  public ElasticsearchRequestMapper(String indexName) {
    this.indexName = indexName;
  }

  @Override
  public DocWriteRequest apply(String readOnlyKey, TimestampedValue<ParsedRecord> value) {
    if (isTombstone(value) || isPrivate(value)) {
      return new DeleteRequest(indexName).id(readOnlyKey);
    }
    try {
      var formattedRecord = IndexingHelpers.reformatMessageForSearch(value.data);
      return new IndexRequest(indexName).id(readOnlyKey).source(formattedRecord);
    } catch (ElasticsearchGenerationException e) {
      log.error("Failed to serialize record with key [" + readOnlyKey + "] to json", e);
      return null;
    }
  }

  private boolean isTombstone(TimestampedValue<ParsedRecord> value) {
    return value == null || value.data == null;
  }

  private boolean isPrivate(TimestampedValue<ParsedRecord> value) {
    var optionalPublishing = Optional.of(value).map(TimestampedValue::getData).map(ParsedRecord::getPublishing);
    var isPrivate = optionalPublishing.map(Publishing::getIsPrivate).orElse(false);
    var until = optionalPublishing.map(Publishing::getUntil).orElse(null);
    return (until == null || until > System.currentTimeMillis()) ? isPrivate : !isPrivate;
  }

}
