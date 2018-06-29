package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.cedar.onestop.api.metadata.authorization.domain.User
import org.cedar.onestop.api.metadata.authorization.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {
    @Autowired
    UserRepository userRepository

    User saveUser(User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder()
        def encodedPassword = encoder.encode(user.password)
        user.password = encodedPassword
        return userRepository.save(user)
    }
}
