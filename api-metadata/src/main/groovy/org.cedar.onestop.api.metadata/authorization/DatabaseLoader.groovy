package org.cedar.onestop.api.metadata.authorization

import org.cedar.onestop.api.metadata.authorization.domain.Role
import org.cedar.onestop.api.metadata.authorization.domain.User
import org.cedar.onestop.api.metadata.authorization.repository.RoleRepository
import org.cedar.onestop.api.metadata.authorization.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DatabaseLoader implements ApplicationRunner {
    @Autowired
    private final UserService userService

    @Autowired
    private final RoleRepository roleRepository

    @Override
    void run(ApplicationArguments args) throws Exception {
        seedRoles()
        seedUsers()
    }

    private void seedRoles() {
        Role admin = new Role(role: 'admin')
        Role user = new Role(role: 'user')

        roleRepository.save(admin)
        roleRepository.save(user)
    }

    private void seedUsers() {
        User bao = new User(username: 'bao', password: 'pass01')
        User zeb = new User(username: 'zeb', password: 'pass02')
        User elliot = new User(username: 'elliot', password: 'pass03')

        Role admin = roleRepository.findByRole('admin')
        Role user = roleRepository.findByRole('user')

        bao.roles.addAll(admin, user)
        zeb.roles.add(user)
        elliot.roles.add(user)

        userService.saveUser(bao)
        userService.saveUser(zeb)
        userService.saveUser(elliot)
    }
}
