package org.cedar.onestop.api.admin.authorization

import org.cedar.onestop.api.admin.authorization.domain.Role
import org.cedar.onestop.api.admin.authorization.domain.User
import org.cedar.onestop.api.admin.authorization.service.RoleService
import org.cedar.onestop.api.admin.authorization.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import javax.transaction.Transactional

@Profile("icam")
@Component
@Transactional
class UserAndRoleLoader implements ApplicationRunner {
    @Autowired
    private final RoleService roleService

    @Autowired
    private final UserService userService

    @Value('${roles.names:}')
    private String roleNames

    @Value('${user.roles:}')
    private String userRoles

    @Override
    void run(ApplicationArguments args) throws Exception {
        seedRoles()
        seedUsersWithRoles()
    }

    private void seedRoles() {
        List<String> roleList = upcasedRoleNames()
        roleList.each { roleName -> roleService.saveRole(roleName) }
    }

    private void seedUsersWithRoles() {
        Map usersWithRoles = mappedUserRoles()
        usersWithRoles.keySet().each { email ->
            User user = userService.createUser(email)
            String roleName = usersWithRoles[email]
            Role role = roleService.findByRole(roleName)
            Role userRole = roleService.findByRole('USER')
            user.roles.addAll([role, userRole])
            userService.saveUser(user)
        }
    }

    private List<String> upcasedRoleNames(String roles=roleNames) {
        return roles.split(';').collect { roleName -> roleName.toUpperCase() }
    }

    private Map<String,String> mappedUserRoles() {
        Map mapUserRoles = [:]

        def userRoleList = userRoles.split(';')
        userRoleList.each { String userRole ->
            String email = userRole.split(':').first().toLowerCase()
            String roleName = userRole.split(':').last().toUpperCase()

            mapUserRoles[email] = roleName
        }

        return mapUserRoles
    }
}
