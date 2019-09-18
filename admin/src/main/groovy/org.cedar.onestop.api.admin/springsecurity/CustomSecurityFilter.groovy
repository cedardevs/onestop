package org.cedar.onestop.api.admin.springsecurity

import org.cedar.onestop.api.admin.authorization.service.UserDetailsServiceImpl
import org.cedar.onestop.api.admin.security.*
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.file.AccessDeniedException
import java.security.Principal

@Profile("icam")
class CustomSecurityFilter extends AbstractAuthenticationProcessingFilter {

    private final List<String> securedEndpointsRegex = [/\/metadata.*$/, /\/admin\/.+$/, /\/upload.html$/, /\/uploadResponse.html$/, /^.+\/userOnly$/]

    // SAMLFilter to be wrapped by this Spring aware filter bean
    private SAMLFilter samlFilter

    UserDetailsServiceImpl userDetailsServiceImpl

    CustomSecurityFilter(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsServiceImpl, ICAMConfiguration icamConfiguration, IdentityProviderConfig idpConfig) {
        super("/")
        this.setAuthenticationManager(authenticationManager)
        this.userDetailsServiceImpl = userDetailsServiceImpl
        this.samlFilter = new SAMLFilter(
                // provide an identity provider for which this filter will authenticate against
                new IdentityProvider(
                        name: idpConfig.name,
                        loginEndpoint: idpConfig.loginEndpoint,
                        loginBinding: idpConfig.loginBinding,
                        logoutEndpoint: idpConfig.logoutEndpoint,
                        logoutBinding: idpConfig.logoutBinding,
                        issuerSP: idpConfig.issuerSP,
                        issuerIDP: idpConfig.issuerIDP,
                        authnContextRefs: idpConfig.authnContextRefs,
                        authnContextComparisonType: idpConfig.authnContextComparisonType,
                        assertionConsumerServiceURL: idpConfig.assertionConsumerServiceURL,
                        assertionConsumerServiceLogoutURL: idpConfig.assertionConsumerServiceLogoutURL,
                        nameIDPolicyFormat: idpConfig.nameIDPolicyFormat,
                        forceAuthn: idpConfig.forceAuthn,
                        isPassive: idpConfig.isPassive,
                        signatureAlgorithm: idpConfig.signatureAlgorithm,
                        signatureCanonicalizationAlgorithm: idpConfig.signatureCanonicalizationAlgorithm,
                        digestAlgorithm: idpConfig.digestAlgorithm,
                        emailAssertionName: idpConfig.emailAssertionName,
                        emailAssertionNameFormat: idpConfig.emailAssertionNameFormat,
                        uuidAssertionName: idpConfig.uuidAssertionName,
                        uuidAssertionNameFormat: idpConfig.uuidAssertionNameFormat
                )
        )

        // set keystore values to be used in custom SAML filter via the SecurityProperties known by Spring config
        CredentialUtil.setKeyStorePath(icamConfiguration.keystore.file)
        CredentialUtil.setKeyStorePassword(icamConfiguration.keystore.password)
        CredentialUtil.setAlias(icamConfiguration.keystore.alias)
        CredentialUtil.setKeyPassword(icamConfiguration.keystore.keyPassword)

        // initialize custom SAML filter
        samlFilter.init()
    }

    @Override
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        // determine which request path we are filtering
        String path = request.getServletPath()
        String fullPath = request.getRequestURL()

        logger.debug("Custom Spring Security Filter:\n\t")
        logger.debug("path = ${path}")
        logger.debug("fullPath = ${fullPath}")
        logger.debug(samlFilter.identityProvider.info())

        boolean secureEndpoint = false
        securedEndpointsRegex.each { regex ->
            if (path.matches(regex)) {
                secureEndpoint = true
            }
        }

        // if we hit "consumeLogin" we've already returned from the identity provider and need to create our authentication context
        if(fullPath == samlFilter.identityProvider.assertionConsumerServiceURL) {
            Authentication authentication
            try {
                authentication = attemptAuthentication(request, response)
                if(authentication == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                    return
                }
            }
            catch(AuthenticationException failed) {
                logger.debug("${failed.message}")
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                return
            }

            try {
                SecurityContextHolder.getContext().setAuthentication(authentication)

                // set authenticated session to be checked to avoid redundant authentication attempts
                SAMLUtil.setAuthenticatedSession(request)

                // redirect to goto url
                SAMLUtil.redirectToGotoURL(request, response)
            }
            catch(Exception e) {
                logger.error(e.getMessage(), e)
                if(e.getCause() instanceof AccessDeniedException) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                    return
                }
            }
        }
        else if(fullPath == samlFilter.identityProvider.assertionConsumerServiceLogoutURL) {
            // TODO: handle logout by unsetting session?
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        }
        else if(secureEndpoint) {
            // delegate filter to our custom SAML filter
            samlFilter.doFilter(req, res, chain)
        }
        else {
            // return to other spring security filters
            chain.doFilter(req, res)
        }
    }

    @Override
    Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        // consumeLogin should have a SAMLResponse param in the request
        String samlResponseEncoded = request.getParameter("SAMLResponse")

        Authentication userAuthenticationToken = authUserBySAMLResponse(samlResponseEncoded, request)

        return userAuthenticationToken
    }

    private AbstractAuthenticationToken authUserBySAMLResponse(String samlResponseEncoded, HttpServletRequest request) {
        AbstractAuthenticationToken authenticationToken = null
        try {
            long clockSkew = 5000       // milliseconds before a lower time bound, or after an upper time bound, to consider still acceptable
            long messageLifetime = 5000 // milliseconds for which a message is valid after it is issued
            Map<String,String> assertions = SAMLConsume.getAssertions(samlResponseEncoded, request, clockSkew, messageLifetime)

            if(assertions.email != null) {
                Principal securityUser = new Principal() {
                    @Override
                    String getName() {
                        return assertions.email
                    }
                }
                UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(securityUser.getName())
                return new PreAuthenticatedAuthenticationToken(securityUser,"", userDetails.getAuthorities())
            }
        }
        catch(Exception e) {
            logger.error("Error during authUserBySAMLResponse", e)
        }
        return authenticationToken
    }
}
