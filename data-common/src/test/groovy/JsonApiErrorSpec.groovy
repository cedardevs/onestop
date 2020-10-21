import org.cedar.onestop.data.api.JsonApiData
import org.cedar.onestop.data.api.JsonApiError
import org.cedar.onestop.data.api.JsonApiError.ErrorSource
import org.cedar.onestop.data.api.JsonApiMeta
import org.cedar.onestop.data.api.JsonApiSuccessResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletResponse

class JsonApiErrorSpec extends Specification {
  @Shared
  HttpServletResponse httpServletResponse = Mock(HttpServletResponse)

  @Shared
  JsonApiMeta validMeta = new JsonApiMeta.Builder().setNonStandardMetadata([:]).build()

  @Unroll
  def 'An error response MAY have any members' () {
    setup:
    String id = '013'
    int status = 200
    String code = 'code message'
    String title = 'title'
    String detail = 'detail'
    ErrorSource errorSource = new ErrorSource('pointer', 'parameter')

    when:
    JsonApiError response = new JsonApiError.Builder()
        .setId(id)
        .setStatus((int)status, httpServletResponse)
        .setMeta(validMeta)
        .setCode(code)
        .setTitle(title)
        .setDetail(detail)
        .setSource(errorSource).build()

    then:
    response.id == id
    response.status == status
    response.code == code
    response.title == title
    response.detail == detail
    response.meta == validMeta
    response.source == errorSource
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
