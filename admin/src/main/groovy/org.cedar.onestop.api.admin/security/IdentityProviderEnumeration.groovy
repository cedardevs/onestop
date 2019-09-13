package org.cedar.onestop.api.admin.security

import org.opensaml.saml.common.xml.SAMLConstants
import org.opensaml.saml.saml2.core.AuthnContext
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration
import org.opensaml.saml.saml2.core.NameIDType
import org.opensaml.xmlsec.signature.support.SignatureConstants

enum IdentityProviderEnumeration {
    ICAM_NOAA_SCIAPPS(
            new IdentityProvider(
                    name: "ICAM (sciapps)",
                    loginEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/SSOPOST/metaAlias/noaa-online/noaa-online-idp",
                    loginBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    logoutEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/IDPSloRedirect/metaAlias/noaa-online/noaa-online-idp",
                    logoutBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    issuerSP: "https://sciapps.colorado.edu/onestop/api/",
                    issuerIDP: "noaa-online-idp",
                    authnContextRefs: [AuthnContext.UNSPECIFIED_AUTHN_CTX],
                    authnContextComparisonType: "minimum",
                    assertionConsumerServiceURL: "https://sciapps.colorado.edu/onestop/api/loginConsume",
                    assertionConsumerServiceLogoutURL: "https://sciapps.colorado.edu/onestop/api/logoutConsume",
                    nameIDPolicyFormat: NameIDType.UNSPECIFIED,
                    forceAuthn: null,
                    isPassive: null,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA1,
                    emailAssertionName: "mail",
                    emailAssertionNameFormat: null,
                    uuidAssertionName: "uid",
                    uuidAssertionNameFormat: null
            )
    ),
    ICAM_NOAA_LOCAL(
            new IdentityProvider(
                    name: "ICAM (localhost)",
                    loginEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/SSOPOST/metaAlias/noaa-online/noaa-online-idp",
                    loginBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    logoutEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/IDPSloRedirect/metaAlias/noaa-online/noaa-online-idp",
                    logoutBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    issuerSP: "https://localhost:8098/onestop/api/",
                    issuerIDP: "noaa-online-idp",
                    authnContextRefs: [AuthnContext.UNSPECIFIED_AUTHN_CTX],
                    authnContextComparisonType: "minimum",
                    assertionConsumerServiceURL: "https://localhost:8098/onestop/api/loginConsume",
                    assertionConsumerServiceLogoutURL: "https://localhost:8098/onestop/api/logoutConsume",
                    nameIDPolicyFormat: NameIDType.UNSPECIFIED,
                    forceAuthn: null,
                    isPassive: null,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA1,
                    emailAssertionName: "mail",
                    emailAssertionNameFormat: null,
                    uuidAssertionName: "uid",
                    uuidAssertionNameFormat: null
            )
    ),
    LOGIN_GOV_SCIAPPS(
            new IdentityProvider(
                    name: "login.gov (sciapps)",
                    loginEndpoint: "https://idp.int.identitysandbox.gov/api/saml/auth",
                    loginBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    logoutEndpoint: "https://idp.int.identitysandbox.gov/api/saml/logout",
                    logoutBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_sciapps",
                    issuerIDP: "https://idp.int.identitysandbox.gov/api/saml",
                    authnContextRefs: ["http://idmanagement.gov/ns/assurance/loa/1", "http://idmanagement.gov/ns/requested_attributes?ReqAttr=email"],
                    authnContextComparisonType: "minimum",
                    assertionConsumerServiceURL: "https://sciapps.colorado.edu/auth/saml/consume",
                    assertionConsumerServiceLogoutURL: "https://sciapps.colorado.edu/auth/saml/consumeLogout",
                    nameIDPolicyFormat: NameIDType.PERSISTENT,
                    forceAuthn: false,
                    isPassive: true,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA256,
                    emailAssertionName: "email",
                    emailAssertionNameFormat: "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
                    uuidAssertionName: "uuid",
                    uuidAssertionNameFormat: "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
            )
    ),
    LOGIN_GOV_LOCAL(
            new IdentityProvider(
                    name: "login.gov (localhost)",
                    loginEndpoint: "https://idp.int.identitysandbox.gov/api/saml/auth",
                    loginBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    logoutEndpoint: "https://idp.int.identitysandbox.gov/api/saml/logout",
                    logoutBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_localhost",
                    issuerIDP: "https://idp.int.identitysandbox.gov/api/saml",
                    authnContextRefs: ["http://idmanagement.gov/ns/assurance/loa/1", "http://idmanagement.gov/ns/requested_attributes?ReqAttr=email"],
                    authnContextComparisonType: "minimum",
                    assertionConsumerServiceURL: "https://localhost:8098/onestop/api/loginConsume",
                    assertionConsumerServiceLogoutURL: "https://localhost:8098/onestop/api/logoutConsume",
                    nameIDPolicyFormat: NameIDType.PERSISTENT,
                    forceAuthn: false,
                    isPassive: true,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA256,
                    emailAssertionName: "email",
                    emailAssertionNameFormat: "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress",
                    uuidAssertionName: "uuid",
                    uuidAssertionNameFormat: "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"
            )
    )

    private final IdentityProvider identityProvider

    private IdentityProviderEnumeration(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider
    }

    static IdentityProviderEnumeration findByName(String name){
        for(IdentityProviderEnumeration idp : values()){
            if( idp.identityProvider.getName() == name){
                return idp
            }
        }
        return null
    }

    static IdentityProviderEnumeration findByIssuerIDP(String issuerIDP){
        for(IdentityProviderEnumeration idp : values()){
            if( idp.identityProvider.getIssuerIDP() == issuerIDP){
                return idp
            }
        }
        return null
    }

    String toString() {
        return identityProvider.getName()
    }

    IdentityProvider getValue() {
        return identityProvider
    }

    static List<String> getNames() {
        return values().collect {
            return it.getValue().getName()
        }
    }
}