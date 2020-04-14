package org.cedar.onestop.user.security;

//import org.pac4j.core.authorization.authorizer.Authorizer;
//import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
//import org.pac4j.core.authorization.generator.AuthorizationGenerator;
//import org.pac4j.core.client.Clients;
//import org.pac4j.core.config.Config;
//import org.pac4j.core.context.HttpConstants;
//import org.pac4j.core.matching.matcher.HttpMethodMatcher;
//import org.pac4j.oidc.client.OidcClient;
//import org.pac4j.oidc.config.OidcConfiguration;
//import org.pac4j.oidc.profile.OidcProfile;
//import org.pac4j.springframework.annotation.AnnotationConfig;
//import org.pac4j.springframework.component.ComponentConfig;
//import org.pac4j.springframework.web.SecurityInterceptor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.*;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;

class SecurityEnabledConfig {

}

//@Profile("logingov")
//@Configuration
//@Import({ComponentConfig.class, AnnotationConfig.class})
//@ComponentScan(basePackages = "org.pac4j.springframework.web")
//class SecurityEnabledConfig implements WebMvcConfigurer {

//  private static final Logger log = LoggerFactory.getLogger(SecurityEnabledConfig.class);
//
//  static final String ROLE_PUBLIC = "ROLE_PUBLIC";
//
//  static class Authorization {
//    private Map<String, List<String>> roles;
//
//    Map<String, List<String>> getRoles() {
//      return roles;
//    }
//
//    void setRoles(Map<String, List<String>> roles) {
//      this.roles = roles;
//    }
//  }
//
//  @Bean
//  @ConfigurationProperties(prefix = "authorization")
//  Authorization authz() {
//    return new Authorization();
//  }
//
//  // Pac4J Configuration
//  static class LoginGovConfiguration {
//    public String clientId;
//    public String discoveryUri;
//    public String scopes;
//    public boolean useNonce;
//    public String acrValues;
//  }
//
//  @Bean
//  @ConfigurationProperties(prefix = "logingov")
//  LoginGovConfiguration loginGov() {
//    return new LoginGovConfiguration();
//  }
//
//  static final String LOGIN_GOV_REST_CLIENT = "LoginGovOidcClient";
//  static final String AUTHORIZER_PUBLIC = "public";
//  static final String MATCHER_HTTP_METHOD_SECURE = "http-methods-secure";
//
//  OidcClient<OidcConfiguration> loginGovClient() {
//
////    login-gov:
////    authorization-uri: https://idp.int.identitysandbox.gov/openid_connect/authorize
////    token-uri: https://idp.int.identitysandbox.gov/api/openid_connect/token
////    user-info-uri: https://idp.int.identitysandbox.gov/api/openid_connect/userinfo
////    user-name-attribute: sub
////    jwk-set-uri: https://idp.int.identitysandbox.gov/api/openid_connect/certs
//
//    LoginGovConfiguration loginGov = loginGov();
//
//    log.info("\n===loginGovClient===\n");
//    final String LOGIN_GOV_REGISTRATION_ID = "login-gov";
//    final String LOGIN_GOV_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
//    final Long LOGIN_GOV_TOKEN_EXPIRATION_TIME = 3600000L; // 1 hour
//
//    // The Authentication Context Class Reference values used to specify the LOA (level of assurance)
//    // of an account, either LOA1 or LOA3. This and the scope determine which user attributes will be available
//    // in the user info response. The possible parameter values are:
//    final String LOGIN_GOV_LOA1 = "http://idmanagement.gov/ns/assurance/loa/1";
//    final String LOGIN_GOV_LOA3 = "http://idmanagement.gov/ns/assurance/loa/3";
//
//    // Logout Constants
//    final String LOGIN_GOV_LOGOUT_ENDPOINT = "https://idp.int.identitysandbox.gov/openid_connect/logout";
//    final String LOGIN_GOV_LOGOUT_PARAM_ID_TOKEN_HINT = "id_token_hint";
//    final String LOGIN_GOV_LOGOUT_PARAM_POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
//    final String LOGIN_GOV_LOGOUT_PARAM_STATE = "state";
//
//    OidcConfiguration oidcConfiguration = new OidcConfiguration();
//    oidcConfiguration.setClientId(loginGov.clientId);
//    oidcConfiguration.setDiscoveryURI(loginGov.discoveryUri);
//    oidcConfiguration.setScope(loginGov.scopes);
//    oidcConfiguration.setUseNonce(loginGov.useNonce);
//
//    oidcConfiguration.setCustomParams(Map.of("acr_values", "http://idmanagement.gov/ns/assurance/loa/1"));
//    //oidcConfiguration.setCustomParams(Map.of("acr_values", loginGov.acrValues));
//
//    AuthorizationGenerator authGen = (context, profile) -> {
//      // all users coming in through `login.gov` are given `ROLE_PUBLIC`
//      profile.addRole(ROLE_PUBLIC);
//      return Optional.of(profile);
//    };
//
//    final OidcClient<OidcConfiguration> oidcClient = new OidcClient<>(oidcConfiguration);
//
//    oidcClient.addAuthorizationGenerator(authGen);
//    return oidcClient;
//  }
//
//  @Bean
//  Config config() {
//    log.info("\n===configUI===\n");
//
//    OidcClient<OidcConfiguration> loginGovClient = loginGovClient();
//    Clients uiClients = new Clients(loginGovClient);
//
//    final Config config = new Config(uiClients);
//    // "authorizer"s are meant to check authorizations on the authenticated user profile(s) or on the current web context
//    Authorizer<OidcProfile> authorizerPublic = new RequireAnyRoleAuthorizer<>(ROLE_PUBLIC);
//    config.addAuthorizer(AUTHORIZER_PUBLIC, authorizerPublic);
//
//    // "matcher"s defines whether the security must apply on the security filter
//    // - all POST/PUT/PATCH/DELETE endpoints should be secured, and this matcher allows us to intercept them
//    config.addMatcher(MATCHER_HTTP_METHOD_SECURE, new HttpMethodMatcher(
//        HttpConstants.HTTP_METHOD.GET,
//        HttpConstants.HTTP_METHOD.POST,
//        HttpConstants.HTTP_METHOD.PUT,
//        HttpConstants.HTTP_METHOD.PATCH,
//        HttpConstants.HTTP_METHOD.DELETE
//    ));
//
//    return config;
//  }
//
//  @Override
//  public void addInterceptors(InterceptorRegistry registry) {
//
//    log.info("\n===addInterceptors===\n");
//
//    SecurityInterceptor interceptorHttpMethodsSecure = new SecurityInterceptor(config(), LOGIN_GOV_REST_CLIENT, AUTHORIZER_PUBLIC, MATCHER_HTTP_METHOD_SECURE);
//    //SecurityInterceptor interceptorResurrection = new SecurityInterceptor(config(), LOGIN_GOV_REST_CLIENT, AUTHORIZER_PUBLIC);
//
//    // this will intercept and secure all POST/PUT/PATCH/DELETE requests
//    registry.addInterceptor(interceptorHttpMethodsSecure).addPathPatterns("/**/*");
//
//    // because the above interceptor will not catch the GET `/metadata/**/resurrection` endpoints
//    //registry.addInterceptor(interceptorResurrection).addPathPatterns("/metadata/**/resurrection");
//  }
//}