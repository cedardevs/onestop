package org.cedar.onestop.user.service;

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
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("OnestopUserService")
public class OnestopUserService {

    Logger logger = LoggerFactory.getLogger(OnestopUserService.class);

    @Autowired
    private OnestopUserRepository onestopUserRepository;

    @Autowired
    private OnestopRoleRepository roleRepository;

    @Autowired
    private OnestopPrivilegeRepository privilegeRepository;

    public OnestopUserService(){}

    public OnestopUser findOrCreateUser(String id) {
        return onestopUserRepository
          .findById(id)
          .orElse(createDefaultUser(id));
    }

    public OnestopUser createDefaultUser(String id){
        logger.info("Creating new user with id: " + id);
        String defaultRoleName = "ROLE_" + SecurityConfig.PUBLIC_ROLE;
        Collection<OnestopPrivilege> defaultPrivileges = createPrivilegesIfNotFound();
        OnestopRole defaultRole = createRoleIfNotFound(defaultRoleName, defaultPrivileges);
        OnestopUser defaultUser = new OnestopUser(id);
        defaultUser.setRoles(Arrays.asList(defaultRole));
        logger.info(defaultUser.toString());
        logger.info(defaultUser.getId());
        if(defaultUser.getId() == null)
            return null;
        return onestopUserRepository.save(defaultUser);
    }

    public Collection<OnestopPrivilege> createPrivilegesIfNotFound() {
        return SecurityConfig.NEW_USER_PRIVILEGES
          .stream()
          .map(String::toString)
          .map(name -> privilegeRepository.findByName(name).orElse(createPrivilege(name)))
          .collect(Collectors.toSet());
    }

    public OnestopPrivilege createPrivilege(String name){
        OnestopPrivilege privilege = new OnestopPrivilege(UUID.randomUUID().toString(), name);
        logger.info("Creating privilege: " + privilege.getName());
        return privilegeRepository.saveAndFlush(privilege);
    }

    public OnestopRole createRoleIfNotFound(String name, Collection<OnestopPrivilege> privileges) {
        return roleRepository.findByName(name).orElse(createRole(name, privileges));
    }

    public OnestopRole createRole(String name, Collection<OnestopPrivilege> privileges){
        OnestopRole role = new OnestopRole(UUID.randomUUID().toString(), name);
        role.setPrivileges(privileges);
        logger.info("Creating role: " + role.getName());
        return roleRepository.saveAndFlush(role);
    }
}
