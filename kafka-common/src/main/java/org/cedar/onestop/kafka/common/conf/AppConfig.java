package org.cedar.onestop.kafka.common.conf;

import org.cedar.onestop.data.util.MapUtils;
import org.cedar.onestop.kafka.common.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AppConfig {
  private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

  public static final String CONFIG_FILE_ENV_VAR = "CONFIG_LOCATION";
  private static final SafeConstructor safeConstructor = new SafeConstructor(new LoaderOptions());

  private final Map<String, Object> defaults;
  private final Map<Object, Object> systemProperties;
  private final Map<String, String> environmentVariables;
  private final Map<String, Object> configFileProperties;
  private final Map<String, Object> combined;

  public AppConfig() {
    this(System.getenv(CONFIG_FILE_ENV_VAR));
  }

  public AppConfig(String filePath) {
    this.defaults = getDefaults();
    this.systemProperties = System.getProperties();
    this.environmentVariables = getEnv();
    this.configFileProperties = parseYamlConfigFile(filePath);
    this.combined = buildCombinedMap();
  }

  private Map<String, Object> buildCombinedMap() {
    // Starting with defaults, merge in order of least --> most preferred props (defaults, sys, env, file).
    var combinedConfig = new LinkedHashMap<>(defaults);
    systemProperties.forEach((k, v) -> combinedConfig.put(normalizeKey(k.toString()), v));
    environmentVariables.forEach((k, v) -> combinedConfig.put(normalizeKey(k), v));
    configFileProperties.forEach((k, v) -> combinedConfig.put(normalizeKey(k), v));

    if (log.isDebugEnabled()) {
      log.debug("Combined app config:");
      combinedConfig.forEach((s, o) -> log.debug("  " + s + ": " + (s.toString().contains("password") ? "[Redacted]" : o)));
    }

    return combinedConfig;
  }

  private static Map<String, Object> parseYamlConfigFile(String filePath) {
    if (filePath == null || filePath.isBlank()) {
      return Collections.emptyMap();
    }
    try {
      Yaml yaml = new Yaml(safeConstructor);
      InputStream inputStream = Files.newInputStream(Path.of(filePath));
      Map<String, Object> configFileMap = yaml.load(inputStream);
      return MapUtils.consolidateNestedKeysInMap(null, ".", configFileMap);
    }
    catch (IOException e) {
      log.error("Cannot open config file path [ " + filePath + " ]. Using defaults and/or system/environment variables.", e);
    }
    catch (Exception e) {
      // In case there's any other exception trying to parse the file...
      log.error("Cannot parse config file path [ " + filePath + " ] as YAML. Using defaults and/or system/environment variables.", e);
    }
    return new LinkedHashMap<>();
  }

  private static Map<String, Object> getDefaults() {
    Yaml yaml = new Yaml(safeConstructor);
    InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("application.yml");
    if (input == null) {
      return Collections.emptyMap();
    }
    else {
      Map<String, Object> config = yaml.load(input);
      return MapUtils.consolidateNestedKeysInMap(null, ".", config);
    }
  }

  private static Map<String, String> getEnv() {
    try {
      return System.getenv();
    }
    catch (SecurityException e) {
      log.error("Application does not have permission to read environment variables. Using defaults.");
      return Collections.emptyMap();
    }
  }

  // lower case, replace '_' and '-' with '.'
  static private String normalizeKey(String key) {
    return Optional.ofNullable(key)
        .map(String::toLowerCase)
        .map(s -> s.replaceAll("[_-]", "."))
        .orElse(null);
  }

  /**
   * Returns an unmodifiable map reflecting the current internal map of config values. Future changes to
   * the internal map will not be reflected here.
   *
   * @return Map containing entries of ManagerConfig's map
   */
  public Map<String, Object> getCurrentConfigMap() {
    return Map.copyOf(combined);
  }

  public int size() {
    return combined.size();
  }

  public boolean isEmpty() {
    return combined.isEmpty();
  }

  public boolean containsKey(Object key) {
    return combined.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return combined.containsValue(value);
  }

  public Object get(Object key) {
    return combined.get(key);
  }

  public Object getOrDefault(Object key, Object defaultValue) {
    return combined.getOrDefault(key, defaultValue);
  }

  /* Alternative getOrDefault that returns the value (or default) cast as the requested class
     Intended to provide protection against wrong types in yaml
     Note: only works for objects within the same hierarchy!!! (e.g. Integer -> String would fail)
  */
  public <T> T getOrDefault(Object key, T defaultValue, Class<T> clazz) {
    var result = combined.getOrDefault(key, defaultValue);
    try {
      return clazz.cast(result);
    } catch (ClassCastException e) {
      log.error("Error while trying to cast key "+key+" with value or default "+result+". Exception: "+e);
      log.error("Returning default: "+defaultValue+" ("+defaultValue.getClass()+")");
      return defaultValue;
    }
  }

}
