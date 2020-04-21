package org.cedar.onestop.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Profile("security")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .authorizeRequests()
        // permit all applied to endpoints which do not need secured
        .antMatchers("/v2/api-docs").permitAll()
        // any url starting with `/admin` will be restricted to users with role `ROLE_ADMIN`
        .antMatchers("/admin/**").hasRole("ADMIN")
        // any request that has not already been matched requires only authentication
        .anyRequest().authenticated()
        .and()
        .oauth2ResourceServer().jwt();
  }
}

@Profile("!security")
@Configuration
class NoSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        // allow everything when security is disabled (used for integration tests)
        .antMatchers("/**")
        .permitAll()
        .and()
        .csrf().disable();
  }
}
