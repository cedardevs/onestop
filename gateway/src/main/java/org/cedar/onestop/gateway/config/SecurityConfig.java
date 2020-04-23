package org.cedar.onestop.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@EnableWebFluxSecurity
public class SecurityConfig {

  @Autowired
  ReactiveClientRegistrationRepository clientRegistrations;

  @Autowired
  ServerOAuth2AuthorizedClientRepository authorizedClients;

  public static final String API_MATCHER_PATH = "/api/**";

  @Bean
  LoginGovReactiveAuthorizationCodeTokenResponseClient tokenResponseClient(WebClient webClient) {
    return new LoginGovReactiveAuthorizationCodeTokenResponseClient(webClient);
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    // the matcher for all paths that need to be secured (require a logged-in user)
    final ServerWebExchangeMatcher apiPathMatcher = pathMatchers(API_MATCHER_PATH);

    return http
        .authorizeExchange().matchers(apiPathMatcher).authenticated()
        .anyExchange().permitAll()
        .and().httpBasic().disable()
        .csrf().disable()
        .oauth2Client()
        .and()
        .oauth2Login()
        .authorizationRequestResolver(new LoginGovServerAuthorizationRequestResolver(clientRegistrations))
        .and()
        .build();
  }
}

