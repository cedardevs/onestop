package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.cedar.onestop.api.metadata.authorization.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RoleService {
    @Autowired
    private final RoleRepository roleRepository

    Role findByRole(String role) {
        roleRepository.findByRole(role)
    }

    Role saveRole(String roleName) {
        Role role = new Role(role: roleName)
        roleRepository.save(role)
    }
}
