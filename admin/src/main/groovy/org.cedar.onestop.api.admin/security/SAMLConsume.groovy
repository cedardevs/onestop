package org.cedar.onestop.api.admin.security

import net.shibboleth.utilities.java.support.component.ComponentInitializationException
import net.shibboleth.utilities.java.support.xml.ParserPool
import org.opensaml.core.xml.XMLObject
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.core.xml.io.UnmarshallingException
import org.opensaml.core.xml.schema.XSString
import org.opensaml.core.xml.schema.impl.XSAnyImpl
import org.opensaml.core.xml.util.XMLObjectSupport
import org.opensaml.messaging.context.MessageContext
import org.opensaml.messaging.handler.MessageHandler
import org.opensaml.messaging.handler.MessageHandlerException
import org.opensaml.messaging.handler.impl.BasicMessageHandlerChain
import org.opensaml.saml.common.binding.security.impl.MessageLifetimeSecurityHandler
import org.opensaml.saml.common.binding.security.impl.ReceivedEndpointSecurityHandler
import org.opensaml.saml.common.messaging.context.SAMLMessageInfoContext
import org.opensaml.saml.saml2.core.ArtifactResponse
import org.opensaml.saml.saml2.core.Assertion
import org.opensaml.saml.saml2.core.Attribute
import org.opensaml.saml.saml2.core.AttributeStatement
import org.opensaml.saml.saml2.core.EncryptedAssertion
import org.opensaml.saml.saml2.core.Response
import org.opensaml.saml.saml2.encryption.Decrypter
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver
import org.opensaml.security.credential.Credential
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver
import org.opensaml.xmlsec.encryption.support.DecryptionException
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver
import org.opensaml.xmlsec.encryption.support.SimpleRetrievalMethodEncryptedKeyResolver
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException

import javax.servlet.http.HttpServletRequest
import javax.xml.bind.DatatypeConverter
import java.nio.charset.StandardCharsets

class SAMLConsume {

    private static Logger logger = LoggerFactory.getLogger(SAMLConsume.class)

    static String getEmail(Assertion assertion, IdentityProvider identityProvider) {
        return getAttributeValue(identityProvider.emailAssertionName, identityProvider.emailAssertionNameFormat, assertion)
    }

    static String getUUID(Assertion assertion, IdentityProvider identityProvider) {
        return getAttributeValue(identityProvider.uuidAssertionName, identityProvider.uuidAssertionNameFormat, assertion)
    }

    private static String getAttributeValue(String attributeName, String attributeNameFormat, Assertion assertion)
    {
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements())
        {
            for (Attribute attribute : attributeStatement.getAttributes())
            {
                if (attributeName == attribute.getName() && attributeNameFormat == attribute.getNameFormat())
                {
                    List<XMLObject> attributeValues = attribute.getAttributeValues()
                    if (!attributeValues.isEmpty())
                    {
                        return getAttributeValue(attributeValues.get(0))
                    }
                }
            }
        }
        throw new IllegalArgumentException("no username attribute found")
    }

    private static String getAttributeValue(XMLObject attributeValue)
    {
        return attributeValue == null ?
                null :
                attributeValue instanceof XSString ?
                        getStringAttributeValue((XSString) attributeValue) :
                        attributeValue instanceof XSAnyImpl ?
                                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                                attributeValue.toString()
    }

    private static String getStringAttributeValue(XSString attributeValue)
    {
        return attributeValue.getValue()
    }

    private static String getAnyAttributeValue(XSAnyImpl attributeValue)
    {
        return attributeValue.getTextContent()
    }


    static Map<String, String> getAssertions(String samlResponseEncoded, final HttpServletRequest request, final long clockSkew, final long messageLifetime) {

        Map<String, String> assertions = [email: null, uuid: null]

        String samlResponseXML = decode(samlResponseEncoded)
        logger.debug("SAML Response XML:\n${samlResponseXML}\n")
        Response samlResponse = getSAMLResponse(samlResponseXML)

        validateDestinationAndLifeTime(samlResponse, request, clockSkew, messageLifetime)

        List<Assertion> assertionList = getSAMLAssertions(samlResponse)
        String issuer
        String email
        String uuid
        for (Assertion assertion : assertionList) {
            issuer = assertion.getIssuer().getValue()
            IdentityProvider idpOfAssertion = IdentityProviderEnumeration.findByIssuerIDP(issuer).getValue()
            email = getEmail(assertion, idpOfAssertion)
            uuid = getUUID(assertion, idpOfAssertion)
        }

        assertions.email = email
        assertions.uuid = uuid

        return assertions
    }

    private static String decode(String samlResponseEncoded) {
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(samlResponseEncoded)
        return new String(decodedBytes, StandardCharsets.UTF_8)
    }

    private static Response getSAMLResponse(String samlResponseXML) throws IOException, UnmarshallingException, SAXException {
        ParserPool parserPool = XMLObjectProviderRegistrySupport.getParserPool()
        InputStream inputStream = new ByteArrayInputStream(samlResponseXML.getBytes(StandardCharsets.UTF_8))
        Response response = (Response) XMLObjectSupport.unmarshallFromInputStream(parserPool, inputStream)
        return response
    }

    private static Decrypter buildAssertionDecrypter() {
        final List<EncryptedKeyResolver> list = new ArrayList<>()
        list.add(new InlineEncryptedKeyResolver())
        list.add(new EncryptedElementTypeEncryptedKeyResolver())
        list.add(new SimpleRetrievalMethodEncryptedKeyResolver())
        logger.debug("Build a list of encrypted key resolvers: [{}]", list)
        final ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver(list)
        logger.debug("Building credential instance to decrypted data")
        final Credential encryptionCredential = CredentialUtil.credential
        final KeyInfoCredentialResolver resolver = new StaticKeyInfoCredentialResolver(encryptionCredential)
        final Decrypter decrypter = new Decrypter(null, resolver, encryptedKeyResolver)
        decrypter.setRootInNewDocument(true)
        return decrypter
    }

    private static List<Assertion> getSAMLAssertions(Response samlResponse) {

        // get list of unencrypted assertions to start
        List<Assertion> assertionList = samlResponse.getAssertions()

        // decrypt encrypted assertions and add to our assertion list
        if (samlResponse.getEncryptedAssertions().size() > 0) {

            Decrypter decrypter = buildAssertionDecrypter()

            assertionList = new ArrayList<Assertion>(samlResponse.getAssertions().size() + samlResponse.getEncryptedAssertions().size())
            assertionList.addAll(samlResponse.getAssertions())
            List<EncryptedAssertion> encryptedAssertionList = samlResponse.getEncryptedAssertions()
            for (EncryptedAssertion encryptedAssertion : encryptedAssertionList) {
                try {
                    Assertion decryptedAssertion = decrypter.decrypt(encryptedAssertion)
                    assertionList.add(decryptedAssertion)
                }
                catch (DecryptionException e) {
                    logger.debug("${e.message} (assertion will be skipped)")
                }
            }
        }

        return assertionList
    }

    private static void validateDestinationAndLifeTime(Response samlResponse, HttpServletRequest request, long clockSkew, long messageLifetime) {

        MessageContext context = new MessageContext<Response>()
        context.setMessage(samlResponse)

        SAMLMessageInfoContext messageInfoContext = context.getSubcontext(SAMLMessageInfoContext.class, true)
        messageInfoContext.setMessageIssueInstant(samlResponse.getIssueInstant())

        MessageLifetimeSecurityHandler lifetimeSecurityHandler = new MessageLifetimeSecurityHandler()
        lifetimeSecurityHandler.setClockSkew(clockSkew)
        lifetimeSecurityHandler.setMessageLifetime(messageLifetime)
        lifetimeSecurityHandler.setRequiredRule(true)

        ReceivedEndpointSecurityHandler receivedEndpointSecurityHandler = new ReceivedEndpointSecurityHandler()
        receivedEndpointSecurityHandler.setHttpServletRequest(request)
        List handlers = new ArrayList<MessageHandler>()
        handlers.add(lifetimeSecurityHandler)
        handlers.add(receivedEndpointSecurityHandler)

        BasicMessageHandlerChain<ArtifactResponse> handlerChain = new BasicMessageHandlerChain<ArtifactResponse>()
        handlerChain.setHandlers(handlers)

        try {
            handlerChain.initialize()
            handlerChain.doInvoke(context)
        } catch (ComponentInitializationException e) {
            throw new RuntimeException(e)
        } catch (MessageHandlerException e) {
            throw new RuntimeException(e)
        }
    }

}
