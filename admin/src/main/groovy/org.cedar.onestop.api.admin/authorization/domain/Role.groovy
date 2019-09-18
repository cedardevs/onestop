package org.cedar.onestop.api.admin.authorization.domain


import org.springframework.context.annotation.Profile

import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Profile("icam")
@Entity
@Table(name='role')
class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name='role_id')
    Long id

    @NotEmpty
    @Column(name='role_name')
    String roleName

    @ManyToMany(fetch=FetchType.LAZY, mappedBy = 'roles')
    List<User> users = []
}
