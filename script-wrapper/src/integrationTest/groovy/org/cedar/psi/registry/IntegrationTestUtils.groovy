package org.cedar.psi.registry

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.cedar.psi.wrapper.stream.ScriptWrapperFunctions
import org.cedar.psi.wrapper.util.IsoConversionUtil

@Slf4j
class IntegrationTestUtils {
    static void produceKeyValuesSynchronously(Properties configProps, String inputTopic, String data){
        def slurper = new JsonSlurper()
        def slurpedKey = slurper.parseText(data) as Map
        String key = slurpedKey.trackingId.toString()

        KafkaProducer kafkaProducer = new KafkaProducer<>(configProps)
        def record = new ProducerRecord<String, String>(inputTopic, key, data)

        log.debug("Sending: ${record}")

        kafkaProducer.send(record)

    }

    static KafkaStreams streamKeyValuesSynchronously(Properties streamsConfig, String inputTopic, String outputTopic, String command){
        Boolean doIsoConversion = true

        final StreamsBuilder builder = new StreamsBuilder()
        Topology topology = builder.build()
        final KStream<String, String> inputStream = builder.stream(inputTopic)
                .mapValues({ msg ->
                   return ScriptWrapperFunctions.scriptCaller(msg, command, 100)
                })
                .filterNot({ key, msg -> msg.toString().startsWith('ERROR') })
                .mapValues({ msg -> doIsoConversion ? IsoConversionUtil.parseXMLMetadata(msg as String) : msg })

        KGroupedStream groupedStream = inputStream.groupByKey()
        KTable mergedGranules = groupedStream.reduce(
                {aggregate, newValue -> newValue},
                Materialized.as(outputTopic)
        )
        return new KafkaStreams(topology, streamsConfig)
    }
}
