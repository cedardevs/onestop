package org.cedar.onestop.kafka.common.util

import org.cedar.schemas.avro.psi.InputEvent
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DataUtilsSpec extends Specification {

  // Note: Most behavior of updateDerivedFields actually tested in registry StreamFunctionsSpec

  def "updateDerivedFields throws up if given an unrecognized builder type"() {
    def fieldData = [
        rawJson: '{"hello":"world","list":[1,2]}'
    ]
    def fieldsToParse = ['rawJson']

    when:
    DataUtils.updateDerivedFields(InputEvent.newBuilder(), fieldData, fieldsToParse)

    then:
    thrown(ClassCastException)
  }

  def "setValueOnPojo... does that"() {
    def pojo = new TestPojo()
    def fieldName = 'testField'
    def value = 'super great test string'

    when:
    def result = DataUtils.setValueOnPojo(pojo, fieldName, value)

    then:
    result.is(pojo) // should be the exact same object
    result.getTestField() == value
  }

  def "setValueOnPojo throws up if it can't find a setter to use"() {
    def pojo = new TestPojo()
    def fieldName = 'NOTAREALFIELD'
    def value = 'super great test string'

    when:
    def result = DataUtils.setValueOnPojo(pojo, fieldName, value)

    then:
    thrown(UnsupportedOperationException)
  }

  def "setValueOnPojo throws up if it tries to set a field it can't"() {
    def pojo = new TestPojo()
    def fieldName = 'hiddenField'
    def value = 'super great test string'

    when:
    def result = DataUtils.setValueOnPojo(pojo, fieldName, value)

    then:
    thrown(UnsupportedOperationException)
  }

  class TestPojo {
    private String testField;
    private String hiddenField = "hidden"
    TestPojo() {}
    void setTestField(String s) { testField = s }
    String getTestField() { return testField; }
  }

}
