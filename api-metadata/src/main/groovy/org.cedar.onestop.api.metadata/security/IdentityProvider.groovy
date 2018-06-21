package org.cedar.onestop.api.metadata.security

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration

class IdentityProvider {
    String name
    String loginEndpoint
    String loginBinding
    String logoutEndpoint
    String logoutBinding
    String issuerSP
    String issuerIDP
    List<String> authnContextRefs
    AuthnContextComparisonTypeEnumeration authnContextComparisonType
    String assertionConsumerServiceURL
    String assertionConsumerServiceLogoutURL
    String nameIDPolicyFormat
    Boolean forceAuthn
    Boolean isPassive
    String signatureAlgorithm
    String signatureCanonicalizationAlgorithm
    String digestAlgorithm
}
