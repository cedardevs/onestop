package org.cedar.onestop.api.admin.authorization.domain


import org.springframework.context.annotation.Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Profile("icam")
class UserDetailsImpl implements UserDetails{
    String username
    String password
    boolean accountNonExpired
    boolean accountNonLocked
    boolean credentialsNonExpired
    boolean enabled
    List<GrantedAuthority> authorities

    UserDetailsImpl(User user, List<GrantedAuthority> auths) {
        this.username = user.email
        this.password = ''
        this.authorities = auths
        this.accountNonExpired = true
        this.accountNonLocked = true
        this.credentialsNonExpired = true
        this.enabled = true
    }
}
