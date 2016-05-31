package ncei.onestop.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@WebIntegrationTest("server.port:8097")
//@WebIntegrationTest("server.port:8097")
@SpringApplicationConfiguration(classes = Application)
class SearchApiIntegrationSpec extends Specification {
    @Value('${local.server.port}')
    String testPort = "8097";
    MediaType contentType = MediaType.APPLICATION_JSON

    String searchBaseUrl = "http://localhost:${testPort}/search"

    private static RestTemplate setupRestClient() {
        RestTemplate restTemplate = new RestTemplate()
        restTemplate.setErrorHandler(new TestResponseErrorHandler())
        restTemplate
    }

    def searches = [
            baresearch: [
                    body: '{"searchText": "temperature"}'
            ]
    ]

    // -------- Test Cases --------
    def 'valid search returns ok and results'() {
        println("searchBaseUrl:${searchBaseUrl}")
        def restTemplate = setupRestClient()
        def requestEntity = RequestEntity
                .post(searchBaseUrl.toURI())
                .contentType(contentType)
                .body(searches.baresearch.body)

        when:
        def result = restTemplate.exchange(requestEntity, Map)

        then:
        result.statusCode == HttpStatus.OK
    }


}
