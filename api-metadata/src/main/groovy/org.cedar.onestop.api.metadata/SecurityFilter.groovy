package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.security.CredentialUtil
import org.cedar.onestop.api.metadata.security.IdentityProviderEnumeration
import org.cedar.onestop.api.metadata.security.SAMLFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class SecurityFilter extends GenericFilterBean {

    @Autowired
    ServerProperties serverProperties

    // create instance of SAMLFilter to be wrapped by this Spring aware filter bean
    // provide an identity provider for which this filter will authenticate against
    private SAMLFilter samlFilter = new SAMLFilter(IdentityProviderEnumeration.LOGIN_GOV_LOCAL.getValue())

    @Override
    protected void initFilterBean() throws ServletException {

        // set keystore values to be used in custom SAML filter via the SecurityProperties known by Spring config
        CredentialUtil.setKeyStorePath(serverProperties.ssl.keyStore)
        CredentialUtil.setKeyStorePassword(serverProperties.ssl.keyStorePassword)
        CredentialUtil.setAlias(serverProperties.ssl.keyAlias)
        CredentialUtil.setKeyPassword(serverProperties.ssl.keyPassword)

        // initialize custom SAML filter
        samlFilter.init()
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // delegate filter to our custom SAML filter
        samlFilter.doFilter(request, response, chain)
    }
}
