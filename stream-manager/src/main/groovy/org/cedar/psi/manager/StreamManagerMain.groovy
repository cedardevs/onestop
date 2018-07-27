package org.cedar.psi.manager

import groovy.transform.CompileStatic
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@CompileStatic
@SpringBootApplication
class StreamManagerMain extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(StreamManagerMain)
    }

    static void main(String[] args) {
        SpringApplication.run(StreamManagerMain.class, args)
    }
}
