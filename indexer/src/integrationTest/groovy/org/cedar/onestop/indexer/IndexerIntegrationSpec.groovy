package org.cedar.onestop.indexer

import org.apache.kafka.clients.admin.AdminClient
import org.cedar.onestop.indexer.util.ElasticsearchFactory
import org.cedar.onestop.kafka.common.conf.AppConfig
import org.cedar.onestop.kafka.common.util.KafkaHelpers
import org.elasticsearch.client.RequestOptions
import org.springframework.kafka.test.EmbeddedKafkaBroker
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class IndexerIntegrationSpec extends Specification {

  static EmbeddedKafkaBroker kafka = new EmbeddedKafkaBroker(1, false)
      .brokerListProperty('kafka.bootstrap.servers')

  def setupSpec() {
    kafka.afterPropertiesSet()
  }

  def cleanupSpec() {
    kafka.destroy()
  }

  def "connects to ES"() {
    def config = new AppConfig()
    def client = ElasticsearchFactory.buildElasticClient(config)

    expect:
    client.info(RequestOptions.DEFAULT).version.number instanceof String
  }

  def "connects to kafka"() {
    def config = new AppConfig()
    def props = KafkaHelpers.buildAdminConfig(config)
    def client = AdminClient.create(props)

    expect:
    client.describeCluster().clusterId().get(10, TimeUnit.SECONDS) instanceof String
    println client.describeCluster().nodes().get()[0]
  }

  def "indexes a collection and granule"() {
    // TODO
  }

}
