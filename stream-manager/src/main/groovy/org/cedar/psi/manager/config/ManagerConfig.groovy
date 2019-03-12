package org.cedar.psi.manager.config

import groovy.transform.CompileStatic

@CompileStatic
class ManagerConfig implements Map<String, String> {

  static final String BOOTSTRAP_SERVERS_CONFIG = 'kafka.bootstrap.servers'
  static final String BOOTSTRAP_SERVERS_DEFAULT = 'http://localhost:9092'

  static final String SCHEMA_REGISTRY_URL_CONFIG = 'schema.registry.url'
  static final String SCHEMA_REGISTRY_URL_DEFAULT = 'http://localhost:8081'

  static final String COMPRESSION_TYPE_CONFIG = 'kafka.compression.type'
  static final String COMPRESSION_TYPE_DEFAULT = 'gzip'

  @Delegate
  private Map<String, String> internal

  private final Map defaults = [
      (BOOTSTRAP_SERVERS_CONFIG): BOOTSTRAP_SERVERS_DEFAULT,
      (SCHEMA_REGISTRY_URL_CONFIG): SCHEMA_REGISTRY_URL_DEFAULT,
      (COMPRESSION_TYPE_CONFIG): COMPRESSION_TYPE_DEFAULT
  ]

  ManagerConfig(Map env = [:]) {
    internal = env.inject(defaults, { result, k, v ->
      def normalizedKey = k.toString().replaceAll('_', '.').toLowerCase()
      result[normalizedKey] = v.toString()
      result
    }) as Map<String, String>
  }

  String bootstrapServers() { internal[BOOTSTRAP_SERVERS_CONFIG] }
  String schemaRegistryUrl() { internal[SCHEMA_REGISTRY_URL_CONFIG] }
  String compressionType() { internal[COMPRESSION_TYPE_CONFIG] }

}
