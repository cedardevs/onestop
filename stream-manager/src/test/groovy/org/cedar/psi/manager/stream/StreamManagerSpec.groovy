package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.cedar.psi.manager.StreamManagerMain
import org.cedar.psi.manager.config.AppConfig
import org.cedar.psi.manager.config.KafkaConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = [StreamManagerMain])
class StreamManagerSpec extends Specification {

  @Autowired
  AppConfig appConfig

  @Autowired
  KafkaConfig kafkaConfig

  def streamsConfig = StreamManager.streamsConfig(kafkaConfig.application.id, kafkaConfig.bootstrapServers)
  def topology = StreamManager.buildTopology(appConfig)
  def driver = new TopologyTestDriver(topology, streamsConfig)
  def consumerFactory = new ConsumerRecordFactory(appConfig.topics.rawGranules,
      Serdes.String().serializer(), Serdes.String().serializer())
  def rawStore = driver.getKeyValueStore(appConfig.topics.rawGranules)
  def unparsedStore = driver.getKeyValueStore(appConfig.topics.unparsedGranules)
  def parsedStore = driver.getKeyValueStore(appConfig.topics.parsedGranules)
  def smeStore = driver.getKeyValueStore(appConfig.topics.parsedGranules)
  def errorStore = driver.getKeyValueStore(appConfig.topics.errorGranules)

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
    driver.pipeInput(consumerFactory.create(key, value))

    then:
    // Not found in error or SME topics
    errorStore.get(key) == null
    smeStore.get(key) == null

    and:
    def output = new JsonSlurper().parseText(parsedStore.get(key)) as Map
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
