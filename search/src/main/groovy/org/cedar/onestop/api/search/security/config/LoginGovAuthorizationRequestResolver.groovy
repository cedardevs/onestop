package org.cedar.onestop.api.search.security.config

import org.cedar.onestop.api.search.security.constants.LoginGovConstants
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

import javax.servlet.http.HttpServletRequest

class LoginGovAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId"
    private OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver
    private AntPathRequestMatcher authorizationRequestMatcher

    LoginGovAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        defaultAuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        )
        String pattern = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}"
        authorizationRequestMatcher = new AntPathRequestMatcher(pattern)
    }

    @Override
    OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(request)
        if(authorizationRequest == null) {
            return null
        }
        else {
            return customAuthorizationRequest(authorizationRequest, request.session.id)
        }
    }

    @Override
    OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId)
        if(authorizationRequest == null) {
            return null
        }
        else {
            return customAuthorizationRequest(authorizationRequest, request.session.id)
        }
    }

    OAuth2AuthorizationRequest customAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, String sessionId ) {
        String registrationId = resolveRegistrationId(authorizationRequest)
        LinkedHashMap additionalParameters = new LinkedHashMap(authorizationRequest.additionalParameters)
        // set login.gov specific params
        // https://developers.login.gov/oidc/#authorization
        if(registrationId == LoginGovConstants.LOGIN_GOV_REGISTRATION_ID) {
            String nonce = NonceUtil.generateNonce(sessionId)
            additionalParameters.put("acr_values", LoginGovConstants.LOGIN_GOV_LOA1)
            additionalParameters.put("nonce", nonce)
        }

        return OAuth2AuthorizationRequest
            .from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build()
    }

    String resolveRegistrationId(OAuth2AuthorizationRequest authorizationRequest) {
        return authorizationRequest.additionalParameters.get(OAuth2ParameterNames.REGISTRATION_ID) as String
    }
}
