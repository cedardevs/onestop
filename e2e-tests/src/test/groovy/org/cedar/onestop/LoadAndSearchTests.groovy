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

class LoadAndSearchTests extends Specification {

  @Shared
  @ClassRule
  DockerComposeContainer docker = new DockerComposeContainer(new File("../docker-compose.yml"))
      .withLocalCompose(true)
      .withPull(false)

  static esApiBase = "http://localhost:9200"
  static searchApiBase = "http://localhost:8097/onestop/api"
  static metadataApiBase = "http://localhost:8098/onestop/api"
  static restTemplate = new RestTemplate()

  def setupSpec() {
    def pollingConditions = new PollingConditions()
    pollingConditions.within(30, {
      restTemplate.exchange(RequestEntity.get(esApiBase.toURI()).build(), Map).statusCode == HttpStatus.OK
    })
    pollingConditions.within(30, {
      restTemplate.exchange(RequestEntity.get("${searchApiBase}/info".toURI()).build(), Map).statusCode == HttpStatus.OK
    })
    pollingConditions.within(30, {
      restTemplate.exchange(RequestEntity.get("${metadataApiBase}/info".toURI()).build(), Map).statusCode == HttpStatus.OK
    })
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

    sleep(2000) // to ensure the ETL finishes

    when:
    def searchRequst = RequestEntity.post("${searchApiBase}/search".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"queries":[{ "type": "queryText", "value": "temperature OR elevation"}]}')
    def searchResult = restTemplate.exchange(searchRequst, Map)

    then:
    searchResult.statusCode == HttpStatus.OK
    searchResult.body.data.size() == 7
    def coopsCollection = searchResult.body.data.find({ it.attributes.fileIdentifier == 'gov.noaa.nodc:NDBC-COOPS' })
    coopsCollection?.id instanceof String

    when:
    def granuleRequst = RequestEntity.post("${searchApiBase}/search".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"filters":[{"type":"collection", "values":["' + coopsCollection.id + '"]}]}')
    def granuleResult = restTemplate.exchange(granuleRequst, Map)

    then:
    granuleResult.statusCode == HttpStatus.OK
    granuleResult.body.data.size() == 2

    when:
    def deleteRequest = RequestEntity.delete("${metadataApiBase}/metadata/${coopsCollection.id}".toURI()).build()
    def deleteResult = restTemplate.exchange(deleteRequest, Map)

    then:
    deleteResult.statusCode == HttpStatus.OK

    sleep(16000) // to ensure the delete finishes

    when:
    def searchResult2 = restTemplate.exchange(searchRequst, Map)

    then:
    searchResult2.statusCode == HttpStatus.OK
    searchResult2.body.data.size() == 6
    searchResult2.body.data.every({ it.attributes.fileIdentifier != 'gov.noaa.nodc:NDBC-COOPS' })

    when:
    def granuleResult2 = restTemplate.exchange(granuleRequst, Map)

    then:
    granuleResult2.statusCode == HttpStatus.OK
    granuleResult2.body.data.size() == 0
  }

}
