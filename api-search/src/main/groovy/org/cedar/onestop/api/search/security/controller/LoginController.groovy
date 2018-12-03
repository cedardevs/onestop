package org.cedar.onestop.api.search.security.controller

import org.cedar.onestop.api.search.security.config.LoginGovConfiguration
import org.cedar.onestop.api.search.security.config.RequestUtil
import org.cedar.onestop.api.search.security.config.SecurityConfig
import org.cedar.onestop.api.search.security.constants.LoginGovConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class LoginController {

    @Autowired
    LoginGovConfiguration loginGovConfiguration

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository

    @RequestMapping(value = SecurityConfig.LOGIN_PROFILE_ENDPOINT, method = [RequestMethod.GET, RequestMethod.OPTIONS])
    @ResponseBody
    HashMap<String, Object> loginProfile(HttpServletRequest httpServletRequest, OAuth2AuthenticationToken authentication) {
        if(authentication == null) {
            HashMap<String, Object> responseUnauthenticated = new HashMap<String, Object>()
            responseUnauthenticated.put("login", RequestUtil.getURL(httpServletRequest, SecurityConfig.LOGIN_ENDPOINT))
            return responseUnauthenticated
        }
        else {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
            String userInfoEndpointUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri
            String email = null
            if(!StringUtils.isEmpty(userInfoEndpointUri)) {
                RestTemplate restTemplate = new RestTemplate()
                HttpHeaders headers = new HttpHeaders()
                String authorizationHeader = "Bearer " + client.accessToken.tokenValue
                headers.add(HttpHeaders.AUTHORIZATION, authorizationHeader)
                HttpEntity<String> entity = new HttpEntity<>("", headers)
                ResponseEntity<Map> response = restTemplate.exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class)
                Map userAttributes = response.body
                email = userAttributes.get("email")
            }
            HashMap<String, Object> responseAuthenticated = new HashMap<String, Object>()
            responseAuthenticated.put("email", email)
            responseAuthenticated.put("logout", RequestUtil.getURL(httpServletRequest, SecurityConfig.LOGOUT_ENDPOINT))
            return responseAuthenticated
        }
    }

    @RequestMapping(SecurityConfig.LOGIN_SUCCESS_ENDPOINT)
    void loginSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.loginSuccessRedirect)
    }

    @RequestMapping(SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)
    void logoutSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.logoutSuccessRedirect)
    }

    @RequestMapping(DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL)
    String login() {
        // bypass the default Spring login page and go straight to login.gov authorization
        String authorizationRedirect = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + LoginGovConstants.LOGIN_GOV_REGISTRATION_ID
        return "redirect:" + authorizationRedirect
    }

}
