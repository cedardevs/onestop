package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Slf4j
@Profile('cas')
@Component
@ConfigurationProperties(prefix = 'authorization')
class AuthorizationConfigurationProperties {

  // associate roles (w/out the ROLE_ prefix) to a list of username/principals
  // in `application-cas.yml` (e.g. -  `authorization.roles.ADMIN: ["casuser"]`
  private Map<String, List<String>> roles

  Map<String, List<String>> getRoles() {
    return roles
  }

  void setRoles(Map<String, List<String>> roles) {
    this.roles = roles
  }

  // based on the configured roles and their associated username/principals
  // return a list of roles for a given username/principals
  String[] getAuthorityList(String principal) {
    Set<String> list = []
    roles.each { String role, List<String> principals ->
      if(principals.contains(principal)) {
        list.add("ROLE_" + role)
      }
    }
    return list.toArray(new String[list.size()])
  }
}