package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.pac4j.cas.client.rest.CasRestBasicAuthClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.cas.profile.CasRestProfile
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Slf4j
@Profile('cas')
@Configuration
class Pac4jConfig {

  @Bean
  Config config() {
    CasConfiguration casConfiguration = new CasConfiguration("http://psi-dev-cas:8080/cas/login", "http://psi-dev-cas:8080/cas")
    casConfiguration.setRestUrl("http://psi-dev-cas:8080/cas/v1/tickets")
    final CasRestBasicAuthClient casRestBasicAuthClient = new CasRestBasicAuthClient(casConfiguration, HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX)
    casRestBasicAuthClient.setName("CasRestBasicAuthClient")


    AuthorizationGenerator<CasRestProfile> authGen = new AuthorizationGenerator<CasRestProfile>() {
      @Override
      CasRestProfile generate(WebContext context, CasRestProfile profile) {
        // TODO: get this from config
        boolean isAdmin = Arrays.asList("casuser").contains(profile.id)
        if(isAdmin) {
          profile.addRole("ROLE_ADMIN")
        }
        return profile
      }
    }
    casRestBasicAuthClient.addAuthorizationGenerator(authGen)

    Clients clients = new Clients(casRestBasicAuthClient)

    final Config config = new Config(clients)

    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"))

    return config
  }
}