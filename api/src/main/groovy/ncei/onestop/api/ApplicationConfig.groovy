package ncei.onestop.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true)
class ApplicationConfig extends WebSecurityConfigurerAdapter {

  @Value('${elasticsearch.port}')
  Integer elasticPort

  @Value('${elasticsearch.host}')
  String elasticHost

  @Value('${server.context-path}')
  String contextPath

//  @Value('${security.user.name}')
//  String username
//
//  @Value('${security.user.password}')
//  String password

  @Bean(destroyMethod = 'close')
  public Client client() {
    TransportClient.builder().build()
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticHost), elasticPort))
  }

//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .csrf().disable()
//        .authorizeRequests()
//            .regexMatchers(/\//, /\/search\/?$/).permitAll()
//            .anyRequest().authenticated()
//  }

//  @Autowired
//  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//    auth
//        .inMemoryAuthentication()
//        .withUser(username).password(password).roles("USER")
//        .and()
//        .withUser('anonymous').roles('ANONYMOUS')
//  }

}

