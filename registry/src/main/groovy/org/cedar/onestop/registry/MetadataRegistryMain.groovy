package org.cedar.onestop.registry

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

import java.util.concurrent.CompletableFuture

@CompileStatic
@SpringBootApplication
class MetadataRegistryMain extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MetadataRegistryMain)
  }

  static void main(String[] args) {
    def context = SpringApplication.run(MetadataRegistryMain.class, args)
    def streamsErrorFuture = context.getBean("streamsErrorFuture", CompletableFuture)
    streamsErrorFuture.thenAcceptAsync({
      SpringApplication.exit(context, {-> 1})
    })
  }

}
