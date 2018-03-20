package org.cedar.psi.wrapper

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.testcontainers.containers.KafkaContainer
import spock.lang.Specification

import java.nio.file.Paths

@Slf4j
class WrapperIntegrationSpec extends Specification{

    private static KafkaContainer kafkaContainer = new KafkaContainer()
    private static Properties producerConfig
    private static Properties streamsConfiguration
    @BeforeClass
    static void startContainer() throws Exception {
        kafkaContainer.start()
    }

    @AfterClass
    static void stopContainer() throws Exception {
        kafkaContainer.stop()
    }

    def setup() {
        producerConfig = new Properties()
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all")
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0)
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class)

        streamsConfiguration = new Properties()
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wrapper-test")
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.bootstrapServers)
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500)
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams")
    }

    @Test
    def 'retrieve raw data from output topic '() {
        def granuleText = '{' +
                    '"dataStream": "dscover", ' +
                    '"trackingId": "3", ' +
                    '"checksum": "fd297fcceb94fdbec5297938c99cc7b5", ' +
                    '"relativePath": "oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_pub.nc.gz", ' +
                    '"path": "/src/test/resources/oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_pub.nc.gz", ' +
                    '"fileSize": 6526, ' +
                    '"lastUpdated":"2017124"' +
                '}'

        def inputTopic = "wrapper-inputTopic"
        def outputTopic = "wrapper-outputTopic"
        String currentDir = Paths.get(".").toAbsolutePath().normalize().toString()
        String command = "python ${currentDir}/scripts/dscovrIsoLite.py stdin"

        when:
        // Step 1: Produce some input data to the input topic.
        IntegrationTestUtils.produceKeyValuesSynchronously(producerConfig, inputTopic , granuleText.toString())

        // Step 2: Verify the application's output data.
        def stream = IntegrationTestUtils.streamKeyValuesSynchronously(streamsConfiguration, inputTopic, outputTopic, command)

        stream.setStateListener({newValue,oldValue ->
            if (newValue == KafkaStreams.State.RUNNING && oldValue.REBALANCING){
                println(stream.allMetadataForStore("wrapper-outputTopic").size())
            }
        })

        stream.cleanUp()
        stream.start()

        Thread.sleep((10000))
        // the store added for test purposes
        def store = stream.store("wrapper-outputTopic", QueryableStoreTypes.keyValueStore())
        def value =  store.get("3")
        def slurper = new JsonSlurper()
        def attributes = slurper.parseText(value as String) as Map

        then:
        attributes.fileIdentifier == "oe_f1m_dscovr_s20180129000000_e20180129235959_p20180130024119_pub.nc"
        attributes.parentIdentifier == "gov.noaa.ncei.swx:dscovr_f1m"

    }
}
