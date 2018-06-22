package org.cedar.onestop.api.metadata

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/loginConsume", "/logoutConsume").permitAll()
                .antMatchers("/admin/**").hasRole("METADATA_CURATOR")
        .and()
        .addFilterBefore(new SecurityFilter(), UsernamePasswordAuthenticationFilter.class)
    }
}
