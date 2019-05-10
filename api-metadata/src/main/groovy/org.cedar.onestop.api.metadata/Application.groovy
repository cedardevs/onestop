package org.cedar.onestop.api.metadata

import org.springframework.boot.SpringApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
// note: no @SpringApplication annotation here
// see "Reference:" link in `DatabaseConfig`
@Import(DatasourceConfig.class)
class Application extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application)
  }

  static void main(String[] args) {
    SpringApplication.run(Application.class, args)
  }

}
