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

  def "All valid fields return expected response from service"() {
    // TODO
  }

  def "Invalidly formatted time fields accurately identified"() {
    // TODO
  }

  def "Invalid access protocols accurately identified"() {
    // TODO
  }

  def "Missing required identifiers detected"() {
    // TODO
  }

  def "Mismatch between metadata type and corresponding identifiers detected"() {
    // TODO
  }

  def "Missing title detected"() {
    // TODO
  }

  def "Missing description detected"() {
    // TODO
  }

  def "Missing thumbnail URL detected"() {
    // TODO
  }
}
