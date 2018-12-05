package org.cedar.onestop.api.search.security.config

import org.cedar.onestop.api.search.security.constants.LoginGovConstants
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoginGovLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Override
    void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // extrapolate id_token from original token response (needed for logout request)
        // Authentication -> OAuth2AuthenticationToken -> OidcUser -> OidcIdToken
        // This cast is possible because of the 'openid' scope/flow in the client registration config
        OAuth2AuthenticationToken authenticationToken = authentication as OAuth2AuthenticationToken
        OidcUser user = authenticationToken.principal as OidcUser
        OidcIdToken idToken = user.idToken
        String idTokenHint = idToken.tokenValue

        // Call the login.gov supported "RP-Initiated Logout" to invalidate login.gov's session
        // https://developers.login.gov/oidc/#logout
        String state = UUID.randomUUID().toString()
        String postLogoutRedirectUri = RequestUtil.getURL(request, SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(LoginGovConstants.LOGIN_GOV_LOGOUT_ENDPOINT)
                .queryParam(LoginGovConstants.LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT, idTokenHint)
                .queryParam(LoginGovConstants.LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI, postLogoutRedirectUri)
                .queryParam(LoginGovConstants.LOGIN_GOV_LOGOUT_PARAM_STATE, state)

        redirectStrategy.sendRedirect(request, response, builder.toUriString())
    }
}
