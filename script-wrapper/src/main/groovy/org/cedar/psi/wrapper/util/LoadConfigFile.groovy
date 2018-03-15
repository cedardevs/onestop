package org.cedar.psi.wrapper.util

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

@Slf4j
class LoadConfigFile {
    def static replaceEnv = {
        if (!it) return it
        it.replaceAll(
                /\$\{([^\s\}]+)\}/) {
            System.getenv(it[1]) ?: it[0]
        }
    }

    static public getConfig = { name ->
        Map<String, Object> conf = [:]
        Yaml yaml = new Yaml()

        def envConf = System.getenv('PSI_CONFIG_LOCATION')
        envConf = envConf ?: null
        if (envConf && envConf?.toList()[0] != File.separator)
            envConf = System.getProperty("user.dir") + File.separator + envConf
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
}