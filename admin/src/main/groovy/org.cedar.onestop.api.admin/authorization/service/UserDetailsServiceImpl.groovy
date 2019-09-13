package org.cedar.onestop.api.admin.authorization.service

import org.cedar.onestop.api.admin.authorization.domain.Role
import org.cedar.onestop.api.admin.authorization.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Profile("icam")
@Service
class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private final UserService userService

    @Autowired
    private final RoleService roleService

    @Transactional
    UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException, DataAccessException {

        User user = userService.findByEmail(email)
        if (user == null) {
            user = createUserWithUserRole(email)
        }

        return userService.buildUserFromUserEntity(user)
    }

    private User createUserWithUserRole(String email) {
        User user = userService.createUser(email)
        Role userRole = roleService.findByRole('USER')
        user.roles.add(userRole)

        return userService.saveUser(user)
    }
}
