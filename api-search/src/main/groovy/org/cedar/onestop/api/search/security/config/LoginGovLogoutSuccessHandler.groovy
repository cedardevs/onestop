package org.cedar.onestop.api.search.security.config

import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    // TODO: should we call the login.gov supported "RP-Initiated Logout" to invalidate login.gov's session too?
    // https://developers.login.gov/oidc/#logout
    @Override
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // call to https://idp.int.identitysandbox.gov/openid_connect/logout would probably go here...

        super.onLogoutSuccess(request, response, authentication)
    }
}
