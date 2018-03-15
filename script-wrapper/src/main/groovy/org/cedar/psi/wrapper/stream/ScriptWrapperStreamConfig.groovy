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
import org.cedar.psi.wrapper.util.LoadConfigFile

@Slf4j
class ScriptWrapperStreamConfig {
    private static String inputTopic
    private static String outputTopic
    private static String command
    private static Boolean doIsoConversion = true
    private static long timeout
    private static String bootstrapServers
    private static String applicationId

    static void main(final String[] args) throws Exception {
        // config file can be set on
        Map configVlaue = LoadConfigFile.getConfig("script-wrapper")

        bootstrapServers = configVlaue.kafka.bootstrap.servers as String
        applicationId    = configVlaue.kafka.bootstrap as String
        inputTopic = configVlaue.stream.topics.input as String
        outputTopic = configVlaue.stream.topics.output as String
        command = configVlaue.stream.command as String
        timeout = configVlaue.stream.command_timeout as Long

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
        .mapValues({ msg ->
            if (doIsoConversion) {
                try {
                    isoConversionUtil.parseXMLMetadata(msg as String)
                }
                catch(e) {
                    log.error("Error parsing script output", e)
                    return "ERROR: ${e.message}"
                }
             } else {
                return msg
             }
        })
        .filterNot({key, msg -> msg.toString().startsWith('ERROR')})
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