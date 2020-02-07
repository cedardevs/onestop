package org.cedar.onestop.indexer.util;

import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;

public class IndexingOutput {

  private final boolean valid;
  private final ValueAndTimestamp<ParsedRecord> recordAndTimestamp;
  private final BulkItemResponse itemResponse;


  public IndexingOutput(boolean valid, ValueAndTimestamp<ParsedRecord> recordAndTimestamp, BulkItemResponse itemResponse) {
    this.valid = valid;
    this.recordAndTimestamp = recordAndTimestamp;
    this.itemResponse = itemResponse;
  }

  public boolean isValid() {
    return valid;
  }

  public ValueAndTimestamp<ParsedRecord> getRecordAndTimestamp() {
    return recordAndTimestamp;
  }

  public ParsedRecord getRecord() {
    return ValueAndTimestamp.getValueOrNull(recordAndTimestamp);
  }

  public Long getTimestamp() {
    return recordAndTimestamp != null ? recordAndTimestamp.timestamp() : null;
  }

  public String getId() {
    return itemResponse != null ? itemResponse.getId() : null;
  }

  public String getIndex() {
    return itemResponse != null ? itemResponse.getIndex() : null;
  }

  public DocWriteRequest.OpType getOperation() {
    return itemResponse != null ? itemResponse.getOpType() : null;
  }

  public boolean isSuccessful() {
    return itemResponse != null && !itemResponse.isFailed();
  }

  @Override
  public String toString() {
    return "IndexingOutput{" +
        "valid=" + valid +
        ", id=" + getId() +
        ", index=" + getIndex() +
        ", operation=" + getOperation() +
        ", successful=" + isSuccessful() +
        ", timestamp=" + getTimestamp() +
        '}';
  }
}
