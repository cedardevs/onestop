package org.cedar.psi.wrapper.util

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import spock.lang.Specification

import java.nio.file.Paths

@Slf4j
class StreamTopologySpec extends Specification{

  static STRING_SERIALIZER = Serdes.String().serializer()
  static STRING_DESERIALIZER = Serdes.String().deserializer()

  def currentDir = Paths.get(".").toAbsolutePath().normalize().toString()
  def command = "python ${currentDir}/scripts/dscovrIsoLite.py stdin"
  def topologyConfig = [
      topics: [
          input: 'test-input',
          output: 'test-output',
          errorout: 'test-sme-error',
      ],
      command: command,
      timeout: 5000,
  ]
  def kafkaConfig = [
      application: [id: 'test-app'],
      bootstrap: [servers: 'localhost:9092']
  ]

  def topology = TopologyUtil.scriptWrapperStreamInstance(new StreamsBuilder(), topologyConfig)
  def driver = new TopologyTestDriver(topology, ConfigUtil.streamsConfig(kafkaConfig))
  def consumerFactory = new ConsumerRecordFactory(topologyConfig.topics.input, STRING_SERIALIZER, STRING_SERIALIZER)


  def cleanup() {
    driver.close()
  }


  def 'parse good granule and put it to an output topic'() {
    def message = [
        dataStream  : "dscovr",
        trackingId  : "3",
        checksum    : "456",
        relativePath: "oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_emb.nc.gz",
        path        : "/src/test/resources/oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_emb.nc.gz",
        fileSize    : 6526,
        lastUpdated : "2017124",
    ]

    when:
    driver.pipeInput(consumerFactory.create(topologyConfig.topics.input, message.trackingId, JsonOutput.toJson(message)))
    def output = driver.readOutput(topologyConfig.topics.output, STRING_DESERIALIZER, STRING_DESERIALIZER)

    then:
    output.key() == message.trackingId

    and:
    def attributes = new JsonSlurper().parseText(output.value()) as Map
    attributes.publishing.private == true
    attributes.discovery.fileIdentifier == "oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_emb.nc"
    attributes.discovery.parentIdentifier == "gov.noaa.ncei.swx:dscovr_f1m"
  }

  def 'parse bad granule and put it to an errorOut topic'() {
    def message = [
        dataStream  : "dscovr",
        trackingId  : "3",
        checksum    : "123",
        relativePath: "oe_f1m_dscovr_s20180129000000", // bad file name
        path        : "/src/test/resources/oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_emb.nc.gz",
        fileSize    : 26,
        lastUpdated : "2017124",
    ]

    when:
    driver.pipeInput(consumerFactory.create(topologyConfig.topics.input, message.trackingId, JsonOutput.toJson(message)))
    def output = driver.readOutput(topologyConfig.topics.errorout, STRING_DESERIALIZER, STRING_DESERIALIZER)

    then:
    output.key() == message.trackingId

    and:
    new JsonSlurper().parseText(output.value()).containsKey('error')
  }

}
