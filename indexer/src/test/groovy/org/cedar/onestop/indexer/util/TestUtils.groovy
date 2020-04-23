package org.cedar.onestop.indexer.util

import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.kafka.common.constants.StreamsApps
import org.cedar.onestop.kafka.common.constants.Topics
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.parse.ISOParser

class TestUtils {

  static final inputAvroStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
  static final inputAvroRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputAvroStream, ParsedRecord.classSchema)

  static collectionTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.collection)
  static granuleTopic = Topics.parsedChangelogTopic(StreamsApps.REGISTRY_ID, RecordType.granule)

  static final inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputCollectionRecord = buildRecordFromXML(inputCollectionXml)

  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-granule.xml').text
  static inputGranuleRecord = buildRecordFromXML(inputGranuleXml)

  public static final esConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "Test-",
      1,
      1,
      1,
      1,
      false
  )

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
