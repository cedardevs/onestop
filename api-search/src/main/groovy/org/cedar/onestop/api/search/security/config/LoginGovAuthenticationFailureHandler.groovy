package org.cedar.onestop.api.search.security.config

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets

class LoginGovAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler  {

  @Override
  void onAuthenticationFailure(HttpServletRequest request,
                                    HttpServletResponse response, AuthenticationException exception){
    // redirect to failure endpoint with optional message on why auth failed
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(SecurityConfig.LOGIN_FAILURE_ENDPOINT)
    if(exception.message) {
      // Ideally the exception would come with a unique code to identify the type of error.
      // Hopefully, future login.gov implementation will provide this feature. See my issue here:
      // https://github.com/18F/identity-idp/issues/2851
      logger.debug("authentication failed with message: " + exception.message)
      String urlEncodedMessage = URLEncoder.encode(exception.message, StandardCharsets.UTF_8.name())
      uriBuilder.queryParam('failureMessage', urlEncodedMessage)
    }
    String urlFailureRedirect = uriBuilder.build().toUriString()
    redirectStrategy.sendRedirect(request, response, urlFailureRedirect)
  }
}
