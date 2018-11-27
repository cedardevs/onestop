package org.cedar.onestop.api.search.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties('logingov.keystore')
class LoginGovKeystoreConfiguration {
    String alias
    String file
    String password
    String type
}
