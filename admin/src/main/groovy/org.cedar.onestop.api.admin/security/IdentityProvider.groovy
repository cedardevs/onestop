package org.cedar.onestop.api.admin.security

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

    String info() {
        return "\nIdentityProvider {" +
                "\n\tname: ${name}" +
                "\n\tloginEndpoint: ${loginEndpoint}" +
                "\n\tloginBinding: ${loginBinding}" +
                "\n\tlogoutEndpoint: ${logoutEndpoint}" +
                "\n\tlogoutBinding: ${logoutBinding}" +
                "\n\tissuerSP: ${issuerSP}" +
                "\n\tissuerIDP: ${issuerIDP}" +
                "\n\tauthnContextRefs: ${authnContextRefs}" +
                "\n\tauthnContextComparisonType: ${authnContextComparisonType}" +
                "\n\tassertionConsumerServiceURL: ${assertionConsumerServiceURL}" +
                "\n\tassertionConsumerServiceLogoutURL: ${assertionConsumerServiceLogoutURL}" +
                "\n\tnameIDPolicyFormat: ${nameIDPolicyFormat}" +
                "\n\tforceAuthn: ${forceAuthn}" +
                "\n\tisPassive: ${isPassive}" +
                "\n\tsignatureAlgorithm: ${signatureAlgorithm}" +
                "\n\tsignatureCanonicalizationAlgorithm: ${signatureCanonicalizationAlgorithm}" +
                "\n\tdigestAlgorithm: ${digestAlgorithm}" +
                "\n\temailAssertionName: ${emailAssertionName}" +
                "\n\temailAssertionNameFormat: ${emailAssertionNameFormat}" +
                "\n\tuuidAssertionName: ${uuidAssertionName}" +
                "\n\tuuidAssertionNameFormat: ${uuidAssertionNameFormat}" +
                "\n}\n"
    }

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
