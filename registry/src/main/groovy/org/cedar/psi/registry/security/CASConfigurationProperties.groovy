package org.cedar.psi.registry.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile('cas')
@Component
@ConfigurationProperties(prefix = 'cas')
class CASConfigurationProperties {
	// The callback URL of the service the user is authenticating to
  // e.g. - "https://sciapps.colorado.edu/registry/login/cas"
  String service

  // The CAS server URL prefix
  // e.g. - "https://auth.ncdc.noaa.gov/cas"
  String serverUrlPrefix

  // The enterprise-wide CAS login URL.
  // e.g. - "https://auth.ncdc.noaa.gov/cas/login"
  String loginUrl

  // After logout, a redirect will be performed to this URL
  // e.g. - "https://auth.ncdc.noaa.gov/cas/logout"
  String logoutSuccessUrl

  // A key is required so CasAuthenticationProvider can identify tokens it previously authenticated
  // e.g. - "CAS_PROVIDER_INVENTORY_MANAGER_SCIAPPS"
  //      - "CAS_PROVIDER_INVENTORY_MANAGER_LOCALHOST"
  String providerKey

  // Force CAS to authenticate the user again (even if the user has previously authenticated).
  // During ticket validation it will require the ticket was generated as a consequence of an explicit login.
  // High-security application would probably set this to `true`. Defaults to `false`, providing automated SSO.
  boolean sendRenew

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

  String getLoginUrl() {
    return loginUrl
  }

  void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl
  }

  String getLogoutSuccessUrl() {
    return logoutSuccessUrl
  }

  void setLogoutSuccessUrl(String logoutSuccessUrl) {
    this.logoutSuccessUrl = logoutSuccessUrl
  }

  String getProviderKey() {
    return providerKey
  }

  void setProviderKey(String providerKey) {
    this.providerKey = providerKey
  }

  boolean isSendRenew() {
    return sendRenew
  }

  void setSendRenew(boolean sendRenew) {
    this.sendRenew = sendRenew
  }
}