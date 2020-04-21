package org.cedar.onestop.gateway.config;

import com.nimbusds.openid.connect.sdk.Nonce;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoginGovServerAuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {

  private final ServerOAuth2AuthorizationRequestResolver defaultRequestResolver;

  public LoginGovServerAuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository) {
    this.defaultRequestResolver =
        new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
  }
  @Override
  public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
    return customAuthorizationRequest(this.defaultRequestResolver.resolve(exchange));
  }

  @Override
  public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
    return customAuthorizationRequest(this.defaultRequestResolver.resolve(exchange, clientRegistrationId));
  }

  private Mono<OAuth2AuthorizationRequest> customAuthorizationRequest(Mono<OAuth2AuthorizationRequest> authorizationRequest) {
    return authorizationRequest.map(request -> {
      Map<String, Object> additionalParameters = new LinkedHashMap<>(request.getAdditionalParameters());
      additionalParameters.put("acr_values", LoginGovConstants.LOGIN_GOV_IAL1);
      additionalParameters.put("nonce", new Nonce());
      return OAuth2AuthorizationRequest.from(request)
          .additionalParameters(additionalParameters)
          .build();
    });
  }
}