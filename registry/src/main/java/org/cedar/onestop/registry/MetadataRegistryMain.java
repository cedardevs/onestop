package org.cedar.onestop.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class MetadataRegistryMain extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MetadataRegistryMain.class);
  }

  public static void main(String[] args) {
    // start the spring application
    var context = SpringApplication.run(MetadataRegistryMain.class, args);

    // if an error happens in the streams application, shut down the spring application
    var streamsErrorFuture = context.getBean("streamsErrorFuture", CompletableFuture.class);
    streamsErrorFuture.thenAcceptAsync((error) ->
      SpringApplication.exit(context, () -> 1)
    );
  }

}
