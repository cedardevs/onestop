package org.cedar.onestop.api.admin.authorization.domain


import org.springframework.context.annotation.Profile

import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@Profile("icam")
@Entity
@Table(name='user')
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name='user_id')
    Long userId

    @NotEmpty
    @Email
    @Column(name='email')
    String email

    @NotEmpty
    @Column(name='uuid')
    String uuid = UUID.randomUUID()

    @ManyToMany(fetch=FetchType.LAZY, cascade=[ CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
            name='user_roles',
            joinColumns=[@JoinColumn(name='user_id')],
            inverseJoinColumns=[@JoinColumn(name='role_id')]
    )
    List<Role> roles = []
}