package org.cedar.onestop.indexer.util

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory

import java.time.Duration
import java.time.Instant


//---------
// Polyfills (remove w/ kafka 2.4+)
// --------
class StreamsTestingPolyfills {

  static applyPolyfills(TopologyTestDriver driver) {
    driver.metaClass.createInputTopic = { String s, Serializer k, Serializer v, Instant i, Duration d ->
      new TestInputTopic(driver, s, k, v, i, d)
    }
    driver.metaClass.createOutputTopic = { String s, Deserializer k, Deserializer v ->
      new TestOutputTopic(driver, s, k, v)
    }
    driver.metaClass.advanceWallClockTime << { Duration d ->
      driver.advanceWallClockTime(d.toMillis())
    }
  }

  static class TestInputTopic<K, V> {
    TopologyTestDriver driver
    String topic
    Serializer keySerializer
    Serializer valueSerializer
    Instant publishTime
    Duration publishInterval

    ConsumerRecordFactory inputFactory

    TestInputTopic(TopologyTestDriver driver, String topic, Serializer<K> keySerializer, Serializer<V> valueSerializer, Instant startTime, Duration publishInterval) {
      this.driver = driver
      this.topic = topic
      this.keySerializer = keySerializer
      this.valueSerializer = valueSerializer
      this.publishTime = startTime
      this.publishInterval = publishInterval

      this.inputFactory = new ConsumerRecordFactory(keySerializer, valueSerializer)
    }

    void pipeInput(K key, V value) {
      driver.pipeInput(inputFactory.create(topic, key, value, publishTime.toEpochMilli()))
      publishTime == publishTime.plus(publishInterval)
    }
  }

  static class TestOutputTopic<K, V> {
    TopologyTestDriver driver
    String topic
    Deserializer keyDeserializer
    Deserializer valueDeserializer

    TestOutputTopic(TopologyTestDriver driver, String topic, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
      this.driver = driver
      this.topic = topic
      this.keyDeserializer = keyDeserializer
      this.valueDeserializer = valueDeserializer
    }

    Map<K, V> readKeyValuesToMap() {
      def result = new LinkedHashMap<K, V>()
      while(true) {
        def record = driver.readOutput(topic, keyDeserializer, valueDeserializer)
        if (record) {
          result.put(record.key(), record.value())
        }
        else {
          return result
        }
      }
    }
  }

}
