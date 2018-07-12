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

        // ICAM is weird, and requires the AuthnRequest "Destination" attribute to have the 443 binding port;
        // however, endpoint for the HTTP request needs the 8443 for email instead of CAC login.
        // Setting both to 8443 will result in an "500 Invalid destination" error
        // TODO: make this condition easier by consolidating the enum or giving a better property to test off
//        if(name == "ICAM (localhost)" || name == "ICAM (sciapps)") {
//            endpoint.setLocation(loginEndpoint.replace(':443', ':8443'))
//        }
//        else {
            endpoint.setLocation(loginEndpoint)
//        }

        return endpoint
    }

    Endpoint buildLogoutEndpoint() {
        SingleLogoutService endpoint = SAMLUtil.buildSAMLObject(SingleLogoutService.class)
        endpoint.setBinding(logoutBinding)
        endpoint.setLocation(logoutEndpoint)
        return endpoint
    }

}
