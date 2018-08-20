package org.cedar.psi.manager.stream

import spock.lang.Specification

class AnalysisAndValidationServiceSpec extends Specification {

  def "Correct response returned from service"() {
    given:
    def msgToParse = ["testField1":"testValueA", "testField2":123]
    def expectedResponse = ["testField1":"testValueA","testField2":123,"analysis":[:]]

    when:
    def response = AnalysisAndValidationService.analyzeParsedMetadata(msgToParse)

    then:
    response == expectedResponse
  }
}
