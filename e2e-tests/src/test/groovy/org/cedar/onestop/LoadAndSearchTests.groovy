package org.cedar.onestop

import org.junit.ClassRule
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

class LoadAndSearchTests extends Specification {

  @Shared
  @ClassRule
  DockerComposeContainer docker = new DockerComposeContainer(new File("src/test/resources/docker-compose-e2e.yml"))
      .withLocalCompose(true)
      .withPull(false)

  static esApiBase = "http://localhost:9200"
  static searchApiBase = "http://localhost:8097/onestop/api"
  static metadataApiBase = "http://localhost:8098/onestop/admin"
  static restTemplate = new RestTemplate()

  def setupSpec() {
    def pollingConditions = new PollingConditions()
    pollingConditions.setDelay(5)
    pollingConditions.within(60, {
      restTemplate.exchange(RequestEntity.get(esApiBase.toURI()).build(), Map).statusCode == HttpStatus.OK
      restTemplate.exchange(RequestEntity.get("${searchApiBase}/actuator/info".toURI()).build(), Map).statusCode == HttpStatus.OK
      restTemplate.exchange(RequestEntity.get("${metadataApiBase}/actuator/info".toURI()).build(), Map).statusCode == HttpStatus.OK
    })
  }

  def setup() {
    // delete all indices between tests
    def deleteRequest = RequestEntity.delete("${esApiBase}/_all".toURI()).build()
    def deleteResult = restTemplate.exchange(deleteRequest, Map)
  }

  @Test
  void 'load -> update -> search -> delete -> search'() {
    when:
    def paths = [
        'data/COOPS/C1.xml',
        'data/COOPS/G1.xml',
        'data/COOPS/G2.xml',
        'data/COOPS/O1.xml',
        'data/DEM/1.xml',
        'data/DEM/2.xml',
        'data/DEM/3.xml',
        'data/GHRSST/1.xml',
        'data/GHRSST/2.xml',
        'data/GHRSST/3.xml',
    ]
    def body = new LinkedMultiValueMap<String, Object>()
    paths.each { body.add("files", new ClassPathResource(it)) }
    def loadRequest = RequestEntity.post("${metadataApiBase}/metadata".toURI())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
    def loadResult = restTemplate.exchange(loadRequest, Map)

    then:
    loadResult.statusCode == HttpStatus.MULTI_STATUS

    when:
    def updateRequest = RequestEntity.get("${metadataApiBase}/admin/index/search/update".toURI()).build()
    def updateResult = restTemplate.exchange(updateRequest, Map)

    then:
    updateResult.statusCode == HttpStatus.OK

    sleep(60000) // to ensure the ETL finishes

    when:
    def refreshRequest = RequestEntity.post("${esApiBase}/_refresh".toURI()).build()
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
    def deleteRequest = RequestEntity.delete("${metadataApiBase}/metadata/${coopsCollection.id}".toURI()).build()
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
    def loadRequest = RequestEntity.post("${metadataApiBase}/metadata".toURI())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
    def loadResult = restTemplate.exchange(loadRequest, Map)
    def updateRequest = RequestEntity.get("${metadataApiBase}/admin/index/search/update".toURI()).build()
    def updateResult = restTemplate.exchange(updateRequest, Map)

    then:
    loadResult.statusCode == HttpStatus.MULTI_STATUS
    updateResult.statusCode == HttpStatus.OK

    sleep(10000) // to ensure the ETL finishes

    when:
    def refreshRequest = RequestEntity.post("${esApiBase}/_refresh".toURI()).build()
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
    // TODO - test files into this subproject and then sharing with api-metadata and api-search

    then:
    resultWithoutId == expectedJson
  }

}
