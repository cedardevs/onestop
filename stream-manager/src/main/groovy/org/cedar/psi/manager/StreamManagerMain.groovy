package org.cedar.psi.manager

import groovy.util.logging.Slf4j

@Slf4j
class StreamManagerMain {

  static void main(String[] args) {
    new SpringApplicationBuilder(StreamManagerMain.class)
        .web(WebApplicationType.NONE)
        .run(args)
  }
}
