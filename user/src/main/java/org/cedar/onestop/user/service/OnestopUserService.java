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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

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

    public OnestopUser findOrCreateUser(String id){
        logger.info("findOrCreateUser");
        logger.info(id);
        return onestopUserRepository.findById(id).orElse(createDefaultUser(id));
    }

    public OnestopUser createDefaultUser(String id){
        String defaultRoleName = "ROLE_" + SecurityConfig.PUBLIC_PRIVILEGE;
        OnestopPrivilege defaultPrivilege = createPrivilegeIfNotFound(defaultRoleName);
        OnestopRole defaultRole = createRoleIfNotFound(defaultRoleName, Arrays.asList(defaultPrivilege));
        logger.info("createDefaultUser");
        logger.info(id);
        OnestopUser defaultUser = new OnestopUser(id);
        defaultUser.setRoles(Arrays.asList(defaultRole));
        logger.info(defaultUser.toString());
        logger.info(defaultUser.getId());
        if(defaultUser.getId() == null)
            return null;
        return onestopUserRepository.save(defaultUser);
    }

//    @Transactional
    public OnestopPrivilege createPrivilegeIfNotFound(String name) {
        logger.info("createPrivilegeIfNotFound");
        OnestopPrivilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new OnestopPrivilege(UUID.randomUUID().toString(), name);
            privilege = privilegeRepository.saveAndFlush(privilege);
        }
        return privilege;
    }

//    @Transactional
    public OnestopRole createRoleIfNotFound(String name, Collection<OnestopPrivilege> privileges) {
        logger.info("createRoleIfNotFound");
        OnestopRole role = roleRepository.findByName(name);
        if (role == null) {
            role = new OnestopRole(UUID.randomUUID().toString(), name);
            role.setPrivileges(privileges);
            role = roleRepository.saveAndFlush(role);
        }
        return role;
    }

//    @Transactional
    public OnestopUser getOrCreateDefaultUser(String id){
        OnestopRole defaultRole = new OnestopRole(SecurityConfig.PUBLIC_PRIVILEGE);
        OnestopPrivilege defaultPriv = new OnestopPrivilege(SecurityConfig.PUBLIC_PRIVILEGE);
        defaultRole.setPrivileges(Arrays.asList(defaultPriv));
        Collection<OnestopRole> publicRoles = Arrays.asList(defaultRole);
        OnestopUser user = null;
        return onestopUserRepository.findById(id).orElse(onestopUserRepository.save(new OnestopUser(id, publicRoles)));
    }
}
