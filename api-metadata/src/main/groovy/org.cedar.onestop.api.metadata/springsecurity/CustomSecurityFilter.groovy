package org.cedar.onestop.api.metadata.springsecurity

import org.cedar.onestop.api.metadata.authorization.service.UserDetailsServiceImpl
import org.cedar.onestop.api.metadata.security.CredentialUtil
import org.cedar.onestop.api.metadata.security.IdentityProviderEnumeration
import org.cedar.onestop.api.metadata.security.SAMLConsume
import org.cedar.onestop.api.metadata.security.SAMLFilter
import org.cedar.onestop.api.metadata.security.SPConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component

import javax.annotation.RegEx
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.file.AccessDeniedException
import java.security.Principal
import java.util.regex.Pattern

@Component
class CustomSecurityFilter extends AbstractAuthenticationProcessingFilter {

    private final List<String> securedEndpointsRegex = [/^.+\/metadata/, /^.+\/admin\/.+$/, /^.+\/upload.html$/, /^.+\/userOnly$/]

    @Autowired
    ServerProperties serverProperties

    // create instance of SAMLFilter to be wrapped by this Spring aware filter bean
    // provide an identity provider for which this filter will authenticate against
//    private SAMLFilter samlFilter = new SAMLFilter(IdentityProviderEnumeration.ICAM_NOAA_LOCAL.getValue())
    private SAMLFilter samlFilter = new SAMLFilter(IdentityProviderEnumeration.LOGIN_GOV_LOCAL.getValue())

    UserDetailsServiceImpl userDetailsServiceImpl

    CustomSecurityFilter(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsServiceImpl) {
        super("/")
        this.setAuthenticationManager(authenticationManager)
        this.userDetailsServiceImpl = userDetailsServiceImpl
    }

    @Override
    protected void initFilterBean() throws ServletException {

        println("CUSTOM SECURITY FILTER::: initFilterBeans")

        // set keystore values to be used in custom SAML filter via the SecurityProperties known by Spring config
        CredentialUtil.setKeyStorePath(serverProperties.ssl.keyStore)
        CredentialUtil.setKeyStorePassword(serverProperties.ssl.keyStorePassword)
        CredentialUtil.setAlias(serverProperties.ssl.keyAlias)
        CredentialUtil.setKeyPassword(serverProperties.ssl.keyPassword)

        CredentialUtil.print()

        // initialize custom SAML filter
        samlFilter.init()
    }

    @Override
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        println("\n\n==============================================================")
        println("==============================================================")
        println("==============================================================")
        println("CustomSecurityFilter:::doFilter()")
        println("==============================================================")
        println("==============================================================")
        println("==============================================================\n\n")


        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        // determine which request path we are filtering
        String path = request.getServletPath()
        String fullPath = request.getRequestURL()

        println("CustomSecurityFilter:::path = ${path}")
        println("CustomSecurityFilter:::fullPath = ${fullPath}")

        println("IDP getAssertionConsumerServiceURL = ${samlFilter.identityProvider.getAssertionConsumerServiceURL()}")
        println("IDP getAssertionConsumerServiceLogoutURL = ${samlFilter.identityProvider.getAssertionConsumerServiceLogoutURL()}")

        boolean secureEndpoint = false
        securedEndpointsRegex.each { regex ->
            if (path.matches(regex)) {
                secureEndpoint = true
            }
        }


        // if we hit "consumeLogin" we've already returned from the identity provider and need to create our authentication context
        if(fullPath == samlFilter.identityProvider.assertionConsumerServiceURL) {

            println("CustomSecurityFilter:::[condition] fullPath == samlFilter.identityProvider.assertionConsumerServiceURL")

            Authentication authentication
            try {
                authentication = attemptAuthentication(request, response)

                println("CustomSecurityFilter:::authentication = ${authentication}")

                if(authentication == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                    return
                }
            }
            catch(AuthenticationException failed) {
                println("CustomSecurityFilter:::AuthenticationException = ${failed}")
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                return
            }

            try {

                SecurityContextHolder.getContext().setAuthentication(authentication)

                // set authenticated session to be checked to avoid redundant authentication attempts
                setAuthenticatedSession(request)

                // redirect to goto url
                redirectToGotoURL(request, response)

                // TODO: do we continue the chain somewhere here or after the redirect?
                // why is authorization not working as expected?
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
            println("CustomSecurityFilter:::[condition] fullPath == samlFilter.identityProvider.assertionConsumerServiceLogoutURL")
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        }
        // TODO: do something smarter here...?
        else if(secureEndpoint) {
            println("CustomSecurityFilter:::[condition] path is secured endpoint")

            // delegate filter to our custom SAML filter
            samlFilter.doFilter(req, res, chain)
        }
        else {
            println("CustomSecurityFilter:::[condition] ELSE")

            // return to other spring security filters
            chain.doFilter(req, res)
        }

        println("\n\n==============================================================")
        println("==============================================================\n\n")
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        println("CustomSecurityFilter::successfulAuthentication!!!")

        super.successfulAuthentication(request, response, chain, authResult)
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
            String principal = SAMLConsume.getSAMLPrincipal(samlResponseEncoded, request)

            if(principal != null) {
                Principal securityUser = new Principal() {
                    @Override
                    String getName() {
                        return principal
                    }
                }

                UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(securityUser.getName())
//                UserDetails userDetails = userDetailsService.loadUserByUsername(securityUser.getName())

                println("userDetails.getUsername(): ${userDetails.getUsername()}")
                println("userDetails.getPassword(): ${userDetails.getPassword()}")

                userDetails.getAuthorities().each {
                    println("it.getAuthority(): ${it.getAuthority()}")
                }
                return new PreAuthenticatedAuthenticationToken(securityUser,"", userDetails.getAuthorities())
            }
        }
        catch(Exception e) {
            logger.error("Error during authUserBySAMLResponse", e)
        }
        return authenticationToken
    }

    // TODO: can we get these functions more inside the security package SAML utility?
    private void setAuthenticatedSession(HttpServletRequest req) {
        req.getSession().setAttribute(SPConstants.AUTHENTICATED_SESSION_ATTRIBUTE, true)
    }

    private void redirectToGotoURL(HttpServletRequest req, HttpServletResponse resp) {
        String gotoURL = (String)req.getSession().getAttribute(SPConstants.GOTO_URL_SESSION_ATTRIBUTE)
        logger.info("Redirecting to requested URL: " + gotoURL)
        try {
            resp.sendRedirect(gotoURL)
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }
}
