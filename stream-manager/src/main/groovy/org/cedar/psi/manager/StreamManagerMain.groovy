package org.cedar.psi.manager

import groovy.transform.CompileStatic
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@CompileStatic
@SpringBootApplication
class StreamManagerMain {

  static void main(String[] args) {
    new SpringApplicationBuilder(StreamManagerMain.class)
        .web(WebApplicationType.NONE)
        .run(args)
  }
}
