package org.cedar.onestop.api.metadata.authorization.domain

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
import javax.validation.constraints.NotEmpty

@Entity
@Table(name='user')
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name='user_id')
    private Long userId

    @NotEmpty
    @Column(name='username')
    private String username

    @NotEmpty
    @Column(name='password')
    private String password
//
//    @ManyToMany(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
//    @JoinTable(
//            name='user_roles',
//            joinColumns=[@JoinColumn(name='user_id')],
//            inverseJoinColumns=[@JoinColumn(name='role_id')]
//    )
//    private List<Role> roles = []
}