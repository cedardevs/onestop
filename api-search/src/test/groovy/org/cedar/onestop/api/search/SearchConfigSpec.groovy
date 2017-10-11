package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.SearchConfig
import spock.lang.Specification


class SearchConfigSpec extends Specification {

  def 'flattenBoosts flattens nested fields into flat field names'() {
    def fieldsConfig = [
        title  : 3,
        contact: [
            individualName  : 2,
            organizationName: null
        ]
    ]
    def flattenedConfig = [
        title                     : 3f,
        'contact.individualName'  : 2f,
        'contact.organizationName': 1f
    ]

    when:
    def config = new SearchConfig(fields: fieldsConfig)
    config.initialize()

    then:
    config.boosts == flattenedConfig
  }

  def 'flattenBoosts fails when config is invalid'() {
    def fieldsConfig = [
        title  : 3,
        contact: [
            individualName  : 2,
            organizationName: "THIS IS NOT A NUMBER"
        ]
    ]

    when:
    def config = new SearchConfig(fields: fieldsConfig)
    config.initialize()

    then:
    thrown(RuntimeException)
  }

}
