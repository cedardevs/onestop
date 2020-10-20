package org.cedar.onestop.user.config;

import org.cedar.onestop.user.service.OnestopUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Arrays;
import java.util.List;

//@Profile("security")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  OnestopUserService userService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .csrf().disable()
        .authorizeRequests()
          .antMatchers("/v2/api-docs")
            .permitAll()
        .and()
        .oauth2ResourceServer()
          .opaqueToken();
//          .jwt()
//      .jwtAuthenticationConverter(jwtAuthenticationConverter());
    ;
  }

  @Bean
  OpaqueTokenIntrospector introspector() {
    return new UserInfoOpaqueTokenIntrospector(userService);
  }

//  JwtAuthenticationConverter jwtAuthenticationConverter() {
//    final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new RoleConverter());
//    return jwtAuthenticationConverter;
//  }
//
//  //As per: https://docs.spring.io/spring-security/site/docs/5.2.x/reference/html5/#oauth2resourceserver-jwt-claimsetmapping-rename
//  class UsernameSubClaimAdapter implements Converter<Map<String, Object>, Map<String, Object>> {
//
//    private final MappedJwtClaimSetConverter delegate = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
//
//    @Override
//    public Map<String, Object> convert(Map<String, Object> claims) {
//      Map<String, Object> convertedClaims = this.delegate.convert(claims);
//      String username = (String) convertedClaims.get("preferred_username");
//      convertedClaims.put("sub", username);
//      return convertedClaims;
//    }
//
//  }
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
