package org.cedar.onestop.api.search

import org.cedar.onestop.api.search.service.ElasticsearchService
import org.cedar.onestop.elastic.common.ElasticsearchTestConfig
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@DirtiesContext
@ActiveProfiles(["integration"])
@SpringBootTest(
    classes = [Application, ElasticsearchTestConfig],
    webEnvironment = RANDOM_PORT,
    properties = ["elasticsearch.index.prefix=search_field_query_"]
)
@Unroll
class FieldQueryIntegrationTests extends Specification {
  static private final String FIELD_QUERY_ALIAS = 'field_query'

  @Autowired
  RestHighLevelClient restHighLevelClient

  @Autowired
  ElasticsearchService esService

  void setup() {
    TestUtil.resetLoadAndRefreshGenericTestIndex(FIELD_QUERY_ALIAS, restHighLevelClient, esService)
  }

  def 'Can search by nested field'() {
    // Null spatial boundings should not be excluded
    given:
    def requestParams = [
        filters: [
            [
                type : 'field',
                name : 'links.linkUrl',
                value: 's3://noaa-goes16/1111.nc'
            ]
        ],
        summary: false
    ]

    when:
    def queryResponse = esService.searchFromRequest(requestParams, FIELD_QUERY_ALIAS)

    then:
    def actualMatchingIds = queryResponse.data.collect { it.id }
    actualMatchingIds.size() == 1
    actualMatchingIds.contains('1')
  }
}