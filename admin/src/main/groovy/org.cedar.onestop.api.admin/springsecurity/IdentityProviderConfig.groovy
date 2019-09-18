package org.cedar.onestop.api.admin.springsecurity


import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("icam")
@Configuration
@ConfigurationProperties(prefix = "identityprovider")
class IdentityProviderConfig {
    String name
    String loginEndpoint
    String loginBinding
    String logoutEndpoint
    String logoutBinding
    String issuerSP
    String issuerIDP
    List<String> authnContextRefs = new ArrayList<>()
    String authnContextComparisonType
    String assertionConsumerServiceURL
    String assertionConsumerServiceLogoutURL
    String nameIDPolicyFormat
    Boolean forceAuthn
    Boolean isPassive
    String signatureAlgorithm
    String signatureCanonicalizationAlgorithm
    String digestAlgorithm
    String emailAssertionName
    String emailAssertionNameFormat
    String uuidAssertionName
    String uuidAssertionNameFormat
}