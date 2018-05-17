package org.cedar.psi.splitter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.cedar.psi.splitter.stream.SplitterStreamMain
import org.junit.AfterClass
import org.junit.BeforeClass
import org.testcontainers.containers.KafkaContainer
import spock.lang.Specification


@Slf4j
class SplitterIntegrationSpec extends Specification{

    static KafkaContainer kafkaContainer = new KafkaContainer()

    @BeforeClass
    static void startContainer() throws Exception {
      kafkaContainer.start()
    }

    @AfterClass
    static void stopContainer() throws Exception {
      kafkaContainer.stop()
    }

    def topologyConfig = [
        input: [
            topic: 'test-input'
        ],
        split: [
            [
                datastreamOne : [
                    key: 'dataStream',
                    value: 'dscovr',
                    output: [topic: 'test-output']
                ]
            ]
        ]
    ]
    def kafkaConfig = [
        application: [id: 'test-app'],
        bootstrap: [servers: kafkaContainer.bootstrapServers]
    ]

    KafkaProducer<String, String> testProducer
    KafkaConsumer<String, String> testConsumer
    KafkaStreams testStream

    def setup() {
      testProducer = new KafkaProducer<>([
          (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)     : kafkaContainer.bootstrapServers,
          (ProducerConfig.ACKS_CONFIG)                  : "all",
          (ProducerConfig.RETRIES_CONFIG)               : 0,
          (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)  : StringSerializer,
          (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG): StringSerializer,
      ])
      testConsumer = new KafkaConsumer<>([
          (ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)       : kafkaContainer.bootstrapServers,
          (ConsumerConfig.GROUP_ID_CONFIG)                : 'integration-test-group',
          (ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)  : StringDeserializer,
          (ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG): StringDeserializer,
      ])

      testStream = SplitterStreamMain.buildStreamsApp(kafkaConfig, topologyConfig)
      testStream.cleanUp()
      testStream.start()
    }

    def cleanup() {
      testProducer.close()
      testConsumer.close()
      testStream.close()
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
      def input = new ProducerRecord(topologyConfig.input.topic, message.trackingId, JsonOutput.toJson(message))
      testProducer.send(input)

      testConsumer.subscribe([topologyConfig.split[0].datastreamOne.output.topic])
      def output = testConsumer.poll(10000)

      then:
      assert true
      output.first().key() == message.trackingId

    }

//    def 'parse bad granule and put it to an errorOut topic'() {
//      def message = [
//          dataStream  : "dscovr",
//          trackingId  : "3",
//          checksum    : "123",
//          relativePath: "oe_f1m_dscovr_s20180129000000", // bad file name
//          path        : "/src/test/resources/oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_emb.nc.gz",
//          fileSize    : 26,
//          lastUpdated : "2017124",
//      ]
//
//      when:
//      def input = new ProducerRecord(topologyConfig.topics.input, message.trackingId, JsonOutput.toJson(message))
//      testProducer.send(input)
//      testConsumer.subscribe([topologyConfig.topics.errorout])
//      def output = testConsumer.poll(10000).first()
//
//      then:
//      output.key() == message.trackingId
//
//      and:
//      new JsonSlurper().parseText(output.value()).containsKey('error')
//    }


}
