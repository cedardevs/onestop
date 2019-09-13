package org.cedar.onestop.api.admin.authorization.configs

import groovy.util.logging.Slf4j
import org.cedar.onestop.api.admin.authorization.service.UserDetailsServiceImpl
import org.cedar.onestop.api.admin.springsecurity.CustomSecurityFilter
import org.cedar.onestop.api.admin.springsecurity.ICAMConfiguration
import org.cedar.onestop.api.admin.springsecurity.IdentityProviderConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Slf4j
@Profile("icam")
@Configuration
@EnableWebSecurity
class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ICAMConfiguration icamConfiguration

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
            .antMatchers("/metadata-form").hasAuthority('ADMIN')
            .antMatchers("/uploadResponse.html").hasAuthority('ADMIN')
            .antMatchers("/admin/**").hasAuthority('ADMIN')
            .antMatchers("/metadata/**").hasAuthority('ADMIN')
            .antMatchers("/test/permitAll").permitAll()
            .antMatchers("/test/userOnly").hasAuthority('USER')
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(
                new CustomSecurityFilter(authenticationManagerBean(), userDetailsServiceImpl, icamConfiguration, idpConfig),
                UsernamePasswordAuthenticationFilter.class
            )
    }
}