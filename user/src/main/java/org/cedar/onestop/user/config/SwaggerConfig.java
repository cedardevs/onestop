package org.cedar.onestop.user.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.matching.matcher.HttpMethodMatcher;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.springframework.annotation.AnnotationConfig;
import org.pac4j.springframework.component.ComponentConfig;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ApiInfoBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Profile("logingov")
@Configuration
@EnableSwagger2
@Import({ComponentConfig.class, AnnotationConfig.class})
@ComponentScan(basePackages = "org.pac4j.springframework.web")
public class SwaggerConfig extends WebMvcConfigurationSupport {
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
        .pathMapping("/")
        .apiInfo(metaData());
  }

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  private ApiInfo metaData() {
    return new ApiInfoBuilder()
        .title("SAVE SEARCH API")
        .description("Spring Boot REST API for User save searches Microservice")
        .version("1.0.0")
        .license("")
        .licenseUrl("")
        .contact(new Contact("cedardevs",
            "https://github.com/cedardevs",
            "cedar.cires@colorado.edu "))
        .build();
  }


  //// SECURITY STUFF
  private static final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

  static final String ROLE_PUBLIC = "ROLE_PUBLIC";

  static class Authorization {
    private Map<String, List<String>> roles;

    Map<String, List<String>> getRoles() {
      return roles;
    }

    void setRoles(Map<String, List<String>> roles) {
      this.roles = roles;
    }
  }

  @Bean
  @ConfigurationProperties(prefix = "authorization")
  Authorization authz() {
    return new Authorization();
  }

  // Pac4J Configuration
  static class LoginGovConfiguration {
    public String clientId;

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getDiscoveryUri() {
      return discoveryUri;
    }

    public void setDiscoveryUri(String discoveryUri) {
      this.discoveryUri = discoveryUri;
    }

    public String getScopes() {
      return scopes;
    }

    public void setScopes(String scopes) {
      this.scopes = scopes;
    }

    public boolean isUseNonce() {
      return useNonce;
    }

    public void setUseNonce(boolean useNonce) {
      this.useNonce = useNonce;
    }

    public String getAcrValues() {
      return acrValues;
    }

    public void setAcrValues(String acrValues) {
      this.acrValues = acrValues;
    }

    public String discoveryUri;
    public String scopes;
    public boolean useNonce;
    public String acrValues;
  }

  @Bean
  @ConfigurationProperties(prefix = "logingov")
  LoginGovConfiguration loginGov() {
    return new LoginGovConfiguration();
  }

  static final String LOGIN_GOV_REST_CLIENT = "LoginGovOidcClient";
  static final String AUTHORIZER_PUBLIC = "public";
  static final String MATCHER_HTTP_METHOD_SECURE = "http-methods-secure";

  OidcClient<OidcConfiguration> loginGovClient() {

//    login-gov:
//    authorization-uri: https://idp.int.identitysandbox.gov/openid_connect/authorize
//    token-uri: https://idp.int.identitysandbox.gov/api/openid_connect/token
//    user-info-uri: https://idp.int.identitysandbox.gov/api/openid_connect/userinfo
//    user-name-attribute: sub
//    jwk-set-uri: https://idp.int.identitysandbox.gov/api/openid_connect/certs

    LoginGovConfiguration loginGov = loginGov();

    log.info("\n===loginGovClient===\n");
    final String LOGIN_GOV_REGISTRATION_ID = "login-gov";
    final String LOGIN_GOV_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    final Long LOGIN_GOV_TOKEN_EXPIRATION_TIME = 3600000L; // 1 hour

    // The Authentication Context Class Reference values used to specify the LOA (level of assurance)
    // of an account, either LOA1 or LOA3. This and the scope determine which user attributes will be available
    // in the user info response. The possible parameter values are:
    final String LOGIN_GOV_LOA1 = "http://idmanagement.gov/ns/assurance/loa/1";
    final String LOGIN_GOV_LOA3 = "http://idmanagement.gov/ns/assurance/loa/3";

    // Logout Constants
    final String LOGIN_GOV_LOGOUT_ENDPOINT = "https://idp.int.identitysandbox.gov/openid_connect/logout";
    final String LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT = "id_token_hint";
    final String LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
    final String LOGIN_GOV_LOGOUT_PARAM_STATE = "state";

    log.info("loginGov.clientId = " + loginGov.clientId);
    log.info("loginGov.discoveryUri = " + loginGov.discoveryUri);
    log.info("loginGov.scopes = " + loginGov.scopes);
    log.info("loginGov.useNonce = " + loginGov.useNonce);

    // TODO: using google temporarily to develop the auth flow for UI
    final OidcConfiguration oidcConfiguration = new OidcConfiguration();
    oidcConfiguration.setClientId("kbyuFDidLLm280LIwVFiazOqjO3ty8KH");
    oidcConfiguration.setSecret("60Op4HFM0I8ajz0WdiStAbziZ-VFQttXuxixHHs2R7r7-CW8GR79l-mmLqMhc-Sa");
    oidcConfiguration.setDiscoveryURI("https://samples.auth0.com/.well-known/openid-configuration");
    oidcConfiguration.setPreferredJwsAlgorithm(JWSAlgorithm.PS384);
//    oidcConfiguration.addCustomParam("prompt", "consent");
    final OidcClient<OidcConfiguration> oidcClient = new OidcClient<>(oidcConfiguration);
    oidcClient.setAuthorizationGenerator((ctx, profile) -> {
      profile.addRole("ROLE_ADMIN");
      return Optional.of(profile);
    });

    // TODO: Can login.gov support auth methods other than 'private_key_jwt' or PKCE?
    // or can pac4j give better guidance on supporting either?
//    OidcConfiguration oidcConfiguration = new OidcConfiguration();
//    oidcConfiguration.setClientId(loginGov.clientId);
//    oidcConfiguration.setDiscoveryURI(loginGov.discoveryUri);
//    oidcConfiguration.setScope(loginGov.scopes);
//    oidcConfiguration.setUseNonce(loginGov.useNonce);
//    oidcConfiguration.setClientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT);
//    oidcConfiguration.setSecret("mysecret");
//    oidcConfiguration.setCustomParams(Map.of("acr_values", "http://idmanagement.gov/ns/assurance/loa/1"));
//    //oidcConfiguration.setCustomParams(Map.of("acr_values", loginGov.acrValues));

    AuthorizationGenerator authGen = (context, profile) -> {
      // all users coming in through `login.gov` are given `ROLE_PUBLIC`
      profile.addRole(ROLE_PUBLIC);
      return Optional.of(profile);
    };

//    final OidcClient<OidcConfiguration> oidcClient = new OidcClient<>(oidcConfiguration);

    oidcClient.addAuthorizationGenerator(authGen);
    oidcClient.setName(LOGIN_GOV_REST_CLIENT);
    oidcClient.setCallbackUrl("https://openidconnect.net/callback");

    return oidcClient;
  }

  @Bean
  Config config() {
    log.info("\n===configUI===\n");

    OidcClient<OidcConfiguration> loginGovClient = loginGovClient();
    Clients uiClients = new Clients(loginGovClient);

    final Config config = new Config(uiClients);
    // "authorizer"s are meant to check authorizations on the authenticated user profile(s) or on the current web context
    Authorizer<OidcProfile> authorizerPublic = new RequireAnyRoleAuthorizer<>(ROLE_PUBLIC);
    config.addAuthorizer(AUTHORIZER_PUBLIC, authorizerPublic);

    // "matcher"s defines whether the security must apply on the security filter
    // - all POST/PUT/PATCH/DELETE endpoints should be secured, and this matcher allows us to intercept them
    config.addMatcher(MATCHER_HTTP_METHOD_SECURE, new HttpMethodMatcher(
        HttpConstants.HTTP_METHOD.GET,
        HttpConstants.HTTP_METHOD.POST,
        HttpConstants.HTTP_METHOD.PUT,
        HttpConstants.HTTP_METHOD.PATCH,
        HttpConstants.HTTP_METHOD.DELETE
    ));

    return config;
  }

  @Override
  protected void addInterceptors(InterceptorRegistry registry) {
    log.info("\n===addInterceptors===\n");

    SecurityInterceptor interceptorHttpMethodsSecure = new SecurityInterceptor(config(), LOGIN_GOV_REST_CLIENT, AUTHORIZER_PUBLIC, MATCHER_HTTP_METHOD_SECURE);
    //SecurityInterceptor interceptorResurrection = new SecurityInterceptor(config(), LOGIN_GOV_REST_CLIENT, AUTHORIZER_PUBLIC);

    // this will intercept and secure all POST/PUT/PATCH/DELETE requests
    registry.addInterceptor(interceptorHttpMethodsSecure).addPathPatterns("/test/**");

    // because the above interceptor will not catch the GET `/metadata/**/resurrection` endpoints
    //registry.addInterceptor(interceptorResurrection).addPathPatterns("/metadata/**/resurrection");
  }
}
