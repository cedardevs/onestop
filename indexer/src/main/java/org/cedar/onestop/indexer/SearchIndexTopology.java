package org.cedar.onestop.indexer;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.cedar.onestop.indexer.util.IndexingHelpers;
import org.cedar.schemas.avro.psi.ParsedRecord;

public class SearchIndexTopology {

  Topology buildSearchIndexTopology(StreamsBuilder streamsBuilder) {
    addGranuleSearchStream(streamsBuilder);
    return streamsBuilder.build();
  }

  private void addGranuleSearchStream(StreamsBuilder builder) {
    //TODO - add granule changelog to Topics
    builder.<String, ParsedRecord>stream("THEBESTTOPIC")
        .mapValues(IndexingHelpers::reformatMessageForSearch)
        .mapValues(v -> {v.remove("type"); return v;}) // TODO - put this in reformat
        .mapValues(v -> {v.put("stagedDate", System.currentTimeMillis()); return v;}); // TODO - put this in reformat
  }


}
