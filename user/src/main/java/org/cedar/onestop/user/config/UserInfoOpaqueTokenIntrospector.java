package org.cedar.onestop.user.config;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;

public class UserInfoOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final WebClient rest = WebClient.create();

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    return requestUserInfo(token);
  }

  private OAuth2AuthenticatedPrincipal requestUserInfo(String token) {
    String response = this.rest.get()
        .uri("https://idp.int.identitysandbox.gov/api/openid_connect/userinfo")
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .bodyToMono(String.class)
        .block();

    return null;
  }
}