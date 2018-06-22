package org.cedar.onestop.api.metadata.authorization.domains

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Entity
import javax.validation.constraints.NotEmpty

@Entity
@Table(name='user')
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name='id')
    private Long userId

    @NotEmpty
    @Column(name='username')
    private String username

    @NotEmpty
    @Column(name='password')
    private String password
}
