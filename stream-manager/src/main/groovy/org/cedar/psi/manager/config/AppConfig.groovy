package org.cedar.psi.manager.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix='stream')
class AppConfig {

  @NotBlank
  String splitField
  List<String> splitValues
  private final Topics topics = new Topics()

  void setSplitValues(String valuesString) {
    List<String> values = new ArrayList<>()
    values.addAll(valuesString.split(','))
  }

  Topics getTopics() { return topics }

  static class Topics {
    @NotBlank
    String rawGranules

    @NotBlank
    String unparsedGranules

    @NotBlank
    String parsedGranules

    @NotBlank
    String smeGranules

    @NotBlank
    String errorGranules
  }
}
