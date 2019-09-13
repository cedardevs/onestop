package org.cedar.onestop.api.admin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
// Default to excluding auto data source configurations based on certain DB classpath dependencies.
// These auto-configurations will be re-included via the following @Profile("icam") based class extensions:
// - DataSourceAutoConfigurationConditional
// - DataSourceTransactionManagerAutoConfigurationConditional
// - HibernateJpaAutoConfigurationConditional
//
// This should prevent a gross looking, but non-fatal stacktrace when ICAM/in-memory DB is not enabled
@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration.class ,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
])
@ComponentScan(["org.cedar.onestop.api.admin", "org.cedar.onestop.elastic.common"])
class Application extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application)
  }

  static void main(String[] args) {
    SpringApplication.run(Application.class, args)
  }

}
