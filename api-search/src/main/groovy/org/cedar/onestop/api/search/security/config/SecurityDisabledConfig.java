package org.cedar.onestop.api.search.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Profile("!login-gov")
@Configuration
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