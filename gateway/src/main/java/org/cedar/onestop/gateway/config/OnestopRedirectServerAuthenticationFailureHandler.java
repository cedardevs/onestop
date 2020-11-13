package org.cedar.onestop.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import reactor.core.publisher.Mono;


public class OnestopRedirectServerAuthenticationFailureHandler extends RedirectServerAuthenticationFailureHandler {

  Logger logger = LoggerFactory.getLogger(OnestopRedirectServerAuthenticationFailureHandler.class);

  public OnestopRedirectServerAuthenticationFailureHandler(String location) {
    super(location);
  }

  @Override
  public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException e) {
    logger.info("AUTHENTICATION FAILURE: " + e.toString());
    return super.onAuthenticationFailure(webFilterExchange, e);
  }
}