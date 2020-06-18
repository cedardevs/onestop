import org.cedar.onestop.data.util.ListUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ListUtilsSpec extends Specification {

  def "addOrInit works"() {
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

  def "truncateList works"() {
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

  def "pruneEmptyElements removes nulls and empty strings"() {
    // TODO
  }

  def "pruneEmptyElements removes empty elements when list of maps"() {
    // TODO
  }
}
