package org.cedar.psi.registry.security

import groovy.util.logging.Slf4j
import org.pac4j.core.config.Config
import org.pac4j.springframework.annotation.CommonAspect
import org.pac4j.springframework.web.SecurityInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Slf4j
@Profile('cas')
@Configuration
@ComponentScan(basePackages = "org.pac4j.springframework.web")
class SecurityEnabledConfig implements WebMvcConfigurer {

  @Autowired
  private Config config

  @Override
  void addInterceptors(InterceptorRegistry registry) {
    SecurityInterceptor interceptor = new SecurityInterceptor(config, "CasRestBasicAuthClient", "admin")
    registry.addInterceptor(interceptor).addPathPatterns("/*")
  }

}