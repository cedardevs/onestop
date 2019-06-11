package org.cedar.psi.manager.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ManagerConfig implements Map<String, Object> {

  static final String BOOTSTRAP_SERVERS_CONFIG = "kafka.bootstrap.servers";
  static final String BOOTSTRAP_SERVERS_DEFAULT = "http://localhost:9092";

  static final String CACHE_MAX_BYTES_CONFIG = "kafka.cache.max.bytes.buffering";
  static final Long CACHE_MAX_BYTES_DEFAULT = 104857600L; // 100 MiB

  static final String COMMIT_INTERVAL_CONFIG = "kafka.commit.interval.ms";
  static final Long COMMIT_INTERVAL_DEFAULT = 30000L;

  static final String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url";
  static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

  static final String COMPRESSION_TYPE_CONFIG = "kafka.compression.type";
  static final String COMPRESSION_TYPE_DEFAULT = "gzip";

  private final Map<String, Object> internal;

  private static final Map<String, Object> defaults = new LinkedHashMap<>();

  static {
    defaults.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_DEFAULT);
    defaults.put(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL_DEFAULT);
    defaults.put(COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE_DEFAULT);
    defaults.put(CACHE_MAX_BYTES_CONFIG, CACHE_MAX_BYTES_DEFAULT);
    defaults.put(COMMIT_INTERVAL_CONFIG, COMMIT_INTERVAL_DEFAULT);
  }

  public ManagerConfig(Map env) {
    internal = new LinkedHashMap<String, Object>(defaults);
    if (env != null) {
      env.forEach((k, v) -> internal.merge(normalizeKey(k), v.toString(), (v1, v2) -> v2));
    }
  }

  private String normalizeKey(Object key) {
    return key.toString().replaceAll("_", ".").toLowerCase();
  }

  public String bootstrapServers() { return (String) internal.get(BOOTSTRAP_SERVERS_CONFIG); }
  public String schemaRegistryUrl() { return (String) internal.get(SCHEMA_REGISTRY_URL_CONFIG); }
  public String compressionType() { return (String) internal.get(COMPRESSION_TYPE_CONFIG); }
  public Long cacheMaxBytes() { return (Long) internal.get(CACHE_MAX_BYTES_CONFIG); }
  public Long commitInterval() { return (Long) internal.get(COMMIT_INTERVAL_CONFIG); }


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
  public Object get(Object key) {
    return internal.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return internal.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return internal.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
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
  public Collection<Object> values() {
    return internal.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return internal.entrySet();
  }
}
