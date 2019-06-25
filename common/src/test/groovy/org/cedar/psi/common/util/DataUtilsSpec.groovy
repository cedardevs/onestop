package org.cedar.psi.common.util

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DataUtilsSpec extends Specification {

  def "addOrInit works"() {
    expect:
    DataUtils.addOrInit(inList, inItem) == out

    where:
    inList | inItem | out
    null   | 'a'    | ['a']
    []     | 'a'    | ['a']
    ['x']  | 'a'    | ['x', 'a']
  }

  def "parseJsonMap works for good json"() {
    when:
    def result = DataUtils.parseJsonMap('{"hello":"world","list":[1,2]}')

    then:
    result instanceof Map
    result.hello == 'world'
    result.list instanceof List
    result.list == [1, 2]
  }

  def "parseJsonMap throws up on bad json"() {
    when:
    def result = DataUtils.parseJsonMap('THIS IS NOT JSON')

    then:
    thrown(Exception)
  }

  def "mergeMaps supports simple, shallow merges"() {
    expect:
    DataUtils.mergeMaps(in1, in2) == out

    where:
    in1           | in2           | out
    null          | null          | null
    null          | [b: 2]        | [b: 2]
    [a: 1]        | null          | [a: 1]
    [:]           | [:]           | [:]
    [a: 1]        | [:]           | [a: 1]
    [:]           | [b: 2]        | [b: 2]
    [a: 1]        | [b: 2]        | [a: 1, b: 2]
    [a: 1]        | [a: 2]        | [a: 2]
  }

  // TODO - there are so many more ways to merge maps...

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

  class TestPojo {
    private String testField;
    TestPojo() {}
    void setTestField(String s) { testField = s }
    String getTestField() { return testField; }
  }

}
