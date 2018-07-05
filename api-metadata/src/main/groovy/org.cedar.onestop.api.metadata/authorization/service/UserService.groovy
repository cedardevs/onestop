package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.cedar.onestop.api.metadata.authorization.domain.UserDetailsImpl
import org.cedar.onestop.api.metadata.authorization.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class UserService {
    @Autowired
    UserRepository userRepository

    User saveUser(User user) {
        return userRepository.save(user)
    }

    UserDetails buildUserFromUserEntity(User user) {
        List<GrantedAuthority> authorities = []

        user.roles.each { role ->
            authorities.add(new SimpleGrantedAuthority(role.role))
        }
        UserDetails userDetails = new UserDetailsImpl(user, authorities)

        return userDetails
    }
}
