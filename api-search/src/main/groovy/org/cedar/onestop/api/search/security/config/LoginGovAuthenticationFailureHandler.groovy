package org.cedar.onestop.api.search.security.config

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class LoginGovAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler  {

  @Override
  void onAuthenticationFailure(HttpServletRequest request,
                                    HttpServletResponse response, AuthenticationException exception){

    redirectStrategy.sendRedirect(request, response, SecurityConfig.LOGIN_SUCCESS_ENDPOINT )
  }
}
