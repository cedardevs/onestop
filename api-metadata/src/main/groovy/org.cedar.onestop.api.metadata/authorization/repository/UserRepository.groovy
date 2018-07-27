package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository

import java.lang.invoke.MethodHandleImpl

@ConditionalOnProperty("features.secure.authorization")
interface UserRepository extends CrudRepository<User, Long>{
    User findByEmail(String email)
}