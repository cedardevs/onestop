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
    private Long id

    @NotEmpty
    @Column(name='role')
    private String role
//
//    @ManyToMany(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = 'roles')
//    private List<User> users = []
}
