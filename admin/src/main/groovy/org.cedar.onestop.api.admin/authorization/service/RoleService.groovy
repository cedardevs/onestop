package org.cedar.onestop.api.admin.authorization.service

import org.cedar.onestop.api.admin.authorization.domain.Role
import org.cedar.onestop.api.admin.authorization.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("icam")
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
