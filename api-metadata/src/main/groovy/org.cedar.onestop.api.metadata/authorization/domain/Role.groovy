package org.cedar.onestop.api.metadata.authorization.domain

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotEmpty

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
