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

  final public static String ROLE_PREFIX = "ROLE_";
  //the role and privilege assigned to new users
  final public static String PUBLIC_ROLE = "PUBLIC";
  final public static String READ_USER_PROFILE = "FETCH_USER_PROFILE";
  final public static String CREATE_SAVED_SEARCH = "CREATE_SAVED_SEARCH";
  final public static String READ_SAVED_SEARCH = "READ_SAVED_SEARCH";
  final public static String UPDATE_SAVED_SEARCH = "UPDATE_SAVED_SEARCH";
  final public static String DELETE_SAVED_SEARCH = "DELETE_SAVED_SEARCH";
  final public static List<String> NEW_USER_PRIVILEGES = Arrays.asList(
    READ_USER_PROFILE,
    CREATE_SAVED_SEARCH,
    READ_SAVED_SEARCH,
    DELETE_SAVED_SEARCH
  );

  final public static String[] NEW_USER_PRIVILEGES_ARRAY = SecurityConfig.NEW_USER_PRIVILEGES.toArray(new String[SecurityConfig.NEW_USER_PRIVILEGES.size()]);

  //the role and privileges of the admin user
  final public static String ADMIN_ROLE = "ADMIN";
  final public static String CREATE_USER = "CREATE_USER";
  final public static String UPDATE_USER = "UPDATE_USER";
  final public static String CREATE_ROLE = "CREATE_ROLE";
  final public static String READ_ROLES_BY_USER_ID = "READ_ROLES_BY_USER_ID";
  final public static String DELETE_ROLE = "DELETE_ROLE";

  final public static String CREATE_PRIVILEGE = "CREATE_PRIVILEGE";
  final public static String DELETE_PRIVILEGE = "DELETE_PRIVILEGE";
  final public static String READ_PRIVILEGE_BY_USER_ID = "READ_PRIVILEGE_BY_USER_ID";

  final public static String LIST_SAVED_SEARCH = "LIST_SAVED_SEARCH";
  final public static String READ_SAVED_SEARCH_BY_USER_ID = "READ_SAVED_SEARCH_BY_USER_ID";
  final public static String READ_SAVED_SEARCH_BY_ID = "READ_SAVED_SEARCH_BY_ID";
  final public static String LIST_ALL_SAVED_SEARCHES = "LIST_ALL_SAVED_SEARCHES";
  final public static List<String> ADMIN_PRIVILEGES = Arrays.asList(
    CREATE_USER,
    UPDATE_USER,
    CREATE_ROLE,
    READ_ROLES_BY_USER_ID,
    CREATE_PRIVILEGE,
    READ_PRIVILEGE_BY_USER_ID,
    DELETE_PRIVILEGE,
    LIST_SAVED_SEARCH,
    READ_SAVED_SEARCH_BY_USER_ID,
    READ_SAVED_SEARCH_BY_ID,
    LIST_ALL_SAVED_SEARCHES
  );


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
