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
  private Map<String, List<String>> roles

  Map<String, List<String>> getRoles() {
    return roles
  }

  void setRoles(Map<String, List<String>> roles) {
    this.roles = roles
  }

  String[] getAuthorityList(String principal) {
    Set<String> list = []
    log.info("principal = ${principal}\n------")
    roles.each { String role, List<String> principals ->
      log.info("role: ${role}, principals: ${principals.dump()}")
      if(principals.contains(principal)) {
        list.add("ROLE_" + role)
      }
    }
    return list.toArray(new String[list.size()])
  }
}