package org.cedar.onestop.api.search.security.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    static final String LOGIN_ENDPOINT = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL
    static final String LOGIN_SUCCESS_ENDPOINT = "/login_success"
    static final String LOGIN_FAILURE_ENDPOINT = "/login_failure"
    static final String LOGIN_PROFILE_ENDPOINT = "/login_profile"
    static final String LOGOUT_ENDPOINT = "/logout"
    static final String LOGOUT_SUCCESS_ENDPOINT = "/"

    private KeystoreUtil keystoreUtil
    private String successRedirect

    @Autowired
    SecurityConfig(LoginGovKeystoreConfiguration keystoreConfig, LoginGovSuccessRedirectConfiguration successRedirectConfiguration) {
        keystoreUtil = new KeystoreUtil(
                keystoreConfig.file,
                keystoreConfig.password,
                keystoreConfig.alias,
                null,
                keystoreConfig.type
        )
        successRedirect = successRedirectConfiguration.successRedirect
    }

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        // login, login failure, and index are allowed by anyone
        .antMatchers(LOGIN_ENDPOINT, LOGIN_SUCCESS_ENDPOINT, LOGIN_PROFILE_ENDPOINT, LOGIN_FAILURE_ENDPOINT, "/")
            .permitAll()
        // make sure our public search endpoints are still available and don't request authentication
        .antMatchers(
                "/collection/**",
                "/search/collection/**",
                "/granule/**",
                "/search/granule/**",
                "/flattened-granule/**",
                "/search/flattened-granule/**",
                "/uiConfig",
                "/sitemap/**",
                "/trending/**",
                "/actuator/info"
        )
            .permitAll()
        // any other requests are allowed by an authenticated user
        .anyRequest()
            .authenticated()
            .and()
        // custom logout behavior
        .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_ENDPOINT))
            .logoutSuccessUrl(LOGOUT_SUCCESS_ENDPOINT)
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true)
            .logoutSuccessHandler(new LoginGovLogoutSuccessHandler())
            .and()
        // configure authentication support using an OAuth 2.0 and/or OpenID Connect 1.0 Provider
        .oauth2Login()
            .authorizationEndpoint()
            .authorizationRequestResolver(new LoginGovAuthorizationRequestResolver(clientRegistrationRepository))
            .authorizationRequestRepository(authorizationRequestRepository())
            .and()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient())
            .and()
            .failureUrl(LOGIN_FAILURE_ENDPOINT)
            .successHandler(new LoginGovAuthenticationSuccessHandler()) // .defaultSuccessUrl() wasn't working
    }

    @Bean
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository()
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient()
        accessTokenResponseClient.setRequestEntityConverter(new LoginGovTokenRequestConverter(clientRegistrationRepository, keystoreUtil))
        return accessTokenResponseClient
    }
}
