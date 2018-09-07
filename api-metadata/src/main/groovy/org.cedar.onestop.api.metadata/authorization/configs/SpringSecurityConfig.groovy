package org.cedar.onestop.api.metadata.authorization.configs

import org.cedar.onestop.api.metadata.authorization.service.UserDetailsServiceImpl
import org.cedar.onestop.api.metadata.springsecurity.CustomSecurityFilter
import org.cedar.onestop.api.metadata.springsecurity.IdentityProviderConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@ConditionalOnProperty("features.secure.authorization")
@Configuration
@EnableWebSecurity
class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ServerProperties serverProperties

    @Autowired
    private IdentityProviderConfig idpConfig

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl

    // this is needed to pass the authentication manager into our custom security filter
    @Bean
    @Override
    AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/upload.html").hasAuthority('ADMIN')
            .antMatchers("/admin/**").hasAuthority('ADMIN')
            .antMatchers("/metadata/**").hasAuthority('ADMIN')
            .antMatchers("/test/permitAll").permitAll()
            .antMatchers("/test/userOnly").hasAuthority('USER')
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(
                new CustomSecurityFilter(authenticationManagerBean(), userDetailsServiceImpl, serverProperties, idpConfig),
                UsernamePasswordAuthenticationFilter.class
            )
    }
}