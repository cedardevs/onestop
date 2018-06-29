package org.cedar.onestop.api.metadata.authorization.configs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint

import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AuthenticationEntryPoint authEntryPoint

    @Autowired
    DataSource dataSource

    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint(authEntryPoint)
    }

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("\$2a\$04\$41jLXtgaFOKTTZ7QjCT9keMv4hM4j/7KNzKo24oGAzAAUaqIeJd2m").roles("ADMIN")
//        auth.jdbcAuthentication().dataSource(dataSource)
//            .usersByUsernameQuery("SELECT * FROM user WHERE username=?")
//            .authoritiesByUsernameQuery("SELECT * FROM role WHERE role_id=?")
    }
}