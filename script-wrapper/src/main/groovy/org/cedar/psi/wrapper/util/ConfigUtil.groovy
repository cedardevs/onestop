package org.cedar.psi.wrapper.util

import groovy.util.logging.Slf4j
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.yaml.snakeyaml.Yaml

@Slf4j
class ConfigUtil {
    def static replaceEnv = {
        if (!it) return it
        it.replaceAll(
                /\$\{([^\s\}]+)\}/) {
            System.getenv(it[1]) ?: it[0]
        }
    }

    static public getConfig = { name ->
        log.info "Retrieving configuration... "
        Map<String, Object> conf = [:]
        Yaml yaml = new Yaml()
        def envConf = System.getenv('PSI_CONFIG_LOCATION')
        Map<String, String> env = System.getenv()
        env.keySet().stream().filter({k -> k.startsWith('PSI')})
        envConf = envConf ?: null
        if (envConf && envConf?.toList()[0] != File.separator){
            envConf = System.getProperty("user.dir") + File.separator + envConf
        }
        envConf = envConf ? ("file://${envConf}") : null

        def propConf = System.properties['psi.config.location']
        propConf = (propConf)

        def defaultConf = "file://" + System.getProperty("user.dir") + "/${name}.yml"

        def url = new URL(envConf ?: propConf ?: defaultConf)
        log.debug "loading config url $url"
        conf = (Map<String, Object>) yaml.load(this.replaceEnv(url.openStream().text))
        log.debug "Loaded full conf: " + conf
        conf
    }

    static Map validateKafkaConfig(Map config){
        log.debug "Validating kafka config: $config"
        if (!config?.application?.id){ throw Exception('Cannot resolve ${kafka.application.id}')}
        if (!config?.bootstrap?.servers){ throw Exception('Cannot resolve ${kafka.bootstrap.servers}')}
        if (!config?.applicationId){ throw Exception('Cannot resolve ${kafka.applicationId}')}
        log.debug "Kafka config is valid"
        return config
    }

    static Map validateTopologyConfig(Map config){
        log.debug "Validating topology config: $config"
        if (!config?.topics?.input){ throw Exception('Cannot resolve ${stream.topics.input}')}
        if (!config?.topics?.output){ throw Exception('Cannot resolve ${stream.topics.output}')}
        if (!config?.command){ throw Exception('Cannot resolve ${stream.topics.output}')}
        if (!config?.command_timeout){ config.stream.command_timeout = 5000}
        if (!config?.convert?.iso){ config.stream.convert.iso = false}
        log.debug "Topology config is valid"
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