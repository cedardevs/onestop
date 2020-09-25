package org.cedar.onestop.user.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.service.OnestopUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

public class UserInfoOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final ObjectMapper mapper = new ObjectMapper();

  private final WebClient rest = WebClient.create();

  Logger logger = LoggerFactory.getLogger(UserInfoOpaqueTokenIntrospector.class);

  OnestopUserService userService;

  public UserInfoOpaqueTokenIntrospector(OnestopUserService userService){
    this.userService = userService;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    logger.info("OAuth2AuthenticatedPrincipal introspect()");
    logger.info(token);
    return requestUserInfo(token);
  }

  private OAuth2AuthenticatedPrincipal requestUserInfo(String token) {
    logger.info("Requesting OIDC profile");
    String jsonUserInfo = this.rest.get()
        .uri("https://idp.int.identitysandbox.gov/api/openid_connect/userinfo")
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .bodyToMono(String.class)
        .block();

    Map<String, Object> attributes = null;
    try {
      logger.info("Retrieved OIDC profile");
      attributes = mapper.readValue(jsonUserInfo, Map.class);
      logger.info(attributes.toString());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
    String id = attributes.get("sub").toString();
    logger.info("Logging in user: " + attributes.get("sub").toString());
    OnestopUser user = userService.findOrCreateUser(id);
    Map<String, Object> userMap = user.toMap();

    logger.info("Populating security context");
    logger.info(userMap.toString());
    logger.info(user.getPrivilegesAsAuthorities().toString());
    userMap.put("sub", userMap.get("id"));
    OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(userMap, user.getPrivilegesAsAuthorities());

    return principal;
  }

  private Collection<GrantedAuthority> extractAuthorities(Map<String, Object> attributes) {

    String email = (String) attributes.get("email");
    Boolean emailVerified = (Boolean) attributes.getOrDefault("email_verified", false);

    List<String> adminEmails = Arrays.asList(
        "christopher.esterlein@noaa.gov"
    );

    List<String> publicRoles = Arrays.asList("ROLE_PUBLIC");
    List<String> adminRoles = Arrays.asList("ROLE_PUBLIC", "ROLE_ADMIN");

    Boolean isAdmin = (email != null) && adminEmails.contains(email);

    List<String> effectiveRoles = Arrays.asList();

    if(emailVerified) {
      if(isAdmin) {
        effectiveRoles = adminRoles;
      } else {
        effectiveRoles = publicRoles;
      }
    }
    return effectiveRoles.stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}