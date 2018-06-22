package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.springframework.data.repository.Repository

interface RoleRepository extends Repository<Role, Long>{
    Role findByRole(String role)
}