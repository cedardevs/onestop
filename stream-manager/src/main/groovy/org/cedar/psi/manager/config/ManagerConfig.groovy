package org.cedar.psi.manager.config

import groovy.transform.CompileStatic

@CompileStatic
class ManagerConfig implements Map<String, String> {

  static final String BOOTSTRAP_SERVERS_CONFIG = 'kafka.bootstrap.servers'
  static final String BOOTSTRAP_SERVERS_DEFAULT = 'http://localhost:9092'

  static final String SCHEMA_REGISTRY_URL_CONFIG = 'schema.registry.url'
  static final String SCHEMA_REGISTRY_URL_DEFAULT = 'http://localhost:8081'

  @Delegate
  private Map<String, String> internal

  ManagerConfig() {
    internal = [
        (BOOTSTRAP_SERVERS_CONFIG): BOOTSTRAP_SERVERS_DEFAULT,
        (SCHEMA_REGISTRY_URL_CONFIG): SCHEMA_REGISTRY_URL_DEFAULT
    ]
  }

  ManagerConfig(Map env) {
    internal = env.inject([:], { result, k, v ->
      def normalizedKey = k.toString().replaceAll('_', '.').toLowerCase()
      result[normalizedKey] = v.toString()
      result
    }) as Map<String, String>
  }

  String bootstrapServers() { internal[BOOTSTRAP_SERVERS_CONFIG] }
  String schemaRegistryUrl() { internal[SCHEMA_REGISTRY_URL_CONFIG] }

}
