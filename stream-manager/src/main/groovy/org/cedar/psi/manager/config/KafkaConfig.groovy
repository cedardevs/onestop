package org.cedar.psi.manager.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix='kafka')
class KafkaConfig {

  @NotBlank
  String bootstrapServers
  private final Application application = new Application()

  Application getApplication() { return application }

  static class Application {
    @NotBlank
    String id
  }
}
