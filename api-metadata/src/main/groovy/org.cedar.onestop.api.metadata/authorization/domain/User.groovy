package org.cedar.onestop.api.metadata.authorization.domain

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.persistence.Entity
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@ConditionalOnProperty("features.secure.authorization")
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