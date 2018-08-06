package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.psi.manager.config.Constants
import spock.lang.Specification

class StreamManagerSpec extends Specification {

  def streamsConfig = StreamManager.streamsConfig(Constants.APP_ID, Constants.BOOTSTRAP_DEFAULT)
  def topology = StreamManager.buildTopology()
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def consumerFactory = new ConsumerRecordFactory(Constants.RAW_TOPIC,
      Serdes.String().serializer(), Serdes.String().serializer())
  def rawStore = driver.getKeyValueStore(Constants.RAW_TOPIC)
  def unparsedStore = driver.getKeyValueStore(Constants.UNPARSED_TOPIC)
  def parsedStore = driver.getKeyValueStore(Constants.PARSED_TOPIC)
  def smeStore = driver.getKeyValueStore(Constants.SME_TOPIC)
  def errorStore = driver.getKeyValueStore(Constants.ERROR_TOPIC)

  def cleanup(){
    driver.close()
  }

  def "Non-SME ISO granule parsed as expected"() {
    given:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
    def key = 'A123'
    def value = JsonOutput.toJson([
        rawFormat: 'isoXml',
        rawMetadata: xml
    ])

    when:
    // Default is raw-granule
    driver.pipeInput(consumerFactory.create(Constants.RAW_TOPIC, key, value))

    then:
    // Not found in error or SME topics
    errorStore.get(key) == null
    smeStore.get(key) == null

    and:
    def output = new JsonSlurper().parseText(parsedStore.get(key) as String) as Map
    !output.containsKey('error')
    output.containsKey('discovery')
    output.discovery.fileIdentifier == 'gov.super.important:FILE-ID'
    output.containsKey('analysis')
    output.analysis == []
  }

  def "Non-SME granule and SME granule end up in parsed-granule topic"() {
    // Simulate SME ending up in parsed-granule since that's another app's responsibility
  }

  def "Multiple split field values work as expected"() {}

  def "Unparsable granule ends up on error-granule topic"() {}
}
