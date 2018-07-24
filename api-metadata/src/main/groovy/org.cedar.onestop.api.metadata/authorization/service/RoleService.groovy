package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.cedar.onestop.api.metadata.authorization.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty("features.secure.authorization")
@Service
class RoleService {
    @Autowired
    private final RoleRepository roleRepository

    Role findByRole(String role) {
        roleRepository.findByRoleName(role)
    }

    Role saveRole(String name) {
        Role role = new Role(roleName: name)
        roleRepository.save(role)
    }
}
