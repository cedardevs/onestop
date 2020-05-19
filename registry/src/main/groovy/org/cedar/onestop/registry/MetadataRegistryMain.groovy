package org.cedar.onestop.registry

import groovy.transform.CompileStatic
import org.apache.kafka.streams.KafkaStreams
import org.cedar.onestop.kafka.common.util.KafkaHelpers
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@CompileStatic
@SpringBootApplication
class MetadataRegistryMain extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MetadataRegistryMain)
  }

  static void main(String[] args) {
    def context = SpringApplication.run(MetadataRegistryMain.class, args)
    def streams = context.getBean("streamsApp", KafkaStreams)
    KafkaHelpers.onError(streams).thenAcceptAsync({
      SpringApplication.exit(context, {-> 1})
    })
  }

}
