package org.cedar.onestop.api.admin.springsecurity

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("icam")
@Component
@ConfigurationProperties('icam')
class ICAMConfiguration {
    static class Keystore {
        String alias
        String file
        String keyPassword
        String password
        String type
    }
    Keystore keystore
}