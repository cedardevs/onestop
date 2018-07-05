package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository

import java.lang.invoke.MethodHandleImpl

interface UserRepository extends CrudRepository<User, Long>{
    // User findByUsername(String username)
    User findByEmail(String email)
}