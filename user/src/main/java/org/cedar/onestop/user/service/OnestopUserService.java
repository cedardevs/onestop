package org.cedar.onestop.user.service;

import org.cedar.onestop.user.config.AuthorizationConfiguration;
import org.cedar.onestop.user.config.SecurityConfig;
import org.cedar.onestop.user.domain.OnestopPrivilege;
import org.cedar.onestop.user.domain.OnestopRole;
import org.cedar.onestop.user.domain.OnestopUser;
import org.cedar.onestop.user.repository.OnestopPrivilegeRepository;
import org.cedar.onestop.user.repository.OnestopRoleRepository;
import org.cedar.onestop.user.repository.OnestopUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("OnestopUserService")
public class OnestopUserService {

    Logger logger = LoggerFactory.getLogger(OnestopUserService.class);

    public final OnestopUserRepository userRepository;

    public final OnestopRoleRepository roleRepository;

    public final OnestopPrivilegeRepository privilegeRepository;

    @Autowired
    public OnestopUserService(OnestopUserRepository userRepository, OnestopRoleRepository roleRepository, OnestopPrivilegeRepository privilegeRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    public OnestopUser findOrCreateUser(String id) {
        return userRepository
          .findById(id)
          .orElse(createDefaultUser(id));
    }

    public OnestopUser findOrCreateAdminUser(String id) {
        return userRepository
          .findById(id)
          .orElse(createAdminUser(id));
    }

    public OnestopUser createAdminUser(String id){
        logger.info("Creating admin user with id: " + id);
        String adminRoleName = "ROLE_" + AuthorizationConfiguration.ADMIN_ROLE;
        List<OnestopPrivilege> adminPrivileges = createAdminPrivilegesIfNotFound();
        OnestopRole defaultRole = createRoleIfNotFound(adminRoleName, adminPrivileges);
        OnestopUser defaultUser = new OnestopUser(id);
        defaultUser.setRoles(Arrays.asList(defaultRole));
        logger.info(defaultUser.toString());
        logger.info(defaultUser.getId());
        if(defaultUser.getId() == null)
            return null;
        return userRepository.save(defaultUser);
    }

    public OnestopUser createDefaultUser(String id){
        logger.info("Creating new user with id: " + id);
        String defaultRoleName = "ROLE_" + AuthorizationConfiguration.PUBLIC_ROLE;
        List<OnestopPrivilege> defaultPrivileges = createNewUserPrivilegesIfNotFound();
        OnestopRole defaultRole = createRoleIfNotFound(defaultRoleName, defaultPrivileges);
        OnestopUser defaultUser = new OnestopUser(id);
        defaultUser.setRoles(Arrays.asList(defaultRole));
        logger.info(defaultUser.toString());
        logger.info(defaultUser.getId());
        if(defaultUser.getId() == null)
            return null;
        return userRepository.save(defaultUser);
    }

    public List<OnestopPrivilege> createAdminPrivilegesIfNotFound() {
        return AuthorizationConfiguration.ADMIN_PRIVILEGES
          .stream()
          .map(String::toString)
          .map(name -> privilegeRepository.findByName(name).orElse(createPrivilege(name)))
          .collect(Collectors.toList());
    }

    public List<OnestopPrivilege> createNewUserPrivilegesIfNotFound() {
        return AuthorizationConfiguration.NEW_USER_PRIVILEGES
          .stream()
          .map(String::toString)
          .map(name -> privilegeRepository.findByName(name).orElse(createPrivilege(name)))
          .collect(Collectors.toList());
    }

    public OnestopPrivilege createPrivilege(String name){
        OnestopPrivilege privilege = new OnestopPrivilege(UUID.randomUUID().toString(), name);
        logger.info("Creating privilege: " + privilege.getName());
        return privilegeRepository.saveAndFlush(privilege);
    }

    public OnestopRole createRoleIfNotFound(String name, List<OnestopPrivilege> privileges) {
        return roleRepository.findByName(name).orElse(createRole(name, privileges));
    }

    public OnestopRole createRole(String name, List<OnestopPrivilege> privileges){
        OnestopRole role = new OnestopRole(UUID.randomUUID().toString(), name);
        role.setPrivileges(privileges);
        logger.info("Creating role: " + role.getName());
        return roleRepository.saveAndFlush(role);
    }
}
