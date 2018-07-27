package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository

@ConditionalOnProperty("features.secure.authorization")
interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByRoleName(String name)
}