package org.cedar.onestop.api.search.security.controller

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.search.security.config.LoginGovConfiguration
import org.cedar.onestop.api.search.security.config.NonceUtil
import org.cedar.onestop.api.search.security.config.RequestUtil
import org.cedar.onestop.api.search.security.config.SecurityConfig
import org.cedar.onestop.api.search.security.constants.LoginGovConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

@Slf4j
@Profile("login-gov")
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
    HashMap<String, Object> loginProfile(
            HttpServletRequest httpServletRequest,
            OAuth2AuthenticationToken authentication,
            @RequestParam(value = "failure", required = false, defaultValue = "false") boolean failure,
            @RequestParam(value = "failureMessage", required = false) String failureMessage
    ) {
        if(authentication == null) {
            HashMap<String, Object> responseUnauthenticated = new HashMap<String, Object>()
            responseUnauthenticated.put("login", RequestUtil.getURL(httpServletRequest, DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL))
            if(failure) {
                responseUnauthenticated.put("error", "Login was canceled or unsuccessful.")
                if(failureMessage) {
                    responseUnauthenticated.put("failureMessage", failureMessage)
                }
            }
            return responseUnauthenticated
        }
        else {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
            String userInfoEndpointUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri

            if(!NonceUtil.extractAndCheckNonce(authentication)){
                HashMap<String, Object> responseUnauthenticated = new HashMap<String, Object>()
                responseUnauthenticated.put("error", "Replay attack detected.")
                responseUnauthenticated.put("login", RequestUtil.getURL(httpServletRequest, DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL))
                return responseUnauthenticated
            }

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

    @RequestMapping(DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL)
    String login() {
        // bypass the default Spring login page and go straight to login.gov authorization
        String authorizationRedirect = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + LoginGovConstants.LOGIN_GOV_REGISTRATION_ID
        return "redirect:" + authorizationRedirect
    }

    @RequestMapping(SecurityConfig.LOGIN_SUCCESS_ENDPOINT)
    void loginSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.loginSuccessRedirect)
    }

    @RequestMapping(SecurityConfig.LOGIN_FAILURE_ENDPOINT)
    void loginFailure(HttpServletResponse httpServletResponse, @RequestParam(value = 'failureMessage', required = false) String failureMessage) {
        // In case you're wondering why we have two redirects for failures...

        // Our configs set `loginGovConfiguration.loginFailureRedirect` from an environment variable.
        // In our case, it coincidentally equals SecurityConfig.LOGIN_PROFILE_ENDPOINT (/login_profile)

        // The flexibility/possibility exists to configure a custom endpoint for failure that behaves differently,
        // but we wanted a convenient endpoint that could handle successes as well as failures in a way that is
        // more consumable by a browser client / JS app.

        // The reason we made the failure endpoint configurable instead of implicitly redirecting to the /login_profile
        // endpoint was to reduce as much "magic" from the code as possible. A developer trying to debug a login failure
        // might otherwise be tempted to believe this failure was swallowed by some Spring default behavior or be
        // confused when they are redirected to something only seen in the config as a "success" endpoint.
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(loginGovConfiguration.loginFailureRedirect)

        if (failureMessage) {
            // caught an error that would be useful to add to the default failure message
            // (e.g. - 'Client assertion Signature verification raised'... IOW: the public cert registered w/login.gov
            //          is probably not corresponding to the keystore this app is configured to use)
            uriBuilder
                    .queryParam('failure', true)
                    .queryParam('failureMessage', URLEncoder.encode(failureMessage, StandardCharsets.UTF_8.name())
            )
        } else {
            uriBuilder
                    .queryParam('failure', true)

        }
        String urlFailureRedirect = uriBuilder.build().toUriString()
        httpServletResponse.sendRedirect(urlFailureRedirect)
    }

    @RequestMapping(SecurityConfig.LOGOUT_SUCCESS_ENDPOINT)
    void logoutSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect(loginGovConfiguration.logoutSuccessRedirect)
    }
}