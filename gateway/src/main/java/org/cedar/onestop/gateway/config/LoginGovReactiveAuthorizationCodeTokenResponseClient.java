package org.cedar.onestop.gateway.config;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.oauth2.core.web.reactive.function.OAuth2BodyExtractors.oauth2AccessTokenResponse;

public class LoginGovReactiveAuthorizationCodeTokenResponseClient extends WebClientReactiveAuthorizationCodeTokenResponseClient {

  private WebClient webClient;

  public LoginGovReactiveAuthorizationCodeTokenResponseClient(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<OAuth2AccessTokenResponse> getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
    return Mono.defer(() -> {
      ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
      OAuth2AuthorizationExchange authorizationExchange = authorizationGrantRequest.getAuthorizationExchange();
      String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
      BodyInserters.FormInserter<String> body = body(authorizationExchange, clientRegistration);

      return this.webClient.post()
          .uri(tokenUri)
          .accept(MediaType.APPLICATION_JSON)
          .headers(headers -> {
            if (ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
              headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
            }
          })
          .body(body)
          .exchange()
          .flatMap(response -> response.body(oauth2AccessTokenResponse()))
          .map(response -> {
            if (response.getAccessToken().getScopes().isEmpty()) {
              response = OAuth2AccessTokenResponse.withResponse(response)
                  .scopes(authorizationExchange.getAuthorizationRequest().getScopes())
                  .build();
            }
            return response;
          });
    });
  }

  private static BodyInserters.FormInserter<String> body(OAuth2AuthorizationExchange authorizationExchange, ClientRegistration clientRegistration) {
    OAuth2AuthorizationResponse authorizationResponse = authorizationExchange.getAuthorizationResponse();
    BodyInserters.FormInserter<String> body = BodyInserters
        .fromFormData(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
        .with(OAuth2ParameterNames.CODE, authorizationResponse.getCode());
    String redirectUri = authorizationExchange.getAuthorizationRequest().getRedirectUri();
    String codeVerifier = authorizationExchange.getAuthorizationRequest().getAttribute(PkceParameterNames.CODE_VERIFIER);
    if (redirectUri != null) {
      body.with(OAuth2ParameterNames.REDIRECT_URI, redirectUri);
    }
    if (!ClientAuthenticationMethod.BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
      body.with(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
    }
    if (ClientAuthenticationMethod.POST.equals(clientRegistration.getClientAuthenticationMethod())) {
      body.with(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
    }
    if (codeVerifier != null) {
      body.with(PkceParameterNames.CODE_VERIFIER, codeVerifier);
    }

    body.with("client_assertion", "YODAWG");
    body.with("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");

    return body;
  }
}
