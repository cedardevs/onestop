package org.cedar.onestop.api.metadata.security

import org.opensaml.saml.common.xml.SAMLConstants
import org.opensaml.saml.saml2.core.AuthnContext
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration
import org.opensaml.saml.saml2.core.NameIDType
import org.opensaml.xmlsec.signature.support.SignatureConstants

enum IdentityProviderEnumeration {
    ICAM_NOAA_SCIAPPS(
            new IdentityProvider(
                    name: "ICAM (sciapps)",
                    loginEndpoint: "https://sso-dev.lb.csp.noaa.gov:8443/openam/SSOPOST/metaAlias/noaa-online/noaa-online-idp",
                    loginBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    logoutEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/IDPSloRedirect/metaAlias/noaa-online/noaa-online-idp",
                    logoutBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_sciapps",
                    issuerIDP: "noaa-online-idp",
                    authnContextRefs: [AuthnContext.PPT_AUTHN_CTX],
                    authnContextComparisonType: AuthnContextComparisonTypeEnumeration.MINIMUM,
                    assertionConsumerServiceURL: "https://sciapps.colorado.edu/auth/saml/consume",
                    assertionConsumerServiceLogoutURL: "https://sciapps.colorado.edu/auth/saml/consumeLogout",
                    nameIDPolicyFormat: NameIDType.EMAIL,
                    forceAuthn: null,
                    isPassive: null,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA1
            )
    ),
    ICAM_NOAA_LOCAL(
            new IdentityProvider(
                    name: "ICAM (localhost)",
                    loginEndpoint: "https://sso-dev.lb.csp.noaa.gov:8443/openam/SSORedirect/metaAlias/noaa-online/noaa-online-idp",
                    loginBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    logoutEndpoint: "https://sso-dev.lb.csp.noaa.gov:443/openam/IDPSloRedirect/metaAlias/noaa-online/noaa-online-idp",
                    logoutBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_localhost",
                    issuerIDP: "noaa-online-idp",
                    authnContextRefs: [AuthnContext.PPT_AUTHN_CTX],
                    authnContextComparisonType: AuthnContextComparisonTypeEnumeration.MINIMUM,
                    assertionConsumerServiceURL: "https://localhost:11235/auth/saml/consume",
                    assertionConsumerServiceLogoutURL: "https://localhost:11235/auth/saml/consumeLogout",
                    nameIDPolicyFormat: NameIDType.EMAIL,
                    forceAuthn: null,
                    isPassive: null,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA1
            )
    ),
    LOGIN_GOV_SCIAPPS(
            new IdentityProvider(
                    name: "login.gov (sciapps)",
                    loginEndpoint: "https://idp.int.identitysandbox.gov/api/saml/auth",
                    loginBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    logoutEndpoint: "https://idp.int.identitysandbox.gov/api/saml/logout",
                    logoutBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_sciapps",
                    issuerIDP: "https://idp.int.identitysandbox.gov/api/saml",
                    authnContextRefs: ["http://idmanagement.gov/ns/assurance/loa/1", "http://idmanagement.gov/ns/requested_attributes?ReqAttr=email"],
                    authnContextComparisonType: AuthnContextComparisonTypeEnumeration.EXACT,
                    assertionConsumerServiceURL: "https://sciapps.colorado.edu/auth/saml/consume",
                    assertionConsumerServiceLogoutURL: "https://sciapps.colorado.edu/auth/saml/consumeLogout",
                    nameIDPolicyFormat: NameIDType.PERSISTENT,
                    forceAuthn: false,
                    isPassive: true,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA256
            )
    ),
    LOGIN_GOV_LOCAL(
            new IdentityProvider(
                    name: "login.gov (localhost)",
                    loginEndpoint: "https://idp.int.identitysandbox.gov/api/saml/auth",
                    loginBinding: SAMLConstants.SAML2_POST_BINDING_URI,
                    logoutEndpoint: "https://idp.int.identitysandbox.gov/api/saml/logout",
                    logoutBinding: SAMLConstants.SAML2_REDIRECT_BINDING_URI,
                    issuerSP: "urn:gov:gsa:SAML:2.0.profiles:sp:sso:NOAA:authentication_localhost",
                    issuerIDP: "https://idp.int.identitysandbox.gov/api/saml",
                    authnContextRefs: ["http://idmanagement.gov/ns/assurance/loa/1", "http://idmanagement.gov/ns/requested_attributes?ReqAttr=email"],
                    authnContextComparisonType: AuthnContextComparisonTypeEnumeration.EXACT,
                    assertionConsumerServiceURL: "https://localhost:11235/auth/saml/consume",
                    assertionConsumerServiceLogoutURL: "https://localhost:11235/auth/saml/consumeLogout",
                    nameIDPolicyFormat: NameIDType.PERSISTENT,
                    forceAuthn: false,
                    isPassive: true,
                    signatureAlgorithm: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256,
                    signatureCanonicalizationAlgorithm: SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                    digestAlgorithm: SignatureConstants.ALGO_ID_DIGEST_SHA256
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