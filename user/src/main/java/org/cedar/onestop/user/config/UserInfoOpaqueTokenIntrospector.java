package org.cedar.onestop.user.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserInfoOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final ObjectMapper mapper = new ObjectMapper();

  private final WebClient rest = WebClient.create();

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    return requestUserInfo(token);
  }

  private OAuth2AuthenticatedPrincipal requestUserInfo(String token) {
    String jsonUserInfo = this.rest.get()
        .uri("https://idp.int.identitysandbox.gov/api/openid_connect/userinfo")
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .bodyToMono(String.class)
        .block();

    Map<String, Object> attributes = null;
    try {
      attributes = mapper.readValue(jsonUserInfo, Map.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }

    OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributes, extractAuthorities(attributes));

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