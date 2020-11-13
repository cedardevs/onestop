import org.cedar.onestop.data.api.JsonApiData
import org.cedar.onestop.data.api.JsonApiMeta
import org.cedar.onestop.data.api.JsonApiSuccessResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import javax.servlet.http.HttpServletResponse;

class JsonApiSuccessResponseSpec extends Specification {
  @Shared
  HttpServletResponse httpServletResponse = Mock(HttpServletResponse)

  @Shared
  JsonApiData validData = new JsonApiData.Builder()
      .setId('1')
      .setType('search').build()

  @Shared
  JsonApiMeta validMeta = new JsonApiMeta.Builder().setNonStandardMetadata([:]).build()

  def 'Success response sets member variables appropriately' () {
    setup:
    List<JsonApiData> listData = new ArrayList<>()
    listData.add(validData)
    int status = 200

    when:
    JsonApiSuccessResponse response = new JsonApiSuccessResponse.Builder()
        .setStatus(status, httpServletResponse)
        .setData(listData)
        .setMeta(validMeta).build()

    then:
    response.data == listData
    response.meta == validMeta
    response.status == status
  }

  @Unroll
  def 'Json document MUST contain at least data or meta and MUST respond with a status where status=#status, data=#data, and meta=#meta' () {
    setup:
    List<JsonApiData> listData = new ArrayList<>()
    listData.add(data)

    when:
    new JsonApiSuccessResponse.Builder()
        .setStatus((int)status, httpServletResponse)
        .setData(data? listData : null)
        .setMeta(meta).build()

    then:
    NullPointerException ex = thrown()
    ex.message == 'JSON:Successful response must have status and either data or meta set'

    where:
    status | data      | meta
    0      | validData | null
    1      | null      | null
  }

  def 'data and errors MUST NOT coexist' () {
    setup:
    List<JsonApiData> listData = new ArrayList<>()
    listData.add(validData)

    when:
    JsonApiSuccessResponse response = new JsonApiSuccessResponse.Builder()
        .setStatus(1, httpServletResponse)
        .setData(listData).build()

    then:
    response.data
    !response.errors
  }

  @Ignore // The mocked HttpServletResponse isn't letting it's status be changed by the method being tested.
  def 'status must also set status in response' () {
    setup:
    int status = 200
    List<JsonApiData> listData = new ArrayList<>()
    listData.add(validData)

    when:
    new JsonApiSuccessResponse.Builder()
        .setStatus(status, httpServletResponse)
        .setData(listData).build()

    then:
    httpServletResponse.status == status
  }
}
