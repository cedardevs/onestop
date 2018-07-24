package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.cedar.onestop.api.metadata.authorization.domain.UserDetailsImpl
import org.cedar.onestop.api.metadata.authorization.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@ConditionalOnProperty("features.secure.authorization")
@Service
class UserService {
    @Autowired
    private final UserRepository userRepository

    User saveUser(User user) {
        return userRepository.save(user)
    }

    User createUser(String email) {
        User user = new User(email: sanitize(email))
        userRepository.save(user)
    }

    User findByEmail(String email) {
        return userRepository.findByEmail(sanitize(email))
    }

    UserDetails buildUserFromUserEntity(User user) {
        List<GrantedAuthority> authorities = []

        user.roles.each { role ->
            authorities.add(new SimpleGrantedAuthority(role.roleName))
        }
        UserDetails userDetails = new UserDetailsImpl(user, authorities)

        return userDetails
    }

    private String sanitize(String email) {
        return email.trim().toLowerCase()
    }
}
