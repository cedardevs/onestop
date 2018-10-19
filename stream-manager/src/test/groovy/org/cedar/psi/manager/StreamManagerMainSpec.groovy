package org.cedar.psi.manager

import spock.lang.Specification


class StreamManagerMainSpec extends Specification {

  def 'pulls bootstrap config from env'() {
    def envServers = 'env:9092'
    def env = ['KAFKA_BOOTSTRAP_SERVERS': envServers]

    expect:
    StreamManagerMain.getBootstrapServers(env, null) == envServers
  }

  def 'pulls bootstrap config from args'() {
    def envServers = 'env:9092'
    def argServers = 'arg:9092'
    def env = ['KAFKA_BOOTSTRAP_SERVERS': envServers]
    def args = [argServers] as String[]

    expect:
    StreamManagerMain.getBootstrapServers(env, args) == argServers
  }

}
