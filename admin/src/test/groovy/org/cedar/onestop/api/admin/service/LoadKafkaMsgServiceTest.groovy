package org.cedar.onestop.api.admin.service

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class LoadKafkaMsgServiceTest extends Specification {

  static testCollectionTopic = 'test_collection_topic'
  static testGranuleTopic = 'test_granule_topic'

  def consumerService = new KafkaConsumerService()
  def mockMetadataService = Mock(MetadataManagementService)

  def setup() {
    consumerService.parsedCollectionTopic = testCollectionTopic
    consumerService.parsedGranulesTopic = testGranuleTopic
    consumerService.metadataManagementService = mockMetadataService
  }
  
  def "loads a valid metadata record" () {
    given:
    def inputKey = 'ABC'
    def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
    def inputValue = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)
    def inputRecord = new ConsumerRecord(testCollectionTopic, 0, 0, inputKey, inputValue)
   
    when:
    consumerService.listen([inputRecord])

    then:
    1 * mockMetadataService.loadParsedRecords([[id: inputKey, parsedRecord: inputValue]])
  }

  def "ignores invalid metadata record" () {
    given:
    def inputKey = 'ABC'
    def inputValue = new ParsedRecord()
    def inputRecord = new ConsumerRecord(testCollectionTopic, 0, 0, inputKey, inputValue)

    when:
    consumerService.listen([inputRecord])

    then:
    0 * mockMetadataService.loadParsedRecords(_)
  }

  def "filters out invalid metadata records" () {
    given:
    def inputKey = 'ABC'
    def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
    def validValue = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)
    def validRecord = new ConsumerRecord(testCollectionTopic, 0, 0, inputKey, validValue)
    def invalidValue = new ParsedRecord()
    def invalidRecord = new ConsumerRecord(testCollectionTopic, 0, 0, inputKey, invalidValue)

    when:
    consumerService.listen([validRecord, invalidRecord])

    then:
    1 * mockMetadataService.loadParsedRecords({ it.size() == 1 && it[0].parsedRecord == validRecord.value() })
  }

  def "appends default index-ready Discovery and Analysis to ParsedRecord"() {
    given:
    def inputKey = 'default123'
    def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('parsed-record-no-discovery-or-analysis.json')
    def inputValue = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)
    def inputRecord = new ConsumerRecord(testCollectionTopic, 0 , 0, inputKey, inputValue)

    // since we are only testing that discovery and analysis get populated with default values, we don't need to
    // compare an entire Avro object
    // TODO: how do we compare when fields like `double` get converted (e.g. - 56.0 --> 55.60000000000001)
    // if this is not a concern, we should eventually remove the unused `parsed-record-with-default-discovery.json`
//    def expectedOutputStream = ClassLoader.systemClassLoader.getResourceAsStream('parsed-record-with-default-discovery.json')
//    def expectedOutputValue = AvroUtils.<ParsedRecord> jsonToAvro(expectedOutputStream, ParsedRecord.classSchema)

    when:
    consumerService.listen([inputRecord])

    then:
    1 * mockMetadataService.loadParsedRecords({
      String id = it[0]?.id as String
      ParsedRecord record = it[0]?.parsedRecord as ParsedRecord

      Boolean isCorrectNumberOfRecords = it.size() == 1
      Boolean idMatchesInputKey = id == inputKey
      Boolean discoveryExists = record.discovery != null
      Boolean analysisExists = record.analysis != null

      isCorrectNumberOfRecords && idMatchesInputKey && discoveryExists && analysisExists
    })
  }

}
