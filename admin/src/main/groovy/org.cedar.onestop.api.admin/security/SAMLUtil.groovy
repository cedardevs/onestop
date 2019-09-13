package org.cedar.onestop.api.admin.security

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy
import org.opensaml.core.xml.XMLObject
import org.opensaml.core.xml.XMLObjectBuilderFactory
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport
import org.opensaml.core.xml.io.Marshaller
import org.opensaml.core.xml.io.MarshallingException
import org.opensaml.saml.common.SignableSAMLObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Element

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.namespace.QName
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class SAMLUtil {
    private static Logger logger = LoggerFactory.getLogger(SAMLUtil.class)
    private static SecureRandomIdentifierGenerationStrategy secureRandomIdGenerator

    static {
        secureRandomIdGenerator = new SecureRandomIdentifierGenerationStrategy()
    }

    static <T> T buildSAMLObject(final Class<T> clazz) {
        T object = null;
        try {
            XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory()
            QName defaultElementName = (QName)clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null)
            object = (T)builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName)
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not create SAML object")
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object")
        }

        return object;
    }

    static String generateSecureRandomId() {
        return secureRandomIdGenerator.generateIdentifier()
    }

    static void logSAMLObject(final XMLObject object) {
        Element element = null

        if (object instanceof SignableSAMLObject && ((SignableSAMLObject)object).isSigned() && object.getDOM() != null) {
            element = object.getDOM()
        } else {
            try {
                Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(object)
                out.marshall(object)
                element = object.getDOM()

            } catch (MarshallingException e) {
                logger.error(e.getMessage(), e)
            }
        }

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            StreamResult result = new StreamResult(new StringWriter())
            DOMSource source = new DOMSource(element)

            transformer.transform(source, result)
            String xmlString = result.getWriter().toString()

            logger.info(xmlString);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace()
        } catch (TransformerException e) {
            e.printStackTrace()
        }
    }

    static void setAuthenticatedSession(HttpServletRequest req) {
        req.getSession().setAttribute(SPConstants.AUTHENTICATED_SESSION_ATTRIBUTE, true)
    }

    static void redirectToGotoURL(HttpServletRequest req, HttpServletResponse resp) {
        String gotoURL = (String)req.getSession().getAttribute(SPConstants.GOTO_URL_SESSION_ATTRIBUTE)
        logger.info("Redirecting to requested URL: " + gotoURL)
        try {
            resp.sendRedirect(gotoURL)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }
}
