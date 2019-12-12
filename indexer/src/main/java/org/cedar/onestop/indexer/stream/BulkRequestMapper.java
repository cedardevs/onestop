package org.cedar.onestop.indexer.stream;

import org.apache.kafka.streams.kstream.ValueMapperWithKey;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.onestop.kafka.common.util.TimestampedValue;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkRequestMapper implements ValueMapperWithKey<String, TimestampedValue<ParsedRecord>, DocWriteRequest> {
  private static final Logger log = LoggerFactory.getLogger(BulkRequestMapper.class);

  private final ElasticsearchConfig esConfig;
  private final String indexName;

  public BulkRequestMapper(ElasticsearchConfig esConfig, String indexName) {
    this.esConfig = esConfig;
    this.indexName = indexName;
  }

  @Override
  public DocWriteRequest apply(String readOnlyKey, TimestampedValue<ParsedRecord> value) {
    if (value == null || value.data == null || (value.data.getPublishing() != null && value.data.getPublishing().getIsPrivate())) {
      return new DeleteRequest(indexName).id(readOnlyKey);
    }
    try {
      var formattedRecord = IndexingHelpers.reformatMessageForSearch(value.data);
      return new IndexRequest(indexName).id(readOnlyKey).source(formattedRecord).type(esConfig.TYPE);
    } catch (ElasticsearchGenerationException e) {
      log.error("Failed to serialize record with key [" + readOnlyKey + "] to json", e);
      return null;
    }
  }
}
