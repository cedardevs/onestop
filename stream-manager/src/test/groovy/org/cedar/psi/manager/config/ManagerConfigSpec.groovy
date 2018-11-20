package org.cedar.psi.manager.config

import spock.lang.Specification

import static ManagerConfig.*


class ManagerConfigSpec extends Specification {

  def 'sets up defaults'() {
    def config = new ManagerConfig()

    expect:
    config.bootstrapServers() == BOOTSTRAP_SERVERS_DEFAULT
    config.schemaRegistryUrl() == SCHEMA_REGISTRY_URL_DEFAULT
  }

  def 'pulls values from env'() {
    def env = [
        (BOOTSTRAP_SERVERS_CONFIG): 'bootstrap:test',
        'SCHEMA_REGISTRY_URL': 'registry:test'
    ]
    def config = new ManagerConfig(env)

    expect:
    config.bootstrapServers() == 'bootstrap:test'
    config.schemaRegistryUrl() == 'registry:test'
  }

  def 'normalizes keys'() {
    def config = new ManagerConfig([
        'test.a': 'A',
        'TEST_B': 'B',
        'tEsT.C': 'C',
        'TeSt_D': 'D'
    ])

    expect:
    config['test.a'] == 'A'
    config['test.b'] == 'B'
    config['test.c'] == 'C'
    config['test.d'] == 'D'
  }

}
