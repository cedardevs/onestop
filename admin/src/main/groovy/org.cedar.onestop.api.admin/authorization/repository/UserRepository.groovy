package org.cedar.onestop.api.admin.authorization.repository

import org.cedar.onestop.api.admin.authorization.domain.User
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository

@Profile("icam")
interface UserRepository extends CrudRepository<User, Long>{
    User findByEmail(String email)
}