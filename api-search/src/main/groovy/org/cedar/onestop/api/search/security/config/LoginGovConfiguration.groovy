package org.cedar.onestop.api.search.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("login-gov")
@Component
@ConfigurationProperties('login-gov')
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
    String loginFailureRedirect
    String logoutSuccessRedirect
}
