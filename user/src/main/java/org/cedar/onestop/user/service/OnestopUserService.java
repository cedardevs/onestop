package org.cedar.onestop.user.service;

import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository;
import org.cedar.onestop.user.repository.OnestopRoleRepository;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("OnestopUserService")
public class OnestopUserService {
  private static final Logger logger = LoggerFactory.getLogger(OnestopUserService.class);

  private static final String DEFAULT_ROLE_NAME = AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.PUBLIC_ROLE;
  private static final String ADMIN_ROLE_NAME = AuthorizationConfiguration.ROLE_PREFIX + AuthorizationConfiguration.ADMIN_ROLE;

  public final OnestopUserRepository userRepository;
  public final OnestopRoleRepository roleRepository;
  public final OnestopPrivilegeRepository privilegeRepository;

  private boolean initialized = false;

  @Autowired
  public OnestopUserService(
      OnestopUserRepository userRepository,
      OnestopRoleRepository roleRepository,
      OnestopPrivilegeRepository privilegeRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.privilegeRepository = privilegeRepository;
  }

  @PostConstruct
  public void ensureDefaults() {
    if (!initialized) {
      createRoleIfNotFound(DEFAULT_ROLE_NAME, createPrivilegesIfNotFound(AuthorizationConfiguration.NEW_USER_PRIVILEGES));
      createRoleIfNotFound(ADMIN_ROLE_NAME, createPrivilegesIfNotFound(AuthorizationConfiguration.ADMIN_PRIVILEGES));
      initialized = true;
    }
  }

  public Page<OnestopUser> findAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  public Optional<OnestopUser> findById(String id) {
    return userRepository.findById(id);
  }

  public boolean exists(String id) {
    return userRepository.existsById(id);
  }

  public OnestopUser save(OnestopUser user) {
    return userRepository.save(user);
  }

  public Page<OnestopRole> findRolesByUserId(String id, Pageable pageable) {
    return roleRepository.findByUsersId(id, pageable);
  }

  public Page<OnestopPrivilege> findPrivilegesByUserId(String id, Pageable pageable) {
    return privilegeRepository.findByRolesUsersId(id, pageable);
  }

  public OnestopUser findOrCreateUser(String id) {
    return findOrCreateUser(id, false);
  }

  public OnestopUser findOrCreateUser(String id, boolean isAdmin) {
    return userRepository
        .findById(id)
        .orElseGet(() -> createDefaultUser(id, isAdmin));
  }

  public OnestopUser createDefaultUser(String id, boolean isAdmin) {
    var roleName = isAdmin ? ADMIN_ROLE_NAME : DEFAULT_ROLE_NAME;
    logger.info("Creating admin user with id: " + id + " and role " + roleName);
    var role = roleRepository.findByName(roleName)
        .orElseThrow(() -> new IllegalStateException("Default role " + roleName + " has not been created"));
    return createUserWithRole(id, role);
  }

  private OnestopUser createUserWithRole(String id, OnestopRole role) {
    if (id == null) {
      return null;
    }
    OnestopUser defaultUser = new OnestopUser(id, role);
    logger.info(defaultUser.toString());
    return userRepository.save(defaultUser);
  }

  public List<OnestopPrivilege> createPrivilegesIfNotFound(List<String> names) {
    return names.stream()
        .map(name -> privilegeRepository.findOneByName(name).orElseGet(() -> createPrivilege(name)))
        .collect(Collectors.toList());
  }

  public OnestopPrivilege createPrivilege(String name) {
    OnestopPrivilege privilege = new OnestopPrivilege(UUID.randomUUID().toString(), name);
    logger.info("Creating privilege: " + privilege.getName());
    return privilegeRepository.saveAndFlush(privilege);
  }

  public OnestopRole createRoleIfNotFound(String name, List<OnestopPrivilege> privileges) {
    return roleRepository.findByName(name).orElseGet(() -> createRole(name, privileges));
  }

  public OnestopRole createRole(String name, List<OnestopPrivilege> privileges) {
    OnestopRole role = new OnestopRole(UUID.randomUUID().toString(), name);
    role.setPrivileges(privileges);
    logger.info("Creating role: " + role.getName());
    return roleRepository.saveAndFlush(role);
  }
}
