package org.cedar.psi.registry.security


import org.jasig.cas.client.session.SingleSignOutFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
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
import org.springframework.security.web.authentication.logout.LogoutFilter

@Profile("cas")
@EnableWebSecurity
class SecurityEnabledConfig extends WebSecurityConfigurerAdapter {

  private AuthenticationProvider authenticationProvider
  private AuthenticationEntryPoint authenticationEntryPoint
  private SingleSignOutFilter singleSignOutFilter
  private LogoutFilter logoutFilter

  @Autowired
  SecurityConfig(CasAuthenticationProvider casAuthenticationProvider, AuthenticationEntryPoint eP, LogoutFilter lF, SingleSignOutFilter ssF) {
    this.authenticationProvider = casAuthenticationProvider
    this.authenticationEntryPoint = eP
    this.logoutFilter = lF
    this.singleSignOutFilter = ssF
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

    http
        .csrf().disable()
        .authorizeRequests()
        // registry secures endpoints primarily based on HTTP verbs since most endpoints start with `/metadata`
        .antMatchers(HttpMethod.POST,"/*").hasAuthority('ADMIN')
        .antMatchers(HttpMethod.PUT,"/*").hasAuthority('ADMIN')
        .antMatchers(HttpMethod.PATCH, "/*").hasAuthority('ADMIN')
        .antMatchers(HttpMethod.DELETE,"/*").hasAuthority('ADMIN')
        .regexMatchers("/secured.*", "/login")
        .authenticated()
        .and()
        .authorizeRequests()
         // if there are any GET endpoints that should be secure, an explicit rule needs to be added above
        .regexMatchers(HttpMethod.GET, "/*")
        .permitAll()
        .and()
        .httpBasic()
        .authenticationEntryPoint(authenticationEntryPoint)
        .and()
        .logout().logoutSuccessUrl("/logout")
        .and()
        .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class)
        .addFilterBefore(logoutFilter, LogoutFilter.class)
  }
}
