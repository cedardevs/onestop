package org.cedar.psi.manager.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ManagerConfig implements Map<String, String> {

  static final String BOOTSTRAP_SERVERS_CONFIG = "kafka.bootstrap.servers";
  static final String BOOTSTRAP_SERVERS_DEFAULT = "http://localhost:9092";

  static final String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url";
  static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

  static final String COMPRESSION_TYPE_CONFIG = "kafka.compression.type";
  static final String COMPRESSION_TYPE_DEFAULT = "gzip";

  private final Map<String, String> internal;

  private static final Map<String, String> defaults = new LinkedHashMap<>();

  static {
    defaults.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_DEFAULT);
    defaults.put(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL_DEFAULT);
    defaults.put(COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE_DEFAULT);
  }

  public ManagerConfig(Map env) {
    internal = new LinkedHashMap<>(defaults);
    if (env != null) {
      env.forEach((k, v) -> internal.merge(normalizeKey(k), v.toString(), (v1, v2) -> v2));
    }
  }

  private String normalizeKey(Object key) {
    return key.toString().replaceAll("_", ".").toLowerCase();
  }

  public String bootstrapServers() { return internal.get(BOOTSTRAP_SERVERS_CONFIG); }
  public String schemaRegistryUrl() { return internal.get(SCHEMA_REGISTRY_URL_CONFIG); }
  public String compressionType() { return internal.get(COMPRESSION_TYPE_CONFIG); }


  //----- Delegated methods -----

  @Override
  public int size() {
    return internal.size();
  }

  @Override
  public boolean isEmpty() {
    return internal.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return internal.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return internal.containsValue(value);
  }

  @Override
  public String get(Object key) {
    return internal.get(key);
  }

  @Override
  public String put(String key, String value) {
    return internal.put(key, value);
  }

  @Override
  public String remove(Object key) {
    return internal.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    internal.putAll(m);
  }

  @Override
  public void clear() {
    internal.clear();
  }

  @Override
  public Set<String> keySet() {
    return internal.keySet();
  }

  @Override
  public Collection<String> values() {
    return internal.values();
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return internal.entrySet();
  }
}
