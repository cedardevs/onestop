package org.cedar.onestop.api.metadata.authorization.configs

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.stereotype.Component

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This class sets an entry point with a response for BASIC AUTH.
 * REALM notifies the user what username/ password they may need if we use multiple realms.
 * Will deprecate this in favor of other authentication methods.
 */
@Component
class AuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException, ServletException {
        response.addHeader("WWW-Authenticate", "Basic realm=" +getRealmName())
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
        PrintWriter writer = response.getWriter()
        writer.println("HTTP Status 401 - " + authEx.getMessage())
    }

    @Override
    void afterPropertiesSet() throws Exception {
        setRealmName("MiddleEarth")
        super.afterPropertiesSet()
    }
}