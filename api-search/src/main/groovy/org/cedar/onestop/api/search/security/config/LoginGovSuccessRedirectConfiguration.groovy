package org.cedar.onestop.api.search.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("logingov.success-redirect")
class LoginGovSuccessRedirectConfiguration {
    String successRedirect
}