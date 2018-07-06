package org.cedar.onestop.api.metadata.authorization.service

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.cedar.onestop.api.metadata.authorization.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.dao.DataAccessException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
@Configuration
@PropertySource("roles.yml")
@Slf4j
class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private final UserService userService

    @Autowired
    private final RoleService roleService

    @Value('${user.roles:}')
    private String userRoles

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

        Map adminUsers = mappedUserRoles()
        log.info("adminUsers: ${adminUsers[user.email]}")
        if (adminUsers[user.email]) {
            Role addRole = new Role(role: adminUsers[user.email])
            log.info("role added: ${addRole.role}")
            user.roles.add(addRole)
        }

        return userService.saveUser(user)
    }

    private Map<String,String> mappedUserRoles() {
        Map mapUserRoles = [:]

        def userRoleList = userRoles.split(',')
        userRoleList.each { String userRole ->
            String email = userRole.split(':').first()
            String roleName = userRole.split(':').last()

            mapUserRoles[email] = roleName
        }

        return mapUserRoles
    }
}
