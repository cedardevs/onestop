package org.cedar.onestop.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

//@Profile("security")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//  @Value("${spring.security.oauth2.resource.jwt.issuer-uri}")
//  private String issuer;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .csrf()
            .ignoringAntMatchers("/v1/user", "/v1/saved-search")
        .and()
        .authorizeRequests()
          .antMatchers("/v2/api-docs")
            .permitAll()
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

//@Profile("!security")
//@Configuration
//class NoSecurityConfig extends WebSecurityConfigurerAdapter {
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .authorizeRequests()
//        // allow everything when security is disabled (used for integration tests)
//        .antMatchers("/**")
//        .permitAll()
//        .and()
//        .csrf().disable();
//  }
//}
