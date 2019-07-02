package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.jasig.cas.client.session.SingleSignOutFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.logout.LogoutFilter

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
@Profile("cas")
@EnableWebSecurity
class SecurityEnabledConfig extends WebSecurityConfigurerAdapter {

  private AuthenticationProvider authenticationProvider
  private AuthenticationEntryPoint authenticationEntryPoint
  private SingleSignOutFilter singleSignOutFilter
  private LogoutFilter logoutFilter
  private CASConfigurationProperties props

  @Autowired
  SecurityEnabledConfig(CasAuthenticationProvider casAuthenticationProvider, AuthenticationEntryPoint eP, LogoutFilter lF, SingleSignOutFilter ssF, CASConfigurationProperties props) {
    this.authenticationProvider = casAuthenticationProvider
    this.authenticationEntryPoint = eP
    this.logoutFilter = lF
    this.singleSignOutFilter = ssF
    this.props = props
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authenticationProvider)
  }

  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return new ProviderManager(Arrays.asList(authenticationProvider))
  }

  @Bean
  CasAuthenticationFilter casAuthenticationFilter(ServiceProperties sP) throws Exception {
    CasAuthenticationFilter filter = new CasAuthenticationFilter()
    filter.setServiceProperties(sP)
    filter.setAuthenticationManager(authenticationManager())
    return filter
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.addFilter(casAuthenticationFilter())

    http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(new AccessDeniedHandler() {
      @Override
      void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Failed : " + accessDeniedException.getMessage())
      }
    })

    http.logout().permitAll().logoutSuccessUrl("/logout")

    http.authenticationProvider(authenticationProvider).authorizeRequests()

      // secured endpoints
      .antMatchers(HttpMethod.POST, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.PUT, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.PATCH, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.DELETE, "/metadata/**").hasRole("ADMIN")
      .antMatchers(HttpMethod.GET, "/metadata/**/resurrection").hasRole("ADMIN")

      .antMatchers("/login/cas","/login").authenticated()

      // everything else is publicly accessible
      .antMatchers("/**").permitAll()
  }
}
