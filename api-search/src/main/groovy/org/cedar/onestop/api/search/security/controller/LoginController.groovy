package org.cedar.onestop.api.search.security.controller

import org.cedar.onestop.api.search.security.config.SecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletResponse

@Controller
class LoginController {

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService

    @GetMapping(SecurityConfig.LOGIN_PROFILE_ENDPOINT)
    @ResponseBody
    HashMap<String, Object> loginProfile(OAuth2AuthenticationToken authentication) {
        if(authentication == null) {
            return new HashMap<String, Object>()
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
            HashMap<String, Object> response = new HashMap<String, Object>()
            response.put("email", email)
            return response
        }
    }

    @RequestMapping(SecurityConfig.LOGIN_SUCCESS_ENDPOINT)
    void loginSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.sendRedirect("http://localhost:30000/onestop")
    }

}
