package org.cedar.psi.splitter.util

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import spock.lang.Specification


class TopologyUtilSpec extends Specification  {

  def STRING_SERIALIZER = Serdes.String().serializer()
  def STRING_DESERIALIZER = Serdes.String().deserializer()


  def streamConfig = [
      (StreamsConfig.APPLICATION_ID_CONFIG)           : 'delayed_publisher_spec',
      (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)        : 'localhost:9092',
      (StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG)  : Serdes.String().class.name,
      (StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG): Serdes.String().class.name,
      (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest'
  ]

  def INPUT_TOPIC = 'test-input'
  def OUTPUT_TOPIC_ONE = 'datastreamOne-output'
  def OUTPUT_TOPIC_TWO = 'datastreamTwo-output'

  def topologyConfig = [
      input: [
          topic: INPUT_TOPIC
      ],
      split: [
          [
              datastreamOne : [
                  key: 'dataStream',
                  value: 'dscovr',
                  output: [topic: OUTPUT_TOPIC_ONE]
              ]
          ],
          [
              datastreamTwo : [
                  key: 'type',
                  value: 'iso-granule',
                  output: [topic: OUTPUT_TOPIC_TWO]
              ]
          ]
      ]
  ]

  Topology topology = TopologyUtil.splitterStreamInstance(topologyConfig)
  def driver = new TopologyTestDriver(topology, new Properties(streamConfig))
  def consumerFactory = new ConsumerRecordFactory(INPUT_TOPIC, STRING_SERIALIZER, STRING_SERIALIZER)

  def 'splits a message to appropriate topics'() {
    def keyOne = topologyConfig.split[0].datastreamOne.key
    def valueOne = topologyConfig.split[0].datastreamOne.value
    def keyTwo = topologyConfig.split[1].datastreamTwo.key
    def valueTwo = topologyConfig.split[1].datastreamTwo.value

    def idOne = 'A'
    def idTwo = 'B'

    String value1 = /{"id":"A","$keyOne":"$valueOne"}/
    String value2 = /{"id":"B","$keyTwo":"$valueTwo"}/

    when:
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, idOne, value1))
    driver.pipeInput(consumerFactory.create(INPUT_TOPIC, idTwo, value2))

    then:
    assert value1 == driver.readOutput(OUTPUT_TOPIC_ONE, STRING_DESERIALIZER, STRING_DESERIALIZER).value()
    assert value2 == driver.readOutput(OUTPUT_TOPIC_TWO, STRING_DESERIALIZER, STRING_DESERIALIZER).value()
  }
}
