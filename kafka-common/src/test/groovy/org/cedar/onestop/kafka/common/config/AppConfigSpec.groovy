package org.cedar.onestop.kafka.common.config

import org.cedar.onestop.kafka.common.conf.AppConfig
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import spock.lang.Specification

class AppConfigSpec extends Specification {

  @Rule
  // Allows modification of environment variables; restores them after test completes
  EnvironmentVariables environmentVariables = new EnvironmentVariables()

  @Rule
  // Restores system properties after test completes
  RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties()

  def 'sets up defaults'() {
    def config = new AppConfig()

    expect:
    config.get("test.default") == "thetestvalue"
  }

  def 'config file overrides defaults for valid properties'() {
    given:
    def filePath = Thread.currentThread().contextClassLoader.getResource('test-config.yaml').file
    def config = new AppConfig(filePath)

    when:
    def result = config.getCurrentConfigMap()

    then:
    result.get("kafka.bootstrap.servers") == "http://bootstrap-servers:3000"
    result.get("kafka.schema.registry.url") == "http://schema-registry:5000"
    result.get("kafka.compression.type") == "snappy"
    result.get("kafka.cache.max.bytes.buffering") == 209715200
    result.get("kafka.commit.interval.ms") == 50000
    result.get("kafka.auto.offset.reset") == "latest"
    result.get("streams.exception.max.time.millis") == 2147483647 // Must fit within an int. See UncaughtExceptionHandler.
    !result.containsKey('producer.one')
  }

  def 'uses defaults when config file not parsable'() {
    def filePath = Thread.currentThread().contextClassLoader.getResource('bad-config.txt').file
    def config = new AppConfig(filePath)

    expect:
    config.get("kafka.bootstrap.servers") == "http://localhost:9092"
  }

  def 'environment variables override defaults'() {
    def newRegistryUrl = 'http://schemaUrl:9000'
    environmentVariables.set('KAFKA_SCHEMA_REGISTRY_URL', newRegistryUrl)
    def config = new AppConfig()

    expect:
    config.get("kafka.schema.registry.url") == newRegistryUrl
  }

  def 'system properties override defaults'() {
    def newCompressionType = 'zstd'
    System.setProperty('KAFKA_COMPRESSION_TYPE', newCompressionType)
    def config = new AppConfig()

    expect:
    config.get("kafka.compression.type") == newCompressionType
  }

  def 'environment variables override system properties'() {
    def envBootstrap = 'http://envBootstrap:3000'
    def sysBootstrap = 'http://sysBootstrap:4000'
    environmentVariables.set('KAFKA_BOOTSTRAP_SERVERS', envBootstrap)
    System.setProperty('KAFKA_BOOTSTRAP_SERVERS', sysBootstrap)
    def config = new AppConfig()

    expect:
    config.get("kafka.bootstrap.servers") == envBootstrap
  }

  def 'config file overrides system properties and environment variables for valid properties'() {
    given:
    def newRegistryUrl = 'http://schemaUrl:9000'
    def newCompressionType = 'zstd'
    environmentVariables.set('KAFKA_SCHEMA_REGISTRY_URL', newRegistryUrl)
    System.setProperty('KAFKA_COMPRESSION_TYPE', newCompressionType)
    def filePath = Thread.currentThread().contextClassLoader.getResource('test-config.yaml').file
    def config = new AppConfig(filePath)

    when:
    def actualMap = config.getCurrentConfigMap()

    then:
    actualMap.get("kafka.bootstrap.servers") == "http://bootstrap-servers:3000"
    actualMap.get("kafka.schema.registry.url") == "http://schema-registry:5000"
    actualMap.get("kafka.compression.type") == "snappy"
    actualMap.get("kafka.cache.max.bytes.buffering") == 209715200
    actualMap.get("kafka.commit.interval.ms") == 50000
    actualMap.get("kafka.auto.offset.reset") == "latest"
    !actualMap.containsKey('producer.one')
  }

  def 'type casting works with getOrDefault'() {
    def filePath = Thread.currentThread().contextClassLoader.getResource('test-config.yaml').file
    def config = new AppConfig(filePath)

    expect:
    // Integer works, default is unused
    config.getOrDefault("kafka.cache.max.bytes.buffering", 2000, Integer.class) == 209715200

    // Key found, value fails cast so default is used (e.g. bad type in yaml)
    config.getOrDefault("kafka.cache.max.bytes.buffering", 2010, String.class) == 2010

    // Key not found, default value returned (technically no casting required)
    config.getOrDefault("keynotfound", "2020", String.class) == "2020"

    // Key not found, default value fails to cast to Integer so returns a String
    // (Downstream code must accept a String or Integer)
    config.getOrDefault("keynotfound", "2030", Integer.class) == "2030"
  }
}
