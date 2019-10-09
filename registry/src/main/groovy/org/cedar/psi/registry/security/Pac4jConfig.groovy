package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.pac4j.cas.client.rest.CasRestBasicAuthClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants
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
    final CasRestBasicAuthClient casRestBasicAuthClient = new CasRestBasicAuthClient(casConfiguration, HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX)
    casRestBasicAuthClient.setName("CasRestBasicAuthClient")

    Clients clients = new Clients("http://localhost/registry/whatever/dude", casRestBasicAuthClient)
    return new Config(clients)
  }
}