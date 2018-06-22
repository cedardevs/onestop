package org.cedar.onestop.api.metadata.security

import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration
import org.opensaml.saml.saml2.metadata.Endpoint
import org.opensaml.saml.saml2.metadata.SingleLogoutService
import org.opensaml.saml.saml2.metadata.SingleSignOnService

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


    Endpoint buildLoginEndpoint() {
        SingleSignOnService endpoint = SAMLUtil.buildSAMLObject(SingleSignOnService.class)
        endpoint.setBinding(loginBinding)
        endpoint.setLocation(loginEndpoint)
        return endpoint
    }

    Endpoint buildLogoutEndpoint() {
        SingleLogoutService endpoint = SAMLUtil.buildSAMLObject(SingleLogoutService.class)
        endpoint.setBinding(logoutBinding)
        endpoint.setLocation(logoutEndpoint)
        return endpoint
    }

}
