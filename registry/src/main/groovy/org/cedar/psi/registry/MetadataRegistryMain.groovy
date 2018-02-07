package org.cedar.psi.registry

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer

@SpringBootApplication
class MetadataRegistryMain extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MetadataRegistryMain)
  }

  static void main(String[] args) {
    SpringApplication.run(MetadataRegistryMain.class, args)
  }

}
