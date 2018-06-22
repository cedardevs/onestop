package org.cedar.onestop.api.metadata

import org.cedar.onestop.api.metadata.security.SAMLFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {

    

    @Bean
    SecurityFilter customSecurityFilter() throws Exception {
        SecurityFilter securityFilter = new SecurityFilter()
        securityFilter
    }

//    @Bean
//    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter()
//            throws Exception {
//        CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter = new CustomUsernamePasswordAuthenticationFilter();
//        customUsernamePasswordAuthenticationFilter
//                .setAuthenticationManager(authenticationManagerBean());
//        customUsernamePasswordAuthenticationFilter
//                .setAuthenticationSuccessHandler(customSuccessHandler());
//        return customUsernamePasswordAuthenticationFilter;
//    }
//
//    @Bean
//    public CustomSuccessHandler customSuccessHandler() {
//        CustomSuccessHandler customSuccessHandler = new CustomSuccessHandler();
//        return customSuccessHandler;
//    }
//
//    @Bean
//    public CustomAuthenticationProvider customAuthenticationProvider() {
//        CustomAuthenticationProvider customAuthenticationProvider = new CustomAuthenticationProvider();
//        return customAuthenticationProvider;
//    }
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        List<AuthenticationProvider> authenticationProviderList = new ArrayList<AuthenticationProvider>();
//        authenticationProviderList.add(customAuthenticationProvider());
//        AuthenticationManager authenticationManager = new ProviderManager(
//                authenticationProviderList);
//        return authenticationManager;
//    }




    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http
//            .authorizeRequests()
//                .antMatchers("/loginConsume", "/logoutConsume").permitAll()
//                .antMatchers("/admin/**").hasRole("METADATA_CURATOR")
//        .and()
//        .addFilterBefore(new SecurityFilter(), UsernamePasswordAuthenticationFilter.class)

        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("METADATA_CURATOR")
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .addFilter(customUsernamePasswordAuthenticationFilter());
    }
}
