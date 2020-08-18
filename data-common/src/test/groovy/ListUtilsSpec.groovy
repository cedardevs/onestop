import org.cedar.onestop.data.util.ListUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ListUtilsSpec extends Specification {

  def "addOrInit works (add #inItem to #inList)"() {
    expect:
    ListUtils.addOrInit(inList, inItem) == out

    where:
    inList | inItem  | out
    null   | 'a'     | ['a']
    []     | 'a'     | ['a']
    ['x']  | 'a'     | ['x', 'a']
    null   | null    | []
    []     | null    | []
    ['x']  | null    | ['x']
  }

  def "truncateList works (#inList)"() {
    expect:
    ListUtils.truncateList(inList, listSize, tailEnd) == outList

    where:
    inList    | listSize | tailEnd | outList
    null      | 2        | true    | []
    []        | 2        | true    | []
    [1, 2, 3] | 2        | true    | [2, 3]
    [1, 2, 3] | 2        | false   | [1, 2]
    [1, 2, 3] | 5        | false   | [1, 2, 3]
  }

  def "trucateList throws error on bad listSize value"() {
    when:
    def list = new ArrayList()
    list.addAll([1, 2, 3])
    def result = ListUtils.truncateList(list, -1, true)

    then:
    thrown(IllegalArgumentException)
  }

  def "pruneEmptyElements removes nulls and empty strings (#inList)"() {
    expect:
    ListUtils.pruneEmptyElements(inList) == outList

    where:
    inList                | outList
    null                  | null
    []                    | []
    ['', '', '']          | []
    [1, 'a', '', 3]       | [1, 'a', 3]
    ['x', 'y', null, 'z'] | ['x', 'y', 'z']
  }

  def "pruneEmptyElements removes empty elements when list of maps"() {
    expect:
    def inList = [ [:], [a: 'b']]
    ListUtils.pruneEmptyElements(inList) == [[ a: 'b'] ]
  }

  def "pruneEmptyElements removes empty elements when list of lists"() {
    expect:
    def inList = [ [], ['a', 'b']]
    ListUtils.pruneEmptyElements(inList) == [[ 'a', 'b'] ]
  }
}
