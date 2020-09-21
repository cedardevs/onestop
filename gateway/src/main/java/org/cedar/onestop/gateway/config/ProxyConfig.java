package org.cedar.onestop.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.web.reactive.function.BodyExtractors.toDataBuffers;
import static org.springframework.web.reactive.function.BodyInserters.fromDataBuffers;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProxyConfig {

  private static final String API_PREFIX = "/api/";
  private static final String CATCH_ALL_SUFFIX = "/**";
  private static final String SERVICE_PARAM = "{service}";
  private static final List<String> STRIPPED_HEADERS = Arrays.asList(COOKIE, SET_COOKIE, SET_COOKIE2, HOST, TRANSFER_ENCODING, CONNECTION, "keep-alive");

  private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
  private final GatewayConfig config;
  Logger logger = LoggerFactory.getLogger(ProxyConfig.class);

//  @Autowired
//  SecurityConfig securityConfig;

  public ProxyConfig(final ReactiveOAuth2AuthorizedClientService authorizedClientService, final GatewayConfig config) {
    this.authorizedClientService = authorizedClientService;
    this.config = config;
  }

//  @Bean
//  public RouterFunction<ServerResponse> index() {
////    OAuth2AuthorizedClient authorizedClient = this.getAuthorizedClient(authentication).block();
////    System.out.println(request.toString());
//    return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(index));
////    System.out.println("redirected to root");
////    return route(GET("/"), req -> ServerResponse.temporaryRedirect(URI.create("http://localhost/onestop/"))
////            .build());
//  }
//  @Bean
//  public RouterFunction<ServerResponse> index(@Value("classpath:/public/index.html") final Resource index) {
//    return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(index));
//  }

  @Bean
  public RouterFunction<ServerResponse> proxy(WebClient webClient) {
    return route(path(API_PREFIX + SERVICE_PARAM + CATCH_ALL_SUFFIX),
        request -> authorizedClient(request)
            .map(attr -> webClient.method(request.method())
                .uri(toBackendUri(request))
                .headers(cleanedHeaders(request.headers().asHttpHeaders()))
//                .header("Authorization", "Bearer " + createJwtHeader(request.principal().cast(OidcUser.class)))
                .body(fromDataBuffers(request.exchange().getRequest().getBody()))
                .attributes(attr))
            .flatMap(WebClient.RequestHeadersSpec::exchange)
            .flatMap(response -> ServerResponse.status(response.statusCode())
                .headers(cleanedHeaders(request.headers().asHttpHeaders()))
                .body(fromDataBuffers(response.body(toDataBuffers()))))
    );
  }

//  private Consumer<HttpHeaders> cleanedRequestHeaders(final ServerRequest request) {
//    System.out.println("cleanedRequestHeaders");
//    logger.info("cleanedRequestHeaders");
//
//    return headers -> {
//      System.out.println("headers ->");
//      HttpHeaders httpHeaders = request.headers().asHttpHeaders();
//      headers.putAll(httpHeaders);
//
//      request.principal()
//              .cast(OidcUser.class)
//              .map(this::createJwtHeader)
//              .subscribe(headers::putAll);
//      STRIPPED_HEADERS.forEach(headers::remove);
//    };
//  }

//  private HttpHeaders createJwtHeader(OidcUser oidcUser){
//    System.out.println("createJwtHeader");
//    logger.info("createJwtHeader");
//    HttpHeaders authHeader = new HttpHeaders();
//    String jwt = "";
//
//    try {
//      jwt = JWT.create()
//              .withSubject(oidcUser.getSubject())
//              .withIssuer("onestop-gateway")
//              .withAudience(oidcUser.getAudience().toString())
//              .withJWTId(UUID.randomUUID().toString())
//              .withClaim("email", oidcUser.getEmail())
//              .withClaim("emailVerified", oidcUser.getEmailVerified())
////            .withClaim("roles", [getRoles(user.getEmail)])
//              .withExpiresAt((Date.from(oidcUser.getExpiresAt())))
//              .sign(Algorithm.RSA256(securityConfig.keystoreUtil.rsaPublicKey(), securityConfig.keystoreUtil.rsaPrivateKey()));
//    }catch(Exception e){
//      logger.info("Failed to create JWT: ", e);
//    }
//    System.out.println("JWT HEADER");
//    System.out.println(jwt);
//    authHeader.setBearerAuth(jwt);
//    return authHeader;
//  }

  private Mono<OidcUser> getOidcUser(final ServerRequest request){
//    Mono<OidcUser> user = Mono.empty().cast(OidcUser.class);
//    user = request.principal().cast(OidcUser.class);
    return request.principal().cast(OidcUser.class);
//    OidcUser user = null;
//    try{
//      user = request.principal()
//              .cast(OidcUser.class).block();
////      .subscribe(
////              oidcUser -> user = oidcUser
////      );
////      OidcUser principal = ((OidcUser) auth.getPrincipal());
////      System.out.println(principal.getUserInfo().getEmail());
////      user = (OidcUser)request.principal();
//    }catch(Exception e){
//      logger.warn("Could not cast request principal to OidcUser", e);
//    }
//    return user;
  }

  private Consumer<HttpHeaders> cleanedHeaders(final HttpHeaders httpHeaders) {
    return headers -> {
      headers.putAll(httpHeaders);
      STRIPPED_HEADERS.forEach(headers::remove);
    };
  }

  private String toBackendUri(final ServerRequest request) {
    final String service = request.pathVariable("service");
    final String backend = config.backend(service);
//    final String userId = getUserId(request);
    return UriComponentsBuilder.fromUriString(backend)
        .path(request.path().substring(API_PREFIX.length() + service.length()))
        .queryParams(request.queryParams())
        .build().toString();
  }

  private Mono<Consumer<Map<String, Object>>> authorizedClient(final ServerRequest request) {
    return request.principal()
        .cast(OAuth2AuthenticationToken.class)
        .flatMap(this::loadAuthorizedClient)
        .map(ServerOAuth2AuthorizedClientExchangeFilterFunction::oauth2AuthorizedClient);
  }

  private Mono<OAuth2AuthorizedClient> loadAuthorizedClient(OAuth2AuthenticationToken authentication) {
    return authorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
  }


}