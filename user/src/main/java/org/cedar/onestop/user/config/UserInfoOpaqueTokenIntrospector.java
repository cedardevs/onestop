package org.cedar.onestop.user.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository;
import org.cedar.onestop.user.repository.OnestopRoleRepository;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.cedar.onestop.user.service.OnestopUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

public class UserInfoOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

  private final ObjectMapper mapper = new ObjectMapper();

  private final WebClient rest = WebClient.create();

  Logger logger = LoggerFactory.getLogger(UserInfoOpaqueTokenIntrospector.class);

//  @Autowired
//  OnestopUserService userService;

//  @Autowired
  OnestopUserRepository onestopUserRepo;

//  @Autowired
  private OnestopRoleRepository roleRepository;

//  @Autowired
  private OnestopPrivilegeRepository privilegeRepository;

  public UserInfoOpaqueTokenIntrospector(OnestopUserRepository onestopUserRepo, OnestopRoleRepository roleRepository, OnestopPrivilegeRepository privilegeRepository){
    this.onestopUserRepo = onestopUserRepo;
    this.roleRepository = roleRepository;
    this.privilegeRepository = privilegeRepository;
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
    logger.info(attributes.get("sub").toString());
    String id = attributes.get("sub").toString();
    OnestopUser user = onestopUserRepo.findById(id).orElse(createDefaultUser(id));
    Map<String, Object> userMap = user.toMap();

    logger.info("Populating security context");
    logger.info(userMap.toString());
    logger.info(user.getPrivilegesAsAuthorities().toString());
    userMap.put("sub", userMap.get("id"));
    OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(userMap, user.getPrivilegesAsAuthorities());

    return principal;
  }

  private OnestopUser createDefaultUser(String id){
    String defaultRoleName = "ROLE_" + SecurityConfig.PUBLIC_PRIVILEGE;
    OnestopPrivilege defaultPrivilege = createPrivilegeIfNotFound(defaultRoleName);
    OnestopRole defaultRole = createRoleIfNotFound(defaultRoleName, Arrays.asList(defaultPrivilege));
    OnestopUser defaultUser = new OnestopUser(id, defaultRole);
    return onestopUserRepo.save(defaultUser);
  }

  @Transactional
  OnestopPrivilege createPrivilegeIfNotFound(String name) {
    OnestopPrivilege privilege = privilegeRepository.findByName(name);
    if (privilege == null) {
      privilege = new OnestopPrivilege(UUID.randomUUID().toString(), name);
      privilegeRepository.save(privilege);
    }
    return privilege;
  }

  @Transactional
  OnestopRole createRoleIfNotFound(String name, Collection<OnestopPrivilege> privileges) {
    System.out.println("createRoleIfNotFound");
    logger.info("createRoleIfNotFound");
    OnestopRole role = roleRepository.findByName(name);
    if (role == null) {
      role = new OnestopRole(UUID.randomUUID().toString(), name);
      role.setPrivileges(privileges);
      roleRepository.save(role);
    }
    return role;
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

//  private Collection<GrantedAuthority> extractAuthorities(Map<String, Object> attributes) {
//
//    String id = (String) attributes.get("sub");
//    logger.info("extractAuthorities :: id");
//    logger.info(id);
//    OnestopPrivilege defaultPriv = createPrivilegeIfNotFound(SecurityConfig.PUBLIC_PRIVILEGE);
//    OnestopRole defaultRole = createRoleIfNotFound(SecurityConfig.PUBLIC_PRIVILEGE, Arrays.asList(defaultPriv));
//    OnestopUser user = onestopUserRepo
//      .findById(id)
//      .orElse(
//        new OnestopUser(id, defaultRole)
//      );
//    return user.getRoles().stream()
//      .map(Object::toString)
//      .map(SimpleGrantedAuthority::new)
//      .collect(Collectors.toList());
//  }
//
//  @Transactional
//  OnestopPrivilege createPrivilegeIfNotFound(String name) {
//    OnestopPrivilege privilege = privilegeRepository.findByName(name);
//    if (privilege == null) {
//      privilege = new OnestopPrivilege(name);
//      privilegeRepository.save(privilege);
//    }
//    return privilege;
//  }
//
//  @Transactional
//  OnestopRole createRoleIfNotFound(String name, Collection<OnestopPrivilege> privileges) {
//    System.out.println("createRoleIfNotFound");
//    logger.info("createRoleIfNotFound");
//    OnestopRole role = roleRepository.findByName(name);
//    if (role == null) {
//      role = new OnestopRole(name);
//      role.setPrivileges(privileges);
//      role = roleRepository.save(role);
//    }
//    return role;
//  }
//
}