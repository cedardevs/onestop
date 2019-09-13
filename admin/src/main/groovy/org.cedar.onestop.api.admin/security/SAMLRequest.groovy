package org.cedar.onestop.api.admin.security

import org.joda.time.DateTime
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.core.xml.io.MarshallingException
import org.opensaml.saml.common.SAMLVersion
import org.opensaml.saml.saml2.core.AuthnContextClassRef
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration
import org.opensaml.saml.saml2.core.AuthnRequest
import org.opensaml.saml.saml2.core.Issuer
import org.opensaml.saml.saml2.core.LogoutRequest
import org.opensaml.saml.saml2.core.NameIDPolicy
import org.opensaml.saml.saml2.core.NameIDType
import org.opensaml.saml.saml2.core.RequestedAuthnContext
import org.opensaml.saml.saml2.core.impl.LogoutRequestBuilder
import org.opensaml.security.credential.Credential
import org.opensaml.xmlsec.signature.KeyInfo
import org.opensaml.xmlsec.signature.Signature
import org.opensaml.xmlsec.signature.X509Certificate
import org.opensaml.xmlsec.signature.X509Data
import org.opensaml.xmlsec.signature.support.SignatureConstants
import org.opensaml.xmlsec.signature.support.SignatureException
import org.opensaml.xmlsec.signature.support.Signer

class SAMLRequest {

    static AuthnRequest buildAuthnRequest(IdentityProvider identityProvider, Credential credential) {
        AuthnRequest authnRequest = SAMLUtil.buildSAMLObject(AuthnRequest.class)
        authnRequest.setIssueInstant(new DateTime())
        authnRequest.setVersion(SAMLVersion.VERSION_20)
        authnRequest.setDestination(identityProvider.getLoginEndpoint())
        authnRequest.setProtocolBinding(identityProvider.getLoginBinding())
        authnRequest.setAssertionConsumerServiceURL(identityProvider.assertionConsumerServiceURL)
        authnRequest.setID(SAMLUtil.generateSecureRandomId())
        authnRequest.setNameIDPolicy(buildNameIdPolicy(identityProvider))
        authnRequest.setRequestedAuthnContext(buildRequestedAuthnContext(identityProvider))
        authnRequest.setIssuer(buildIssuer(identityProvider))

        if(identityProvider.getForceAuthn() != null) {
            // should IDP force user to re-auth?
            authnRequest.setForceAuthn(identityProvider.getForceAuthn())
        }

        if(identityProvider.getIsPassive() != null) {
            // should IDP refrain rom interacting w/user during auth
            authnRequest.setIsPassive(identityProvider.getIsPassive())
        }

        // sign login request
        Signature signature = buildSignature(identityProvider, credential)

        authnRequest.setSignature(signature)

        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest)
        }
        catch (MarshallingException e) {
            e.printStackTrace()
        }

        try {
            Signer.signObject(signature)
        }
        catch (SignatureException e) {
            e.printStackTrace()
        }

        return authnRequest
    }


    static LogoutRequest buildLogoutRequest(IdentityProvider identityProvider, String keyStoreConfig) {

        LogoutRequestBuilder logout_request_builder = new LogoutRequestBuilder()
        LogoutRequest logoutRequest = logout_request_builder.buildObject(LogoutRequest.DEFAULT_ELEMENT_NAME)
        logoutRequest.setID(identityProvider.getName() + "_" + UUID.randomUUID().toString())
        logoutRequest.setDestination(identityProvider.getLogoutEndpoint())
        logoutRequest.setIssueInstant(new DateTime())
        logoutRequest.setVersion(SAMLVersion.VERSION_20)
        logoutRequest.setIssuer(buildIssuer(identityProvider))
        logoutRequest.setNameID(buildNameID(identityProvider))

        // sign logout request
//        Signature signature = buildSignature(keyStoreConfig)
//
//        logoutRequest.setSignature(signature)
//        LogoutRequestMarshaller marshaller = new LogoutRequestMarshaller()
//        marshaller.marshall(logoutRequest)
//        Signer.signObject(signature)

        return logoutRequest
    }

    private static Signature buildSignature(IdentityProvider identityProvider, Credential credential) {

        Signature signature = SAMLUtil.buildSAMLObject(Signature.class)
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS)
        signature.setSignatureAlgorithm(identityProvider.getSignatureAlgorithm())
        signature.setSigningCredential(credential)
        signature.setKeyInfo(buildKeyInfo())

        return signature
    }

    private static KeyInfo buildKeyInfo() {
        KeyInfo keyInfo = SAMLUtil.buildSAMLObject(KeyInfo.class)
        X509Data data = (X509Data) SAMLUtil.buildSAMLObject(X509Data.class)
        X509Certificate cert = (X509Certificate) SAMLUtil.buildSAMLObject(X509Certificate.class)
        // x509 certificate was saved as string when credential was built earlier
        // extracting the cert directly from the credential never seems to get same exact value...
        cert.setValue(CredentialUtil.x509Certificate)
        data.getX509Certificates().add(cert)
        keyInfo.getX509Datas().add(data)
        return keyInfo
    }

    private static NameIDPolicy buildNameIdPolicy(IdentityProvider identityProvider) {
        NameIDPolicy nameIDPolicy = SAMLUtil.buildSAMLObject(NameIDPolicy.class)
        nameIDPolicy.setAllowCreate(true) // allow IDP, in fulfillment, to create new id to represent principal... TODO: should we?
        nameIDPolicy.setFormat(identityProvider.getNameIDPolicyFormat())
        return nameIDPolicy
    }

    private static RequestedAuthnContext buildRequestedAuthnContext(IdentityProvider identityProvider) {
        RequestedAuthnContext requestedAuthnContext = SAMLUtil.buildSAMLObject(RequestedAuthnContext.class)
        requestedAuthnContext.setComparison(new AuthnContextComparisonTypeEnumeration(identityProvider.authnContextComparisonType))
        identityProvider.getAuthnContextRefs().each {
            AuthnContextClassRef authnContextClassRef = SAMLUtil.buildSAMLObject(AuthnContextClassRef.class)
            authnContextClassRef.setAuthnContextClassRef(it)
            requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef)
        }
        return requestedAuthnContext
    }

    private static Issuer buildIssuer(IdentityProvider identityProvider) {
        Issuer issuer = SAMLUtil.buildSAMLObject(Issuer.class)
        issuer.setValue(identityProvider.getIssuerSP())
        issuer.setFormat(NameIDType.ENTITY)
        return issuer
    }

}
