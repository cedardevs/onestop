import groovy.json.JsonOutput
import org.cedar.onestop.data.util.JsonUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JsonUtilsSpec extends Specification {

  def "getJsonDiffList returns empty list for matching maps"() {
    given:
    def source = [
        a: 1,
        b: "B value",
        c: [
            d: 1.2,
            e: "E!"
        ]
    ]

    def target = [
        a: 1,
        c: [
            e: "E!",
            d: 1.2
        ],
        b: "B value"
    ]

    when:
    def diffList = JsonUtils.getJsonDiffList(source, target)

    then:
    diffList.isEmpty()
  }

  def "getJsonDiffList returns ordered list of JSON PATCH diffs to go from source to target"() {
    given:
    def source = [
        a: 1,
        b: "B value",
        c: [
            d: 1.2,
            e: "E!",
            f: ["Here I am"]
        ]
    ]

    def target = [
        a: 2,
        c: [
            d: 1.2,
            e: "E!",
            f: ["Here am I", "I wasn't here before"]
        ],
        g: true,
        h: null
    ]

    when:
    def diffList = JsonUtils.getJsonDiffList(source, target)

    then:
    diffList.size() == 6
    JsonOutput.toJson(diffList.get(0)) == JsonOutput.toJson([op: "replace", path: "/a", value: 2])
    JsonOutput.toJson(diffList.get(1)) == JsonOutput.toJson([op: "remove", path: "/b"])
    JsonOutput.toJson(diffList.get(2)) == JsonOutput.toJson([op: "replace", path: "/c/f/0", value: "I wasn't here before"])
    JsonOutput.toJson(diffList.get(3)) == JsonOutput.toJson([op: "add", path: "/c/f/0", value: "Here am I"])
    JsonOutput.toJson(diffList.get(4)) == JsonOutput.toJson([op: "add", path: "/g", value: true])
    JsonOutput.toJson(diffList.get(5)) == JsonOutput.toJson([op: "add", path: "/h", value: null])

  }

  def "parseJsonAsMap works for good json"() {
    when:
    def result = JsonUtils.parseJsonAsMapSafe('{"hello":"world","list":[1,2]}')

    then:
    result instanceof Map
    result.hello == 'world'
    result.list instanceof List
    result.list == [1, 2]
  }

  def "parseJsonAsMap throws up on bad json"() {
    when:
    def result = JsonUtils.parseJsonAsMapSafe('THIS IS NOT JSON')

    then:
    thrown(Exception)
  }

  def "parseJsonAsMap handles #situation as expected"() {
    when:
    def result = JsonUtils.parseJsonAsMapSafe(input)

    then:
    result == expected

    where:
    situation            | input | expected
    'null input'         | null  | [:]
    '{} input'           | '{}'  | [:]
    'empty string input' | ''    | [:]
  }

  def "toJson handles #situation as expected"() {
    expect:
    JsonUtils.toJson(input) == expected

    where:
    situation         | input | expected
    'null input'      | null  | null
    'empty map input' | [:]   | '{}'
    'empty string'    | ''    | '""'
  }
}
