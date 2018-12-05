package org.cedar.onestop.api.search.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties('logingov')
class LoginGovConfiguration {

    static class Keystore {
        String alias
        String file
        String password
        String type
    }

    Keystore keystore
    String allowedOrigin
    String loginSuccessRedirect
    String logoutSuccessRedirect
}
