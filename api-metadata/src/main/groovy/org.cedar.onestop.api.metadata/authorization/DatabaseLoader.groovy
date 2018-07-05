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
        Role admin = new Role(role: 'ADMIN')
        Role user = new Role(role: 'USER')

        roleRepository.save(admin)
        roleRepository.save(user)
    }

    private void seedUsers() {
        User bao = new User(email: 'bao@mail.com', uuid: '274b9b15-0bbc-4e93-9c3b-ac2afe54c336')
        User zeb = new User(email: 'zeb@mail.com', uuid: '9ea916eb-7ab6-4a7d-9e76-944635b0c051')
        User elliot = new User(email: 'elliott.richerson@noaa.gov', uuid: '85c631fb-e165-4878-a482-9cd0435aadf3')

        Role admin = roleRepository.findByRole('ADMIN')
        Role user = roleRepository.findByRole('USER')

        bao.roles.addAll(admin, user)
        zeb.roles.add(user)
        elliot.roles.addAll(user)

        userService.saveUser(bao)
        userService.saveUser(zeb)
        userService.saveUser(elliot)
    }
}