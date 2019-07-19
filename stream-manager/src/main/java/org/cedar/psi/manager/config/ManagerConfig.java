package org.cedar.psi.manager.config;

import org.apache.kafka.common.config.TopicConfig;
import org.cedar.psi.common.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.*;

public class ManagerConfig {

  private static final Logger log = LoggerFactory.getLogger(ManagerConfig.class);

  // Needed default values for starting (in case no config provided)
  static final String BOOTSTRAP_SERVERS_DEFAULT = "http://localhost:9092";
  static final Long CACHE_MAX_BYTES_DEFAULT = 104857600L; // 100 MiB
  static final Long COMMIT_INTERVAL_DEFAULT = 30000L;
  static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";
  static final String COMPRESSION_TYPE_DEFAULT = "gzip";
  static final String AUTO_OFFSET_RESET_DEFAULT = "earliest";

  private final Map<String, Object> internal;

  private final Map<String, String> environmentVariables;
  private final Map<Object, Object> systemProperties;
  private final Map<String, Object> configFileProperties;

  private static final Map<String, Object> defaults = new LinkedHashMap<>();

  static {
    defaults.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_DEFAULT);
    defaults.put(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL_DEFAULT);
    defaults.put(TopicConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE_DEFAULT);
    defaults.put(CACHE_MAX_BYTES_BUFFERING_CONFIG, CACHE_MAX_BYTES_DEFAULT);
    defaults.put(COMMIT_INTERVAL_MS_CONFIG, COMMIT_INTERVAL_DEFAULT);
    defaults.put(AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_DEFAULT);
  }

  public ManagerConfig() {
    internal = new LinkedHashMap<String, Object>(defaults);
    environmentVariables = getEnv();
    systemProperties = System.getProperties();
    configFileProperties = new LinkedHashMap<>();
    updateInternalMapping();
  }

  public ManagerConfig(String filePath) {
    internal = new LinkedHashMap<String, Object>(defaults);
    environmentVariables = getEnv();
    systemProperties = System.getProperties();
    configFileProperties = parseYamlConfigFile(filePath);
    updateInternalMapping();
  }

  public ManagerConfig(Map<String, Object> configFileProps) {
    internal = new LinkedHashMap<String, Object>(defaults);
    environmentVariables = getEnv();
    systemProperties = System.getProperties();
    configFileProperties = configFileProps;
    updateInternalMapping();
  }

  private Map<String, Object> parseYamlConfigFile(String filePath) {
    Map<String, Object> yamlKafkaConfigs = new LinkedHashMap<>();
    Yaml yaml = new Yaml();
    try {
      InputStream inputStream = Files.newInputStream(Path.of(filePath));
      Map<String, Object> configFileMap = yaml.load(inputStream);

      // Note -- if anything other than "kafka" config values are in the yaml file, we are currently dropping them.
      yamlKafkaConfigs = DataUtils.consolidateNestedKeysInMap(null, ".", (Map<String, Object>) configFileMap.get("kafka"));
    } catch (IOException e) {
      log.error("Cannot open config file path [ " + filePath + " ]. Using defaults and/or system/environment variables.");
    }
    catch (Exception e) {
      // In case there's any other exception trying to parse the file...
      log.error("Cannot parse config file path [ " + filePath + " ] as YAML. Using defaults and/or system/environment variables.");
    }
    return yamlKafkaConfigs;
  }

  private static Map<String, String> getEnv() {
    try {
      return System.getenv();
    }
    catch(SecurityException e) {
      log.error("Application does not have permission to read environment variables. Using defaults.");
      return Collections.EMPTY_MAP;
    }
  }

  private void updateInternalMapping() {
    // Internal starts as defaults; merge in order of least --> most preferred props (env, sys, file)
    // NOTE: Any environment or system properties are prefixed with "kafka." so remove the prefix before merging (Kafka
    // config values do NOT have this prefix)
    environmentVariables.forEach((k, v) -> internal.merge(normalizeKey(k, "kafka."), v.toString(), (v1, v2) -> v2));
    systemProperties.forEach((k, v) -> internal.merge(normalizeKey(k, "kafka."), v.toString(), (v1, v2) -> v2));

    internal.putAll(configFileProperties);
  }

  private String normalizeKey(Object key, String caseInsensitivePrefix) {
    String newKey = key.toString().toLowerCase();
    String prefix = caseInsensitivePrefix.toLowerCase();
    if(prefix != null && !prefix.isEmpty() && newKey.startsWith(prefix)) {
      newKey = newKey.substring(prefix.length());
    }
    return newKey.replaceAll("_", ".");
  }


  public String bootstrapServers() { return (String) internal.get(BOOTSTRAP_SERVERS_CONFIG); }
  public String schemaRegistryUrl() { return (String) internal.get(SCHEMA_REGISTRY_URL_CONFIG); }
  public String compressionType() { return (String) internal.get(TopicConfig.COMPRESSION_TYPE_CONFIG); }
  public Long cacheMaxBytes() { return (Long) internal.get(CACHE_MAX_BYTES_BUFFERING_CONFIG); }
  public Long commitInterval() { return (Long) internal.get(COMMIT_INTERVAL_MS_CONFIG); }


  /**
   * Returns an unmodifiable map reflecting the current internal map in the ManagerConfig instance. Future changes to
   * the internal map will not be reflected here.
   * @return Map containing entries of ManagerConfig's map
   */
  public Map<String, Object> getCurrentConfigMap() {
    return Map.copyOf(internal);
  }

  public int size() {
    return internal.size();
  }


  public boolean isEmpty() {
    return internal.isEmpty();
  }


  public boolean containsKey(Object key) {
    return internal.containsKey(key);
  }


  public boolean containsValue(Object value) {
    return internal.containsValue(value);
  }


  public Object get(Object key) {
    return internal.get(key);
  }

}
