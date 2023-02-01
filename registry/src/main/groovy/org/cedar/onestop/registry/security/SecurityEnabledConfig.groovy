package org.cedar.onestop.registry.security

import groovy.util.logging.Slf4j
import org.pac4j.cas.client.rest.CasRestBasicAuthClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.cas.profile.CasRestProfile
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.matching.matcher.HttpMethodMatcher
import org.pac4j.core.profile.UserProfile
import org.pac4j.springframework.web.SecurityInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Slf4j
@Profile('cas')
@Configuration
@ComponentScan(basePackages = "org.pac4j.springframework.web")
class SecurityEnabledConfig implements WebMvcConfigurer {

  static final String ROLE_ADMIN = "ROLE_ADMIN"

  class Authorization {
    private Map<String, List<String>> roles

    Map<String, List<String>> getRoles() {
      return roles
    }

    void setRoles(Map<String, List<String>> roles) {
      this.roles = roles
    }
  }

  @Bean
  @ConfigurationProperties(prefix = 'authorization')
  Authorization authz() {
    return new Authorization()
  }

  // Pac4J Configuration
  @Value('${cas.prefixUrl}')
  private String CAS_PREFIX_URL
  static final String CAS_REST_CLIENT = "CasRestBasicAuthClient"
  static final String AUTHORIZER_ADMIN = "admin"
  static final String MATCHER_HTTP_METHOD_SECURE = "http-methods-secure"

  @Bean
  Config config() {
    CasConfiguration casConfiguration = new CasConfiguration(CAS_PREFIX_URL + "/login", CAS_PREFIX_URL)
    CasRestBasicAuthClient casRestBasicAuthClient = new CasRestBasicAuthClient()
    casRestBasicAuthClient.setConfiguration(casConfiguration)
    casRestBasicAuthClient.setName(CAS_REST_CLIENT)
    AuthorizationGenerator authGen = new AuthorizationGenerator() {
      @Override
      java.util.Optional<UserProfile> generate(WebContext context, UserProfile profile) {
        authz().getRoles().each { String role, List<String> authorizedUsers ->
          if (authorizedUsers.contains(profile.id)) {
            profile.addRole(role)
          }
        }
        return java.util.Optional.of(profile)
      }
    }
    casRestBasicAuthClient.addAuthorizationGenerator(authGen)
    Clients clients = new Clients(casRestBasicAuthClient)
    final Config config = new Config(clients)

    // "authorizer"s are meant to check authorizations on the authenticated user profile(s) or on the current web context
    Authorizer<CasRestProfile> authorizerAdmin = new RequireAnyRoleAuthorizer<>(ROLE_ADMIN)
    config.addAuthorizer(AUTHORIZER_ADMIN, authorizerAdmin)

    // "matcher"s defines whether the security must apply on the security filter
    // - all POST/PUT/PATCH/DELETE endpoints should be secured, and this matcher allows us to intercept them
    config.addMatcher(MATCHER_HTTP_METHOD_SECURE, new HttpMethodMatcher(
        HttpConstants.HTTP_METHOD.POST,
        HttpConstants.HTTP_METHOD.PUT,
        HttpConstants.HTTP_METHOD.PATCH,
        HttpConstants.HTTP_METHOD.DELETE
    ))

    return config
  }

  @Override
  void addInterceptors(InterceptorRegistry registry) {

    SecurityInterceptor interceptorHttpMethodsSecure = new SecurityInterceptor(config(), CAS_REST_CLIENT, AUTHORIZER_ADMIN, MATCHER_HTTP_METHOD_SECURE)
    SecurityInterceptor interceptorResurrection = new SecurityInterceptor(config(), CAS_REST_CLIENT, AUTHORIZER_ADMIN)

    // this will intercept and secure all POST/PUT/PATCH/DELETE requests
    registry.addInterceptor(interceptorHttpMethodsSecure).addPathPatterns("/metadata/**")

    // because the above interceptor will not catch the GET `/metadata/**/resurrection` endpoints
    registry.addInterceptor(interceptorResurrection).addPathPatterns("/metadata/**/resurrection")
  }

}
