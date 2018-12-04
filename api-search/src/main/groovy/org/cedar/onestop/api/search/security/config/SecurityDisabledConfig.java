package org.cedar.onestop.api.search.security.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("integration")
@EnableWebSecurity
class SecurityDisabledConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            // allow everything when security is disabled (used for integration tests)
            .antMatchers("/**")
            .permitAll()
            .and()
            .csrf().disable();
    }
}