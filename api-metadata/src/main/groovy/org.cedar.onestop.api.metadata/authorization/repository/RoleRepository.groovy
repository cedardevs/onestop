package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository

@Profile("icam")
interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByRoleName(String name)
}