package org.cedar.psi.common.serde

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.test.ConsumerRecordFactory
import spock.lang.Specification


class JsonMapSerdeSpec extends Specification {

  def serde = new JsonMapSerde()

  def 'serializes a map'() {
    setup:
    def testMap = [hello: 'world']

    expect:
    serde.serializer().serialize('test', testMap) == '{"hello":"world"}'.bytes
  }

  def 'deserializes a map'() {
    setup:
    def testBytes = '{"hello":"world"}'.bytes

    expect:
    serde.deserializer().deserialize('test', testBytes) == [hello: 'world']
  }

  def 'works in a kstream app'() {
    setup:
    def value = [hello: 'world']
    def collector = []

    def builder = new StreamsBuilder()
    builder
        .stream('test_in', Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .peek({ k, v -> collector << v})
        .to('test_out', Produced.with(Serdes.String(), JsonSerdes.Map()))

    def driver = new TopologyTestDriver(builder.build(), buildTestStreamConfig())
    def consumerFactory = new ConsumerRecordFactory(Serdes.String().serializer(), serde.serializer())

    when:
    driver.pipeInput(consumerFactory.create('test_in', 'A', value))

    then:
    collector == [ value ]
    driver.readOutput('test_out').value() == '{"hello":"world"}'.bytes
  }

  def 'can materialize ktables'() {
    setup:
    def key = 'A'
    def value = [hello: 'world']
    def topicName = 'test_in'
    def tableName = 'test_table'

    def builder = new StreamsBuilder()
    builder
        .stream(topicName, Consumed.with(Serdes.String(), JsonSerdes.Map()))
        .groupByKey()
        .reduce({a, b -> b}, Materialized.as(tableName).withKeySerde(Serdes.String()).withValueSerde(JsonSerdes.Map()))

    def driver = new TopologyTestDriver(builder.build(), buildTestStreamConfig())
    def consumerFactory = new ConsumerRecordFactory(Serdes.String().serializer(), serde.serializer())

    when:
    driver.pipeInput(consumerFactory.create(topicName, key, value))

    then:
    driver.getKeyValueStore(tableName).get(key) == value
  }

  private static buildTestStreamConfig() {
    def streamConfig = new Properties()
    streamConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, 'testapp')
    streamConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 'localhost:9092')
    streamConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    return streamConfig
  }

}
