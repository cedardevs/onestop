package org.cedar.psi.registry.security


import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotNull

@Validated
class CASConfigurationProperties {

	// The callback URL of the service the user is authenticating to
  // e.g. - "https://sciapps.colorado.edu/registry/login/cas"
  @NotNull
  String service

  // The CAS server URL prefix
  // e.g. - "https://auth.ncdc.noaa.gov/cas"
  @NotNull
  String serverUrlPrefix


  // getters and setters
  String getService() {
    return service
  }

  void setService(String service) {
    this.service = service
  }

  String getServerUrlPrefix() {
    return serverUrlPrefix
  }

  void setServerUrlPrefix(String serverUrlPrefix) {
    this.serverUrlPrefix = serverUrlPrefix
  }
}