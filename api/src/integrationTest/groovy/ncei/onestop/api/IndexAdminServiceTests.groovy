package ncei.onestop.api

import ncei.onestop.api.service.IndexAdminService
import org.elasticsearch.client.Client
import org.elasticsearch.common.io.stream.OutputStreamStreamOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@ActiveProfiles("integration")
@TestPropertySource(properties = ['elasticsearch.index.prefix=testprefix-'])
@SpringBootTest(classes = [Application, IntegrationTestConfig])
class IndexAdminServiceTests extends Specification {

  @Autowired
  private Client client

  @Autowired
  private IndexAdminService indexAdminService

  private String prefix = 'testprefix-' // NOTE - set in Environment via @TestPropertySource above

  def 'the service can use a prefix when creating indices and aliases'() {
    expect:
    client.admin().indices()
        .prepareGetAliases("${prefix}*")
        .execute().actionGet().writeTo(new OutputStreamStreamOutput(System.out))

    def aliasResponse = client.admin().indices()
        .prepareGetAliases("${prefix}*")
        .execute().actionGet().aliases

    def indexNames = aliasResponse.keys()
    indexNames.size() == 2
    indexNames.every { it.value.startsWith('testprefix-') }

    def aliasNames = aliasResponse.collect({ it.value*.alias }).flatten()
    aliasNames.size() == 2
    aliasNames.find { it.startsWith("${prefix}search_")}
    aliasNames.find { it.startsWith("${prefix}staging_") }
  }

}
