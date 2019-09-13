package org.cedar.onestop

import org.junit.ClassRule
import org.junit.Ignore
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.DockerComposeContainer
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import groovy.json.JsonSlurper

@Ignore
class LoadAndSearchTests extends Specification {

  @Shared
  @ClassRule
  DockerComposeContainer docker

  static searchApiBase = "http://localhost:8097/onestop-search"
  static adminApiBase = "http://localhost:8098/onestop-admin"
  static restTemplate = new RestTemplate()

  static String dockerComposeFile() {
    String file = System.getenv("docker.compose.file")
    if(file == null) {
      throw new RuntimeException("E2E test could not determine `docker.compose.file` from env.")
    }
    return file
  }

  static String esApiBase() {
    String host = System.getenv("elasticsearch.host")
    String port = System.getenv("elasticsearch.port")
    if(host == null || port == null) {
      throw new RuntimeException("E2E test could not determine `elasticsearch.host` or `elasticsearch.port` from env.")
    }
    return String.format("http://%s:%s", host, port)
  }

  def setupSpec() {
    def pollingConditions = new PollingConditions()
    pollingConditions.setDelay(5)
    pollingConditions.within(60, {
      restTemplate.exchange(RequestEntity.get(esApiBase().toURI()).build(), Map).statusCode == HttpStatus.OK
      restTemplate.exchange(RequestEntity.get("${searchApiBase}/actuator/info".toURI()).build(), Map).statusCode == HttpStatus.OK
      restTemplate.exchange(RequestEntity.get("${adminApiBase}/actuator/info".toURI()).build(), Map).statusCode == HttpStatus.OK
    })
  }

  def setup() {
    // run docker-compose test containers to be able to run e2e against
    this.docker = new DockerComposeContainer(new File(dockerComposeFile()))
        .withLocalCompose(true)
        .withPull(false)

    // delete all indices between tests
    def deleteRequest = RequestEntity.delete("${esApiBase()}/_all".toURI()).build()
    def deleteResult = restTemplate.exchange(deleteRequest, Map)
  }

  @Test
  void 'load -> update -> search -> delete -> search'() {
    when:
    def paths = [
        'test/data/xml/COOPS/C1.xml',
        'test/data/xml/COOPS/G1.xml',
        'test/data/xml/COOPS/G2.xml',
        'test/data/xml/COOPS/O1.xml',
        'test/data/xml/DEM/1.xml',
        'test/data/xml/DEM/2.xml',
        'test/data/xml/DEM/3.xml',
        'test/data/xml/GHRSST/1.xml',
        'test/data/xml/GHRSST/2.xml',
        'test/data/xml/GHRSST/3.xml',
    ]
    def body = new LinkedMultiValueMap<String, Object>()
    paths.each { body.add("files", new ClassPathResource(it)) }
    def loadRequest = RequestEntity.post("${adminApiBase}/metadata".toURI())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
    def loadResult = restTemplate.exchange(loadRequest, Map)

    then:
    loadResult.statusCode == HttpStatus.MULTI_STATUS

    when:
    def updateRequest = RequestEntity.get("${adminApiBase}/admin/index/search/update".toURI()).build()
    def updateResult = restTemplate.exchange(updateRequest, Map)

    then:
    updateResult.statusCode == HttpStatus.OK

    sleep(60000) // to ensure the ETL finishes

    when:
    def refreshRequest = RequestEntity.post("${esApiBase()}/_refresh".toURI()).build()
    restTemplate.exchange(refreshRequest, Map)
    def searchRequest = RequestEntity.post("${searchApiBase}/search/collection".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"queries":[{ "type": "queryText", "value": "temperature OR elevation"}],"summary": false}')
    def searchResult = restTemplate.exchange(searchRequest, Map)
    def collectionData = searchResult.body.data

    then:
    searchResult.statusCode == HttpStatus.OK
    collectionData.size() == 7
    def coopsCollection = collectionData.find({ it.attributes.fileIdentifier == 'gov.noaa.nodc:NDBC-COOPS' })
    coopsCollection?.id instanceof String

    when:
    def granuleRequest = RequestEntity.post("${searchApiBase}/search/granule".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"filters":[{"type":"collection", "values":["' + coopsCollection.id + '"]}],"summary": false}')
    def granuleResult = restTemplate.exchange(granuleRequest, Map)
    def granuleData = granuleResult.body.data

    then:
    granuleResult.statusCode == HttpStatus.OK
    granuleData.size() == 2

    when:
    def deleteRequest = RequestEntity.delete("${adminApiBase}/metadata/${coopsCollection.id}".toURI()).build()
    def deleteResult = restTemplate.exchange(deleteRequest, Map)

    then:
    deleteResult.statusCode == HttpStatus.OK

    sleep(10000) // to ensure the delete finishes

    when:
    restTemplate.exchange(refreshRequest, Map)
    def searchResult2 = restTemplate.exchange(searchRequest, Map)

    then:
    searchResult2.statusCode == HttpStatus.OK
    searchResult2.body.data.size() == 6
    searchResult2.body.data.every({ it.attributes.fileIdentifier != 'gov.noaa.nodc:NDBC-COOPS' })

    when:
    def granuleResult2 = restTemplate.exchange(granuleRequest, Map)

    then:
    granuleResult2.statusCode == HttpStatus.OK
    granuleResult2.body.data.size() == 0
  }

  @Test
  void 'full json output'() {
    when:
    def paths = [
        'test-iso-metadata-collection.xml',
    ]
    def body = new LinkedMultiValueMap<String, Object>()
    paths.each { body.add("files", new ClassPathResource(it)) }
    def loadRequest = RequestEntity.post("${adminApiBase}/metadata".toURI())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
    def loadResult = restTemplate.exchange(loadRequest, Map)
    def updateRequest = RequestEntity.get("${adminApiBase}/admin/index/search/update".toURI()).build()
    def updateResult = restTemplate.exchange(updateRequest, Map)

    then:
    loadResult.statusCode == HttpStatus.MULTI_STATUS
    updateResult.statusCode == HttpStatus.OK

    sleep(10000) // to ensure the ETL finishes

    when:
    def refreshRequest = RequestEntity.post("${esApiBase()}/_refresh".toURI()).build()
    restTemplate.exchange(refreshRequest, Map)
    def searchRequest = RequestEntity.post("${searchApiBase}/search/collection".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"queries":[{ "type": "queryText", "value": "super"}],"summary": false}')
    def searchResult = restTemplate.exchange(searchRequest, Map)

    then:
    searchResult.statusCode == HttpStatus.OK
    searchResult.body.data.size() == 1
    searchResult.body.data[0].id != null
    searchResult.body.data[0].attributes.stagedDate != null

    when: // remove the fields that are unique each time:
    def resultWithoutId = searchResult.body.data[0]
    resultWithoutId.id = null
    resultWithoutId.attributes.stagedDate = null
    def expectedJson = (new JsonSlurper()).parseText( ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.json").text)

    // TODO - Next time we come through here, think about consolidating all our matching xml and json
    // TODO - test files into this subproject and then sharing with admin and search APIs

    then:
    resultWithoutId == expectedJson
  }

}
