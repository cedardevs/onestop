package org.cedar.onestop.indexer.util

import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.parse.ISOParser

class TestUtils {

  static ParsedRecord buildRecordFromXML(String xml) {
    def discovery = ISOParser.parseXMLMetadataToDiscovery(xml)
    def analysis = Analyzers.analyze(discovery)
    def builder = ParsedRecord.newBuilder().setDiscovery(discovery).setAnalysis(analysis)

    // Determine RecordType (aka granule or collection) from Discovery & Analysis info
    String parentIdentifier = discovery.parentIdentifier
    String hierarchyLevelName = discovery.hierarchyLevelName
    if (hierarchyLevelName == null || hierarchyLevelName != 'granule' || !parentIdentifier) {
      builder.setType(RecordType.collection)
    }
    else {
      builder.setType(RecordType.granule)
    }

    return builder.build()
  }

}
