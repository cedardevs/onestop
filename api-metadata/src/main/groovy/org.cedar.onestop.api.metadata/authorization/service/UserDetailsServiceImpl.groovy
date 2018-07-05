package org.cedar.onestop.api.metadata.authorization.service

import org.cedar.onestop.api.metadata.authorization.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private final UserService userService

    @Transactional
    UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException, DataAccessException {

        User user = userService.findByEmail(email)
        if (user == null) { throw new UsernameNotFoundException("User Not Found")}

        return userService.buildUserFromUserEntity(user)
    }
}
