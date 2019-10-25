package org.cedar.onestop.manager.config

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import spock.lang.Specification

import static org.cedar.onestop.manager.config.ManagerConfig.*

class ManagerConfigSpec extends Specification {

  @Rule // Allows modification of environment variables; restores them after test completes
  EnvironmentVariables environmentVariables = new EnvironmentVariables()

  @Rule // Restores system properties after test completes
  RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties()

  def 'sets up defaults'() {
    def config = new ManagerConfig()

    expect:
    config.bootstrapServers() == BOOTSTRAP_SERVERS_DEFAULT
    config.schemaRegistryUrl() == SCHEMA_REGISTRY_URL_DEFAULT
    config.compressionType() == COMPRESSION_TYPE_DEFAULT
    config.cacheMaxBytes() == CACHE_MAX_BYTES_DEFAULT
    config.commitInterval() == COMMIT_INTERVAL_DEFAULT
    config.autoOffsetReset() == AUTO_OFFSET_RESET_DEFAULT
  }

  def 'config file overrides defaults for valid properties'() {
    given:
    def filePath = Thread.currentThread().contextClassLoader.getResource('test-config.yaml').file
    def config = new ManagerConfig(filePath)

    when:
    def actualMap = config.getCurrentConfigMap()

    then:
    actualMap.equals([
        "bootstrap.servers": "http://bootstrap-servers:3000",
        "schema.registry.url": "http://schema-registry:5000",
        "compression.type": "snappy",
        "cache.max.bytes.buffering": 209715200,
        "commit.interval.ms": 50000,
        "auto.offset.reset": "latest"
    ])
    !actualMap.containsKey('producer.one')
  }

  def 'uses defaults when config file not parsable'() {
    def filePath = Thread.currentThread().contextClassLoader.getResource('bad-config.txt').file
    def config = new ManagerConfig(filePath)

    expect:
    config.bootstrapServers() == BOOTSTRAP_SERVERS_DEFAULT
    config.schemaRegistryUrl() == SCHEMA_REGISTRY_URL_DEFAULT
    config.compressionType() == COMPRESSION_TYPE_DEFAULT
    config.cacheMaxBytes() == CACHE_MAX_BYTES_DEFAULT
    config.commitInterval() == COMMIT_INTERVAL_DEFAULT
    config.autoOffsetReset() == AUTO_OFFSET_RESET_DEFAULT
  }

  def 'environment variables override defaults'() {
    def newRegistryUrl = 'http://schemaUrl:9000'
    environmentVariables.set('KAFKA_SCHEMA_REGISTRY_URL', newRegistryUrl)
    def config = new ManagerConfig()

    expect:
    config.bootstrapServers() == BOOTSTRAP_SERVERS_DEFAULT
    config.schemaRegistryUrl() == newRegistryUrl
    config.compressionType() == COMPRESSION_TYPE_DEFAULT
    config.cacheMaxBytes() == CACHE_MAX_BYTES_DEFAULT
    config.commitInterval() == COMMIT_INTERVAL_DEFAULT
    config.autoOffsetReset() == AUTO_OFFSET_RESET_DEFAULT
  }

  def 'system properties override defaults'() {
    def newCompressionType = 'zstd'
    System.setProperty('KAFKA_COMPRESSION_TYPE', newCompressionType)
    def config = new ManagerConfig()

    expect:
    config.bootstrapServers() == BOOTSTRAP_SERVERS_DEFAULT
    config.schemaRegistryUrl() == SCHEMA_REGISTRY_URL_DEFAULT
    config.compressionType() == newCompressionType
    config.cacheMaxBytes() == CACHE_MAX_BYTES_DEFAULT
    config.commitInterval() == COMMIT_INTERVAL_DEFAULT
    config.autoOffsetReset() == AUTO_OFFSET_RESET_DEFAULT
  }

  def 'system properties override environment variables'() {
    def envBootstrap = 'http://envBootstrap:3000'
    def sysBootstrap = 'http://sysBootstrap:4000'
    environmentVariables.set('KAFKA_BOOTSTRAP_SERVERS', envBootstrap)
    System.setProperty('KAFKA_BOOTSTRAP_SERVERS', sysBootstrap)
    def config = new ManagerConfig()

    expect:
    config.bootstrapServers() == sysBootstrap
    config.schemaRegistryUrl() == SCHEMA_REGISTRY_URL_DEFAULT
    config.compressionType() == COMPRESSION_TYPE_DEFAULT
    config.cacheMaxBytes() == CACHE_MAX_BYTES_DEFAULT
    config.commitInterval() == COMMIT_INTERVAL_DEFAULT
    config.autoOffsetReset() == AUTO_OFFSET_RESET_DEFAULT
  }

  def 'config file overrides system properties and environment variables for valid properties'() {
    given:
    def newRegistryUrl = 'http://schemaUrl:9000'
    def newCompressionType = 'zstd'
    environmentVariables.set('KAFKA_SCHEMA_REGISTRY_URL', newRegistryUrl)
    System.setProperty('KAFKA_COMPRESSION_TYPE', newCompressionType)
    def filePath = Thread.currentThread().contextClassLoader.getResource('test-config.yaml').file
    def config = new ManagerConfig(filePath)

    when:
    def actualMap = config.getCurrentConfigMap()

    then:
    actualMap.equals([
        "bootstrap.servers": "http://bootstrap-servers:3000",
        "schema.registry.url": "http://schema-registry:5000",
        "compression.type": "snappy",
        "cache.max.bytes.buffering": 209715200,
        "commit.interval.ms": 50000,
        "auto.offset.reset": "latest"
    ])
    !actualMap.containsKey('producer.one')
  }
}
