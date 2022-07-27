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
      new ElasticsearchVersion("7.17.5"),
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
    boolean parentIdentifier = analysis.identification.parentIdentifierExists
    boolean hlmExists = analysis.identification.hierarchyLevelNameExists
    String hierarchyLevelName = discovery.hierarchyLevelName
    if(hlmExists && hierarchyLevelName.toLowerCase() == 'granule' && parentIdentifier) {
      builder.setType(RecordType.granule)
    }
    else {
      builder.setType(RecordType.collection)
    }

    return builder.build()
  }

}
