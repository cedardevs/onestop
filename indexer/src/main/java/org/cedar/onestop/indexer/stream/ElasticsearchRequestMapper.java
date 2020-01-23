package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.kstream.ValueMapperWithKey;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Publishing;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticsearchRequestMapper implements ValueMapperWithKey<String, ValueAndTimestamp<ParsedRecord>, List<DocWriteRequest>> {
  private static final Logger log = LoggerFactory.getLogger(ElasticsearchRequestMapper.class);

  private final Stream<String> insertIndices;
  private final Stream<String> deleteIndices;

  public ElasticsearchRequestMapper(Collection<String> insertIndices, Collection<String> deleteIndices) {
    this.insertIndices = insertIndices.stream();
    this.deleteIndices = deleteIndices.stream();
  }

  @Override
  public List<DocWriteRequest> apply(String readOnlyKey, ValueAndTimestamp<ParsedRecord> value) {
    if (isTombstone(value) || isPrivate(value)) {
      return deleteIndices
          .map(indexName -> new DeleteRequest(indexName).id(readOnlyKey))
          .collect(Collectors.toList());
    }
    try {
      var formattedRecord = IndexingHelpers.reformatMessageForSearch(value.value());
      return insertIndices
          .map(indexName -> new IndexRequest(indexName).id(readOnlyKey).source(formattedRecord))
          .collect(Collectors.toList());
    } catch (ElasticsearchGenerationException e) {
      log.error("Failed to serialize record with key [" + readOnlyKey + "] to json", e);
      return null;
    }
  }

  private boolean isTombstone(ValueAndTimestamp<ParsedRecord> value) {
    return ValueAndTimestamp.getValueOrNull(value) == null;
  }

  private boolean isPrivate(ValueAndTimestamp<ParsedRecord> value) {
    var optionalPublishing = Optional.of(value).map(ValueAndTimestamp::value).map(ParsedRecord::getPublishing);
    var isPrivate = optionalPublishing.map(Publishing::getIsPrivate).orElse(false);
    var until = optionalPublishing.map(Publishing::getUntil).orElse(null);
    return (until == null || until > System.currentTimeMillis()) ? isPrivate : !isPrivate;
  }

}
