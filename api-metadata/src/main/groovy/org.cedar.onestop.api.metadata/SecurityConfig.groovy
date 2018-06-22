package org.cedar.onestop.api.metadata

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.savedrequest.NullRequestCache

@EnableWebSecurity(debug = true)
@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityFilter securityFilter

    // disable default to rpevent filter from running twice ?
    @Bean
    FilterRegistrationBean<SecurityFilter> filterRegistrationBean() {
        FilterRegistrationBean<SecurityFilter> filterRegistrationBean = new FilterRegistrationBean<SecurityFilter>()
        filterRegistrationBean.setEnabled(false)
        filterRegistrationBean.setFilter(securityFilter)
        return filterRegistrationBean
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(securityFilter, LogoutFilter.class)
            .authorizeRequests()
                .antMatchers("/admin/**").authenticated()
                .and()
                .authorizeRequests().antMatchers(("/**")).permitAll()
//                .anyRequest().authenticated()
//                .and()
//            .csrf()
//                .disable()
//            .exceptionHandling()
//                .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
//                .and()
//            .requestCache()
//                .requestCache(new NullRequestCache())
//                .and()
//            .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

//        http
//            .authorizeRequests()
//                .antMatchers("/loginConsume", "/logoutConsume").permitAll()
//                .antMatchers("/admin/**").hasRole("METADATA_CURATOR")
//        .and()
//        .addFilterBefore(new SecurityFilter(), UsernamePasswordAuthenticationFilter.class)
    }
}
