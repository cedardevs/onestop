package org.cedar.psi.splitter.util

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.yaml.snakeyaml.Yaml

@Slf4j
class ConfigUtil {

  //resolve ENV vars that might be in YAML
  def static replaceEnv(String yamlString) {
    if (!yamlString) return yamlString
    yamlString.replaceAll(
        /\$\{([^\s\}]+)\}/) {
      System.getenv(it[1]) ?: it[0]
    }
  }

  static getConfig(String name) {
    log.info "Retrieving configuration... "
    Map conf = [:]
    Yaml yaml = new Yaml()
    String configPath = System.getenv('PSI_CONFIG_LOCATION') ? "file://${System.getenv('PSI_CONFIG_LOCATION')}" :  System.properties['psi.config.location'] ?: "file://" + System.getProperty("user.dir") + "/${name}.yml"
    URL url = new URL(configPath)

    try{
      log.info "Loading config at $url"
      conf = (Map<String, Object>) yaml.load(replaceEnv(url.openStream().text) as String)
    }catch(e){
      log.warn "Failed to find config file: ${e.message}"
    }
    Map compositeConfig = mergeEnvConfig(conf)
    log.info "Starting with working configuration: ${compositeConfig} "
    compositeConfig
  }

  static mergeEnvConfig(Map conf){
    log.info "Searching environment for configuration settings"
    Map<String, String> env = System.getenv()
    Map psiConfig = env.subMap(env.keySet().findAll({k -> k.startsWith('PSI')}) as List)
    log.info "Found configuration settings: $psiConfig"
    psiConfig.inject(conf ?: [:]) { Map result, String k, String v ->
      List keys = k.toLowerCase().replace('psi_', '').tokenize('_')
      result + insertEnvConfig(result, keys, v)
    }
  }

  static Map insertEnvConfig(def conf, List keys, String value){
    Map newMap = (conf && conf instanceof Map) ? conf : [:]
    String k = keys[0] ?: null
    if(keys.size() == 1){
      newMap.put(k, value)
      return newMap
    }else{
      keys.remove(0)
      newMap.put(k, insertEnvConfig(conf?.get(k) ?: [:], keys, value))
      return newMap
    }
  }

  static Map validateKafkaConfig(Map config){
    log.info  "Validating kafka config: $config"
    if (!config?.application?.id){ throw new RuntimeException('Cannot resolve ${kafka.application.id}')}
    if (!config?.bootstrap?.servers){ throw new RuntimeException('Cannot resolve ${kafka.bootstrap.servers}')}
    log.info "Kafka config is valid"
    return config
  }

  static Map validateTopologyConfig(Map config){
    log.info "Validating topology config: $config"
    if (!config?.input?.topic){ throw new RuntimeException('Cannot resolve ${stream.input.topic}')}
    if (!config?.split || !(config?.split instanceof List)){ throw new RuntimeException('Cannot resolve ${stream.split}')}
    config.split.each{ stream ->
      stream.each{k, v ->
        log.info "validating stream: ${k}"
        if(!(v.containsKey('key') && v.containsKey('value'))){
          throw new RuntimeException( "Stream ${k} does not specify a key and value to split on")
        }
        if(!v.containsKey('output') || !(v.get('output').containsKey('topic'))){
          throw new RuntimeException( "Stream ${k} must specify an output topic")
        }
      }
    }
    log.info "config.split  ${config.split as List}"
    log.info "Topology config is valid"
    return config
  }

  static Properties streamsConfig(Map kafkaConfig) {
    log.debug "Building kafka stream config: $kafkaConfig"
    Properties streamsConfiguration = new Properties()
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaConfig.application.id)
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrap.servers)
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().class.name)
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().class.name)
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500)
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    return streamsConfiguration
  }
}
