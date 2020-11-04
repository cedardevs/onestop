package org.cedar.onestop.gateway.config;

import java.time.Duration;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Token Relay Gateway Filter with Token Refresh. This can be removed when issue {@see https://github.com/spring-cloud/spring-cloud-security/issues/175} is closed.
 * Implementierung in Anlehnung an {@link ServerOAuth2AuthorizedClientExchangeFilterFunction}
 */
@Component
public class TokenRelayWithTokenRefreshGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

  private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

  private static final Duration accessTokenExpiresSkew = Duration.ofSeconds(3);

  public TokenRelayWithTokenRefreshGatewayFilterFactory(ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
                                                        ReactiveClientRegistrationRepository clientRegistrationRepository) {
    super(Object.class);
    this.authorizedClientManager = createDefaultAuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
  }

  private static ReactiveOAuth2AuthorizedClientManager createDefaultAuthorizedClientManager(
    ReactiveClientRegistrationRepository clientRegistrationRepository,
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

    final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
      ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
        .authorizationCode()
        .refreshToken(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
        .clientCredentials(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
        .password(configurer -> configurer.clockSkew(accessTokenExpiresSkew))
        .build();
    final DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
      clientRegistrationRepository, authorizedClientRepository);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

    return authorizedClientManager;
  }

  public GatewayFilter apply() {
    return apply((Object) null);
  }

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> exchange.getPrincipal()
      // .log("token-relay-filter")
      .filter(principal -> principal instanceof OAuth2AuthenticationToken)
      .cast(OAuth2AuthenticationToken.class)
      .flatMap(this::authorizeClient)
      .map(OAuth2AuthorizedClient::getAccessToken)
      .map(token -> withBearerAuth(exchange, token))
      // TODO: adjustable behavior if empty
      .defaultIfEmpty(exchange).flatMap(chain::filter);
  }

  private ServerWebExchange withBearerAuth(ServerWebExchange exchange, OAuth2AccessToken accessToken) {
    return exchange.mutate().request(r -> r.headers(headers -> headers.setBearerAuth(accessToken.getTokenValue()))).build();
  }

  private Mono<OAuth2AuthorizedClient> authorizeClient(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    final String clientRegistrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
    return Mono.defer(() -> authorizedClientManager.authorize(createOAuth2AuthorizeRequest(clientRegistrationId, oAuth2AuthenticationToken)));
  }

  private OAuth2AuthorizeRequest createOAuth2AuthorizeRequest(String clientRegistrationId, Authentication principal) {
    return OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId).principal(principal).build();
  }
}