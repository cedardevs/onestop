package org.cedar.onestop.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Profile("security")
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//  @Value("${spring.security.oauth2.resource.jwt.issuer-uri}")
//  private String issuer;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
          // permit all applied to endpoints which do not need secured
          .antMatchers("/v2/api-docs")
            .permitAll()
          // any url starting with `/admin` will be restricted to users with role `ROLE_ADMIN`
          .antMatchers("/admin/**")
            .hasRole("ADMIN")
          //getting user data with id as a path param is an admin operation
          .antMatchers("/v1/user/**")
            .hasRole(("ADMIN"))
          //use auth principal name to get user without path param
          .antMatchers("/v1/user")
            .hasRole(("PUBLIC"))
          // any url starting with `/v1/saved-search` will be restricted to users with role `ROLE_PUBLIC`
          .antMatchers("/v1/saved-search/**")
            .hasRole(("PUBLIC"))
          .antMatchers("/v1/saved-search/user")
            .hasRole(("PUBLIC"))
          .antMatchers("/v1/saved-search/user/**")
            .hasRole(("ADMIN"))
          // any request that has not already been matched requires only authentication
          .anyRequest()
            .authenticated()
        .and()
        .oauth2ResourceServer()
          .opaqueToken();
//          .jwt();
  }

  @Bean
  OpaqueTokenIntrospector introspector() {
    return new UserInfoOpaqueTokenIntrospector();
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
