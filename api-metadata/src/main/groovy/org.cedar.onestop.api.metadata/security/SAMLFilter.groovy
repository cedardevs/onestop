package org.cedar.onestop.api.metadata.security

import net.shibboleth.utilities.java.support.component.ComponentInitializationException

import org.joda.time.DateTime
import org.opensaml.core.config.InitializationException
import org.opensaml.core.config.InitializationService
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.core.xml.io.MarshallingException
import org.opensaml.messaging.context.MessageContext
import org.opensaml.messaging.encoder.MessageEncodingException
import org.opensaml.saml.common.SAMLVersion
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder
import org.opensaml.saml.saml2.core.*
import org.opensaml.saml.saml2.metadata.Endpoint
import org.opensaml.saml.saml2.metadata.SingleSignOnService
import org.opensaml.security.credential.Credential
import org.opensaml.security.x509.BasicX509Credential
import org.opensaml.xmlsec.SignatureSigningParameters
import org.opensaml.xmlsec.config.JavaCryptoValidationInitializer
import org.opensaml.xmlsec.context.SecurityParametersContext
import org.opensaml.xmlsec.signature.Signature
import org.opensaml.xmlsec.signature.support.SignatureConstants
import org.opensaml.xmlsec.signature.support.SignatureException
import org.opensaml.xmlsec.signature.support.Signer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Provider
import java.security.Security
import java.security.cert.X509Certificate

class SAMLFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(SAMLFilter.class)

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer()
        try {
            javaCryptoValidationInitializer.init()
        }
        catch(InitializationException e) {
            e.printStackTrace()
        }

        for (Provider jceProvider : Security.getProviders()) {
            logger.info(jceProvider.getInfo())
        }

        try {
            logger.info("Initializing")
            InitializationService.initialize()
        }
        catch(InitializationException e) {
            throw new RuntimeException("Initialization failed")
        }
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request
        HttpServletResponse httpServletResponse = (HttpServletResponse)response

        // TODO: get identity provider from session attribute and/or request param?
        IdentityProvider identityProvider = IdentityProviderEnumeration.ICAM_NOAA_LOCAL.getValue()

        if (httpServletRequest.getSession().getAttribute(SPConstants.AUTHENTICATED_SESSION_ATTRIBUTE) != null) {
            chain.doFilter(request, response)
        }
        else {
            setGotoURLOnSession(httpServletRequest)
            redirectUserForAuthentication(httpServletResponse, identityProvider)
        }
    }

    private static void setGotoURLOnSession(HttpServletRequest request) {
        request.getSession().setAttribute(SPConstants.GOTO_URL_SESSION_ATTRIBUTE, request.getRequestURL().toString())
    }

    private static void redirectUserForAuthentication(HttpServletResponse httpServletResponse, IdentityProvider identityProvider) {
        Credential credential = buildCredentialFromKeyStore()

        AuthnRequest authnRequest = buildAuthnRequest(identityProvider, credential)
        redirectUserWithRequest(httpServletResponse, authnRequest, identityProvider, credential)
    }

    private static void redirectUserWithRequest(HttpServletResponse httpServletResponse, AuthnRequest authnRequest, IdentityProvider identityProvider, Credential credential) {


        MessageContext context = new MessageContext()

        context.setMessage(authnRequest)

        SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true)

        SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true)
        endpointContext.setEndpoint(getIDPLoginEndpoint(identityProvider))

        SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters()
        signatureSigningParameters.setSigningCredential(credential)
        signatureSigningParameters.setSignatureAlgorithm(identityProvider.getSignatureAlgorithm())
        signatureSigningParameters.setSignatureCanonicalizationAlgorithm(identityProvider.getSignatureCanonicalizationAlgorithm())
        signatureSigningParameters.setSignatureReferenceDigestMethod(identityProvider.getDigestAlgorithm())

        context.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters)

        HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder()

//        VelocityEngine velocityEngine = new VelocityEngine()
//        velocityEngine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8")
//        velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8")
//        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
//        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName())
//        velocityEngine.init()

//        HTTPPostSimpleSignEncoder encoder = new HTTPPostSimpleSignEncoder()
//        HTTPPostEncoder encoder = new HTTPPostEncoder()
//        encoder.setVelocityTemplateId("/templates/saml2-post-binding.vm")
//        encoder.setVelocityTemplateId("/templates/saml2-post-simplesign-binding.vm")
//        encoder.setVelocityEngine(velocityEngine)

//        println("context: ${context.toString()}")
        encoder.setMessageContext(context)
        encoder.setHttpServletResponse(httpServletResponse)
//        println("encoder.getBindingURI: ${encoder.getBindingURI()}")
//        println("encoder.getProperties(): ${encoder.getProperties().toString()}")

        try {
            encoder.initialize()

        }
        catch (ComponentInitializationException e) {
            throw new RuntimeException(e)
        }

        logger.info("Redirecting to IDP")

        try {

            encoder.encode()

            println("encoder header names: " + encoder.getHttpServletResponse().getHeaderNames().toString())
        }
        catch (MessageEncodingException e) {
            throw new RuntimeException(e)
        }
    }

    static Credential buildCredentialFromKeyStore() {

        String keyStorePassword = SPCredentialsParam.getKeyStorePassword()
        String keyAlias = SPCredentialsParam.getAlias()

        KeyStore keyStore = buildKeyStore(SPCredentialsParam.getKeyStorePath(), keyStorePassword)

        KeyStore.PasswordProtection protectionParameter = new KeyStore.PasswordProtection(keyStorePassword.toCharArray())
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, protectionParameter)
        PrivateKey privateKey = privateKeyEntry.getPrivateKey()

        X509Certificate cert = (X509Certificate) privateKeyEntry.getCertificate()
        privateKeyEntry.getCertificate()

        BasicX509Credential credential = new BasicX509Credential(cert, privateKey)

//
//
//        BasicX509Credential
//        Credential credential = new BasicX509Credential()
//        credential.setEntityCertificate(cert)
//        credential.setPrivateKey(privateKey)






        return credential
    }

    static KeyStore buildKeyStore(String keyStoreConfig, String keyStorePassword) {
        Path keyStorePath = Paths.get(keyStoreConfig)
        KeyStore keyStore = KeyStore.getInstance("JKS")
        InputStream keyStoreStream = Files.newInputStream(keyStorePath)
        keyStore.load(keyStoreStream, keyStorePassword.toCharArray())
        return keyStore
    }

    private static AuthnRequest buildAuthnRequest(IdentityProvider identityProvider, Credential credential) {
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

    static Signature buildSignature(IdentityProvider identityProvider, Credential credential) {

        Signature signature = SAMLUtil.buildSAMLObject(Signature.class)
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS)
        signature.setSignatureAlgorithm(identityProvider.getSignatureAlgorithm())
        signature.setSigningCredential(credential)

        return signature
    }

    private static NameIDPolicy buildNameIdPolicy(IdentityProvider identityProvider) {
        NameIDPolicy nameIDPolicy = SAMLUtil.buildSAMLObject(NameIDPolicy.class)
        nameIDPolicy.setAllowCreate(true) // allow IDP, in fulfillment, to create new id to represent principal... TODO: should we?
        nameIDPolicy.setFormat(identityProvider.getNameIDPolicyFormat())
        return nameIDPolicy
    }

    private static RequestedAuthnContext buildRequestedAuthnContext(IdentityProvider identityProvider) {
        RequestedAuthnContext requestedAuthnContext = SAMLUtil.buildSAMLObject(RequestedAuthnContext.class)
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM)
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
    }

    private static Endpoint getIDPLoginEndpoint(IdentityProvider identityProvider) {
        SingleSignOnService endpoint = SAMLUtil.buildSAMLObject(SingleSignOnService.class)
        endpoint.setBinding(identityProvider.getLoginBinding())
        endpoint.setLocation(identityProvider.getLoginEndpoint())
        return endpoint
    }

//    private Endpoint getIDPLogoutEndpoint(IdentityProvider identityProvider) {
//        SingleLogoutService endpoint = SAMLUtil.buildSAMLObject(SingleLogoutService.class)
//        endpoint.setBinding(identityProvider.getLogoutBinding())
//        endpoint.setLocation(identityProvider.getLogoutEndpoint())
//        return endpoint
//    }

    @Override
    void destroy() {
//        super.destroy()
    }
}
