package org.cedar.onestop.api.metadata.authorization.configs

import org.cedar.onestop.api.metadata.authorization.service.UserDetailsServiceImpl
import org.cedar.onestop.api.metadata.springsecurity.CustomSecurityFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity(debug = true)
class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AuthenticationEntryPoint authEntryPoint

    @Autowired
    private UserDetailsServiceImpl userDetailsService

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
            .antMatchers("/test").hasAuthority('USER')
            .antMatchers("/admin/**").hasAuthority('ADMIN')
            .antMatchers("/metadata/**").hasAuthority('ADMIN')
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(new CustomSecurityFilter(authenticationManagerBean(), userDetailsService), UsernamePasswordAuthenticationFilter.class)
    }
}