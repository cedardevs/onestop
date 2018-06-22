package org.cedar.onestop.api.metadata.authorization.repository

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository

interface UserRepository extends Repository<User, Long>{
    User findByUsername(String username)
}