import groovy.json.JsonOutput
import groovy.json.JsonParser
import org.cedar.onestop.data.util.MapUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MapUtilsSpec extends Specification {

  def "mergeMaps supports simple, shallow merges"() {
    expect:
    MapUtils.mergeMaps(in1, in2) == out

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
    def result = MapUtils.mergeMaps(in1, in2)

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
    def result = MapUtils.mergeMaps(in1, in2)

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
    def result = MapUtils.mergeMaps(in1, in2)

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
    MapUtils.removeFromMap(in1, in2) == out

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
    def result = MapUtils.removeFromMap(in1, in2)

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
    def result = MapUtils.removeFromMap(in1, in2)

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
    MapUtils.consolidateNestedKeysInMap(parentKey, ".", inputMap).equals(expected);

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
    def result = MapUtils.trimMapKeys(trimString, original)

    then:
    result == [
        A: "A string",
        B: 123,
        C: [1, 2, 3]
    ]
  }

  def "sortMapByKeys works at top level and in nested map and lists of maps"() {
    given:
    def expectedMap = [
        a: "valueA",
        b: 123,
        c: [
            d: false,
            e: [5, 4, 2],
            f: [[a: 3, b: 4], [a: 1, b: 2]]
        ]
    ]

    def givenMap = [
        b: 123,
        c: [
            e: [5, 4, 2],
            d: false,
            f: [[a: 3, b: 4], [b: 2, a: 1]]
        ],
        a: "valueA"
    ]

    when:
    def sortedResultMap = MapUtils.sortMapByKeys(givenMap)
    def sortedResult = JsonOutput.toJson(sortedResultMap)

    then:
    sortedResult.equals(JsonOutput.toJson(expectedMap))
  }

  def "pruneEmptyElements traverses a nested map successfully"() {
    given:
    def input = [
        a: 1,
        b: null,
        c: [
            d: "string",
            e: "",
            f: [],
            g: [1, "", "stuff", null]
        ],
        h: [:]
    ]

    def expected = [
        a: 1,
        c: [
            d: "string",
            g: [1, "stuff"]
        ]
    ]

    when:
    def result = MapUtils.pruneEmptyElements(input)

    then:
    result == expected
  }

  def "pruneEmptyElements handles null inputs"() {
    expect:
    MapUtils.pruneEmptyElements(null) == null
  }
}
