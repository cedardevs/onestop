package org.cedar.onestop.api.admin.security

import net.shibboleth.utilities.java.support.component.ComponentInitializationException
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.opensaml.core.config.InitializationException
import org.opensaml.core.config.InitializationService
import org.opensaml.messaging.context.MessageContext
import org.opensaml.messaging.encoder.MessageEncodingException
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext
import org.opensaml.saml.common.xml.SAMLConstants
import org.opensaml.saml.saml2.binding.encoding.impl.BaseSAML2MessageEncoder
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPArtifactEncoder
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostSimpleSignEncoder
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder
import org.opensaml.saml.saml2.core.*
import org.opensaml.security.credential.Credential
import org.opensaml.xmlsec.SignatureSigningParameters
import org.opensaml.xmlsec.config.impl.JavaCryptoValidationInitializer
import org.opensaml.xmlsec.context.SecurityParametersContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Provider
import java.security.Security

class SAMLFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(SAMLFilter.class)

    IdentityProvider identityProvider

    SAMLFilter(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {

        // validates the set of security providers configured in the JVM supports required cryptographic capabilities
        JavaCryptoValidationInitializer javaCryptoValidationInitializer = new JavaCryptoValidationInitializer()
        try {
            javaCryptoValidationInitializer.init()
        }
        catch(InitializationException e) {
            e.printStackTrace()
        }

        // log a human-readable description of the security providers and their services
        for (Provider jceProvider : Security.getProviders()) {
            logger.info(jceProvider.getInfo())
        }

        // service which initializes OpenSAML library modules using the Java Services API
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

        // cast to provide request information for HTTP servlets
        HttpServletRequest httpServletRequest = (HttpServletRequest)request

        // cast to provide HTTP-specific functionality in sending a response.
        // for example, include methods to access HTTP headers and cookies
        HttpServletResponse httpServletResponse = (HttpServletResponse)response

        if (httpServletRequest.getSession().getAttribute(SPConstants.AUTHENTICATED_SESSION_ATTRIBUTE) != null) {
            // we've previously been authenticated, continue filter chain
            chain.doFilter(request, response)
        }
        else {
            // remember where we want to go and redirect to IDP authentication
            setGotoURLOnSession(httpServletRequest)
            redirectUserForAuthentication(httpServletResponse)
        }
    }

    private static void setGotoURLOnSession(HttpServletRequest request) {
        request.getSession().setAttribute(SPConstants.GOTO_URL_SESSION_ATTRIBUTE, request.getRequestURL().toString())
    }

    private void redirectUserForAuthentication(HttpServletResponse httpServletResponse) {
        Credential credential = CredentialUtil.buildCredential()
        AuthnRequest authnRequest = SAMLRequest.buildAuthnRequest(identityProvider, credential)

        // uncomment this line to see AuthnRequest XML in output
        // SAMLUtil.logSAMLObject(authnRequest)

        redirectUserWithRequest(httpServletResponse, authnRequest, credential)
    }

    private static VelocityEngine buildVelocityEngine() {
        VelocityEngine velocityEngine = new VelocityEngine()
        velocityEngine.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8")
        velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8")
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName())
        velocityEngine.init()
        return velocityEngine
    }

    private void redirectUserWithRequest(HttpServletResponse httpServletResponse, AuthnRequest authnRequest, Credential credential) {

        MessageContext context = new MessageContext()
        context.setMessage(authnRequest)
        SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true)
        SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true)
        endpointContext.setEndpoint(identityProvider.buildLoginEndpoint())

        SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters()
        signatureSigningParameters.setSigningCredential(credential)
        signatureSigningParameters.setSignatureAlgorithm(identityProvider.getSignatureAlgorithm())
        signatureSigningParameters.setSignatureCanonicalizationAlgorithm(identityProvider.getSignatureCanonicalizationAlgorithm())
        signatureSigningParameters.setSignatureReferenceDigestMethod(identityProvider.getDigestAlgorithm())

        context.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters)

        // depending on the login binding, use different kind of encoder
        BaseSAML2MessageEncoder encoder

        // post login binding
        if(identityProvider.loginBinding == SAMLConstants.SAML2_POST_BINDING_URI) {
            logger.info("using POST login binding")
            VelocityEngine velocityEngine = buildVelocityEngine()
            encoder = new HTTPPostEncoder()
            encoder.setVelocityTemplateId("/templates/saml2-post-binding.vm")
            encoder.setVelocityEngine(velocityEngine)
        }
        // artifact login binding
        else if(identityProvider.loginBinding == SAMLConstants.SAML2_ARTIFACT_BINDING_URI) {
            logger.info("using ARTIFACT login binding")
            VelocityEngine velocityEngine = buildVelocityEngine()
            encoder = new HTTPArtifactEncoder()
            encoder.setVelocityTemplateId("/templates/saml2-post-artifact-binding.vm")
            encoder.setVelocityEngine(velocityEngine)
        }
        // simple sign login binding
        else if(identityProvider.loginBinding == SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI) {
            logger.info("using SIMPLE SIGN login binding")
            VelocityEngine velocityEngine = buildVelocityEngine()
            encoder = new HTTPPostSimpleSignEncoder()
            encoder.setVelocityTemplateId("/templates/saml2-post-simplesign-binding.vm")
            encoder.setVelocityEngine(velocityEngine)
        }
        // otherwise, assume we are using a redirect login binding
        else {
            logger.info("using REDIRECT login binding")
            encoder = new HTTPRedirectDeflateEncoder()
        }

        encoder.setMessageContext(context)
        encoder.setHttpServletResponse(httpServletResponse)

        try {
            encoder.initialize()

        }
        catch (ComponentInitializationException e) {
            throw new RuntimeException(e)
        }

        logger.info("Redirecting to IDP")

        try {
            encoder.encode()
            logger.debug("encoder header names: " + encoder.getHttpServletResponse().getHeaderNames().toString())
        }
        catch (MessageEncodingException e) {
            throw new RuntimeException(e)
        }
    }

    @Override
    void destroy() {
        // do nothing
    }
}
