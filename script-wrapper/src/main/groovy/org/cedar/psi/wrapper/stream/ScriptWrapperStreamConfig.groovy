package org.cedar.psi.wrapper.stream

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KStream
import org.cedar.psi.wrapper.util.IsoConversionUtil

@Slf4j
class ScriptWrapperStreamConfig {
    private static String inputTopic = "metadata-aggregator-raw-granule-changelog"
    private static String outputTopic = "parsed-granules"
    private static String command = "python /usr/src/app/scripts/dscovrIsoLite.py stdin"
    private static Boolean doIsoConversion = true
    private static long timeout = 5000
    private static String bootstrapServers = "kafka:9092"
    private static String applicationId = "dscovr-iso"

    static void main(final String[] args) throws Exception {

        log.info("streaming ...")

        final KafkaStreams streams = scriptWrapperStreamInstance()

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread({ streams.close() }))
    }

    static KafkaStreams scriptWrapperStreamInstance() {
        Properties streamsConfiguration = streamsConfig()
        IsoConversionUtil isoConversionUtil = new IsoConversionUtil()

        // stream DSL
        final StreamsBuilder builder = new StreamsBuilder()
        Topology topology = builder.build()
        final KStream<String, String> inputStream = builder.stream(inputTopic)
        inputStream.mapValues({ msg ->
            try{
                return ScriptWrapperFunctions.scriptCaller(msg, command, timeout)
            }catch(Exception e){
                log.error("Caught exception $e: ${e.message}")
            }
        })
        .filterNot({ key, msg -> msg.toString().startsWith('ERROR') })
        .mapValues({ msg -> doIsoConversion ? isoConversionUtil.parseXMLMetadata(msg as String) : msg })
        .to(outputTopic)

        final KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration)
        try{
            streams.start()
        }catch(Exception e){
            log.error("connection error $e")
        }

        return streams
    }

    private static Properties streamsConfig() {
        final Properties streamsConfiguration = new Properties()
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().class.name)
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500)
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        return streamsConfiguration
    }
}