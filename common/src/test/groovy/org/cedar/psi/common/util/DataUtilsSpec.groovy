package org.cedar.psi.common.util

import org.cedar.schemas.avro.psi.InputEvent
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DataUtilsSpec extends Specification {

  def "addOrInit works"() {
    expect:
    DataUtils.addOrInit(inList, inItem) == out

    where:
    inList | inItem  | out
    null   | 'a'     | ['a']
    []     | 'a'     | ['a']
    ['x']  | 'a'     | ['x', 'a']
    null   | null    | []
    []     | null    | []
    ['x']  | null    | ['x']
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
    in1             | in2             | out
    null            | null            | [:]
    null            | [b: 2]          | [b: 2]
    [a: 1]          | null            | [a: 1]
    [:]             | [:]             | [:]
    [a: 1]          | [:]             | [a: 1]
    [:]             | [b: 2]          | [b: 2]
    [a: 1]          | [b: 2]          | [a: 1, b: 2]
    [a: 1]          | [a: 2]          | [a: 2]
    [a: 1, b: 1]    | [b: 2]          | [a: 1, b: 2]
    [b: 1]          | [a: null, b: 2] | [a: null, b: 2]
    [a: null, b: 1] | [b: 2]          | [a: null, b: 2]
  }

  def "mergeMaps supports deep merge"() {
    when:
    def in1 = [
        a: "First a value",
        b: [1, 2],
        c: [
            d: "First d value",
            e: "First e value",
            g: ["a", "b"]
        ]
    ]
    def in2 = [
        a: "Second a value",
        c: [
            f: "First f value",
            g: ["a", "c"]
        ]
    ]
    def result = DataUtils.mergeMaps(in1, in2)

    then:
    def expected = [
        a: "Second a value",
        b: [1, 2],
        c: [
            d: "First d value",
            e: "First e value",
            f: "First f value",
            g: ["a", "b", "c"]
        ]
    ]
    result.a == expected.a
    result.b.containsAll(expected.b)
    result.c.d == expected.c.d
    result.c.e == expected.c.e
    result.c.f == expected.c.f
    result.c.g.size() == 3
    result.c.g.containsAll(expected.c.g)
  }

  def "mergeMaps merges a list of simple maps, doesn't introduce duplicates"() {
    when:
    def in1 = [
        a: [[
            b: "string1",
            c: 1234
        ],
        [
            b: "string2",
            c: 5678
        ]]
    ]
    def in2 = [
        a: [[
            b: "string2",
            c: 5678
        ],[
            b: "string3",
            c: 9012
        ]]
    ]
    def result = DataUtils.mergeMaps(in1, in2)

    then:
    def expectedA = new ArrayList(Arrays.asList([b: "string1", c: 1234], [b: "string2", c: 5678], [b: "string3", c: 9012]))
    result.a instanceof List
    result.a.size() == 3
    result.a.containsAll(expectedA)
  }

  def "mergeMaps merges a nested list of maps, doesn't introduce duplicates"() {
    when:
    def in1 = [
        a: 1,
        b: [
            c: "First b.c value",
            d: [[
                e: "First b.d.e value",
                f: 1
            ],[
                e: "Second b.d.e value",
                f: 2
            ]]
        ]
    ]
    def in2 = [
        b: [
            d: [[
                    e: "Third b.d.e value",
                    f: 3
                ],[
                    e: "Fourth b.d.e value",
                    f: 1
                ],[
                    e: "Second b.d.e value",
                    f: 2
                ]]
        ]
    ]
    def result = DataUtils.mergeMaps(in1, in2)

    then:
    def expected = [
        a: 1,
        b: [
            c: "First b.c value",
            d: [[
                    e: "First b.d.e value",
                    f: 1
                ],[
                    e: "Second b.d.e value",
                    f: 2
                ],[
                    e: "Third b.d.e value",
                    f: 3
                ],[
                    e: "Fourth b.d.e value",
                    f: 1
                ]]
        ]
    ]
    result.a == expected.a
    result.b.c == expected.b.c
    result.b.d.size() == 4
    result.b.d.containsAll(expected.b.d)
  }

  def "removeFromMap supports simple, shallow removals"() {
    expect:
    DataUtils.removeFromMap(in1, in2) == out

    where:
    in1             | in2             | out
    null            | null            | [:]
    [:]             | [:]             | [:]
    null            | [b: 2]          | [:]
    [:]             | [b: 2]          | [:]
    [a: 1]          | null            | [a: 1]
    [a: 1]          | [:]             | [a: 1]
    [a: 1]          | [b: 2]          | [a: 1]
    [a: 1]          | [a: 2]          | [a: 1]
    [a: 1]          | [a: 1]          | [:]
    [a: 1, b: 1]    | [b: 1]          | [a: 1]
    [a: null, b: 1] | [a: null]       | [b: 1]
    [a: "hat"]      | [a: "cat"]      | [a: "hat"]
    [a: "hat"]      | [a: "hat"]      | [:]
  }

  def "removeFromMap supports nested removal from a simple list"() {
    when:
    def in1 = [
        a: ["one", "two", "three"]
    ]
    def in2 = [
        a: ["two"]
    ]
    def result = DataUtils.removeFromMap(in1, in2)

    then:
    def expectedA = ["one", "three"]
    result.a.size() == 2
    result.a.containsAll(expectedA)
  }

  def "removeFromMap supports removal from a nested list of maps"() {
    when:
    def in1 = [
        a: 1,
        b: [
            c: "First b.c value",
            d: [[
                    e: "First b.d.e value",
                    f: 1
                ],[
                    e: "Second b.d.e value",
                    f: 2
                ]]
        ]
    ]
    def in2 = [
        b: [
            d: [[
                    e: "Fourth b.d.e value",
                    f: 1
                ],[
                    e: "Second b.d.e value",
                    f: 2
                ]]
        ]
    ]
    def result = DataUtils.removeFromMap(in1, in2)

    then:
    def expected = [
        a: 1,
        b: [
            c: "First b.c value",
            d: [[
                    e: "First b.d.e value",
                    f: 1
                ]]
        ]
    ]
    result.a == expected.a
    result.b.c == expected.b.c
    result.b.d.size() == 1
    result.b.d.containsAll(expected.b.d)
  }

  def "consolidateNestedKeysInMap returns a non-nested map"() {
    given:
    def inputMap = [
        a: "a value",
        b: [1, 2],
        c: [
            d: "c.d value",
            e: "c.e value",
            f: ["f1", "f2"],
            g: [
                h: "c.g.h value"
            ]
        ]
    ]

    def expected = [
        a: "a value",
        b: [1, 2],
        "c.d": "c.d value",
        "c.e": "c.e value",
        "c.f": ["f1", "f2"],
        "c.g.h": "c.g.h value"
    ]

    expect:
    DataUtils.consolidateNestedKeysInMap(parentKey, ".", inputMap).equals(expected);

    where:
    parentKey << [new String(), "", null]
  }

  def "trimMapKeys... does that"() {
    def original = [
        trimA: "A string",
        trimB: 123,
        C: [1, 2, 3]
    ]
    def trimString = 'trim'

    when:
    def result = DataUtils.trimMapKeys(trimString, original)

    then:
    result == [
        A: "A string",
        B: 123,
        C: [1, 2, 3]
    ]
  }

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
