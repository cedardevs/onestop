package org.cedar.onestop.api.metadata.springsecurity

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity(debug = true)
@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

    // this is needed to pass the authentication manager into our custom security filter
    @Bean
    @Override
    AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
//            .authorizeRequests()
//                .antMatchers("/admin/**").hasRole("METADATA_CURATOR")
//            .and()
            .addFilterBefore(new CustomSecurityFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class)
    }

}