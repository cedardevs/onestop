package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.security.SAMLFilter
import org.cedar.onestop.api.metadata.security.SPCredentialsParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class SecurityFilter extends GenericFilterBean {

    @Autowired
    ServerProperties serverProperties

    private SAMLFilter samlFilter

    @Override
    protected void initFilterBean() throws ServletException {

        // create instance of SAMLFilter to be wrapped by this Spring aware filter bean
        samlFilter = new SAMLFilter()

        // set keystore values to be used in custom SAML filter via the SecurityProperties known by Spring config
        SPCredentialsParam.setKeyStorePath(serverProperties.ssl.keyStore)
        SPCredentialsParam.setKeyStorePassword(serverProperties.ssl.keyStorePassword)
        SPCredentialsParam.setAlias(serverProperties.ssl.keyAlias)
        SPCredentialsParam.setKeyPassword(serverProperties.ssl.keyPassword)

        // initialize SAML filter
        samlFilter.init()
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // delegate filter to our SAML filter
        samlFilter.doFilter(request, response, chain)
    }
}
