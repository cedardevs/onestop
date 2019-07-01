package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.jasig.cas.client.session.SingleSignOutFilter
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator
import org.jasig.cas.client.validation.TicketValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

import javax.servlet.http.HttpSessionEvent

@Slf4j
@Profile('cas')
@Configuration
class CASConfiguration {

  /*

  Authentication Flow:
  -------------------

  1. A user attempts to access a secured page

  2. The AuthenticationEntryPoint is triggered and takes the user to the server.
     The login address of the server has been specified in the AuthenticationEntryPoint.

  3. On a successful authentication with the server, it redirects the request back to the service URL
     that has been specified, with the service ticket appended as a query parameter.

  4. CasAuthenticationFilter is mapped to a URL that matches the pattern and in turn, triggers the
     ticket validation internally. If the ticket is valid, a user will be redirected to the originally requested URL.

   */

  @Autowired
  CASConfigurationProperties props

  @Autowired
  AuthorizationConfigurationProperties authz

  @Bean
  ServiceProperties serviceProperties() {
    ServiceProperties serviceProperties = new ServiceProperties()
    serviceProperties.setService(props.getService())
    serviceProperties.setSendRenew(props.isSendRenew())
    return serviceProperties
  }

  @Bean
  @Primary
  AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties sP) {
    CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint()
    entryPoint.setLoginUrl(props.getLoginUrl())
    entryPoint.setServiceProperties(sP)
    return entryPoint
  }

  @Bean
  TicketValidator ticketValidator() {
    return new Cas30ServiceTicketValidator(
        props.getServerUrlPrefix())
  }

  @Bean
  CasAuthenticationProvider casAuthenticationProvider() {
    CasAuthenticationProvider provider = new CasAuthenticationProvider()
    UserDetailsService userDetailsService = new UserDetailsService() {
      @Override
      UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] authorityList = authz.getAuthorityList(username)
        if(authorityList == null || authorityList.length == 0) {
          throw new UsernameNotFoundException("User name not found.")
        }
        else {
          return new User(username, "", AuthorityUtils.createAuthorityList(authorityList))
        }
      }
    }
    provider.setServiceProperties(serviceProperties())
    provider.setTicketValidator(ticketValidator())
    provider.setUserDetailsService(userDetailsService)
    provider.setKey(props.getProviderKey())
    return provider
  }

  // single logout
  @Bean
  SecurityContextLogoutHandler securityContextLogoutHandler() {
    return new SecurityContextLogoutHandler()
  }

  @Bean
  LogoutFilter logoutFilter() {
    LogoutFilter logoutFilter = new LogoutFilter(
        props.getLogoutSuccessUrl(),
        securityContextLogoutHandler())
    logoutFilter.setFilterProcessesUrl("/logout/cas")
    return logoutFilter
  }

  @Bean
  SingleSignOutFilter singleSignOutFilter() {
    SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter()
    singleSignOutFilter.setCasServerUrlPrefix(props.getServerUrlPrefix())
    singleSignOutFilter.setIgnoreInitConfiguration(true)
    return singleSignOutFilter
  }

  @EventListener
  SingleSignOutHttpSessionListener singleSignOutHttpSessionListener(
      HttpSessionEvent event) {
    return new SingleSignOutHttpSessionListener()
  }

}
