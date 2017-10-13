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
import groovy.json.JsonBuilder

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

  def setup() {
    // delete all indicies between tests
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

    sleep(2000) // to ensure the delete finishes

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

  @Test
  void 'full json output'() {
    when:
    def paths = [
        'test-iso-metadata.xml',
    ]
    def body = new LinkedMultiValueMap<String, Object>()
    paths.each { body.add("files", new ClassPathResource(it)) }
    def loadRequest = RequestEntity.post("${metadataApiBase}/metadata".toURI())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
    def loadResult = restTemplate.exchange(loadRequest, Map)
    println('Zeb look!')
    println(body)
    def updateRequest = RequestEntity.get("${metadataApiBase}/admin/index/search/update".toURI()).build()
    def updateResult = restTemplate.exchange(updateRequest, Map)

    then:

sleep(2000) // to ensure the ETL finishes

    def esSearchRequest = RequestEntity.get("${esApiBase}/_search".toURI()).build()
    def esSearchResult = restTemplate.exchange(esSearchRequest, Map)
    println('esSearchResult')
    println(esSearchResult)

    loadResult.statusCode == HttpStatus.MULTI_STATUS
    println('Zeb look here too')
    println(loadResult)
    updateResult.statusCode == HttpStatus.OK

    sleep(2000) // to ensure the ETL finishes

    when:
    def searchRequst = RequestEntity.post("${searchApiBase}/search".toURI())
        .contentType(MediaType.APPLICATION_JSON)
        .body('{"queries":[{ "type": "queryText", "value": "super"}]}')
    def searchResult = restTemplate.exchange(searchRequst, Map)

    then:
    searchResult.statusCode == HttpStatus.OK
    println("ZEB LOOK HERE")
    println(searchResult.body)
    println("ZEB LOOK HERE")
    println(searchResult.body.data)
    println(new JsonBuilder( searchResult.body.data ).toPrettyString())
    searchResult.body.data.size() == 1

println(new JsonBuilder( searchResult.body.data[0] ).toPrettyString())
when:
    def resultWithoutId = searchResult.body.data[0]
    resultWithoutId.id = null
    then:
    new JsonBuilder( resultWithoutId ).toPrettyString() == '''{
    "id": null,
    "type": "collection",
    "attributes": {
        "fileIdentifier": "gov.super.important:FILE-ID",
        "parentIdentifier": null,
        "doi": "doi:10.5072/FK2TEST",
        "title": "Important Organization's Important File's Super Important Title",
        "alternateTitle": "Still (But Slightly Less) Important Alternate Title",
        "description": "Wall of overly detailed, super informative, extra important text.",
        "keywords": [
            "SIO > Super Important Organization",
            "Air temperature",
            "Water temperature",
            "Wind speed",
            "Wind direction",
            "Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature",
            "Oceans > Salinity/Density > Salinity",
            "Volcanoes > This Keyword > Is Invalid",
            "Geographic Region > Arctic",
            "Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico",
            "Liquid Earth > This Keyword > Is Invalid"
        ],
        "topicCategories": [
            "environment",
            "oceans"
        ],
        "gcmdScience": [
            "Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature",
            "Atmosphere > Atmospheric Temperature > Surface Temperature",
            "Atmosphere > Atmospheric Temperature",
            "Atmosphere",
            "Oceans > Salinity/Density > Salinity",
            "Oceans > Salinity/Density",
            "Oceans",
            "Volcanoes > This Keyword > Is Invalid",
            "Volcanoes > This Keyword",
            "Volcanoes"
        ],
        "gcmdLocations": [
            "Geographic Region > Arctic",
            "Geographic Region",
            "Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico",
            "Ocean > Atlantic Ocean > North Atlantic Ocean",
            "Ocean > Atlantic Ocean",
            "Ocean",
            "Liquid Earth > This Keyword > Is Invalid",
            "Liquid Earth > This Keyword",
            "Liquid Earth"
        ],
        "gcmdInstruments": [

        ],
        "gcmdPlatforms": [

        ],
        "gcmdProjects": [

        ],
        "gcmdDataCenters": [
            "SIO > Super Important Organization"
        ],
        "gcmdDataResolution": [

        ],
        "temporalBounding": {
            "beginDate": "2005-05-09",
            "beginIndeterminate": null,
            "endDate": null,
            "endIndeterminate": "now",
            "instant": null,
            "instantIndeterminate": null
        },
        "spatialBounding": {
            "type": "Polygon",
            "coordinates": [
                [
                    [
                        -180.0,
                        -90.0
                    ],
                    [
                        180.0,
                        -90.0
                    ],
                    [
                        180.0,
                        90.0
                    ],
                    [
                        -180.0,
                        90.0
                    ],
                    [
                        -180.0,
                        -90.0
                    ]
                ]
            ]
        },
        "isGlobal": true,
        "acquisitionInstruments": [
            {
                "instrumentIdentifier": "SII > Super Important Instrument",
                "instrumentType": "sensor",
                "instrumentDescription": "The Super Important Organization's (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system."
            }
        ],
        "acquisitionOperations": [
            {
                "operationDescription": null,
                "operationIdentifier": "Super Important Project",
                "operationStatus": null,
                "operationType": null
            }
        ],
        "acquisitionPlatforms": [
            {
                "platformIdentifier": "TS-18 > TumbleSat-18",
                "platformDescription": "The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.",
                "platformSponsor": [
                    "Super Important Organization",
                    "Other (Kind Of) Important Organization"
                ]
            }
        ],
        "dataFormats": [
            "NETCDF",
            "CSV",
            "ASCII"
        ],
        "links": [
            {
                "linkName": "Super Important Access Link",
                "linkProtocol": "HTTP",
                "linkUrl": "http://www.example.com",
                "linkDescription": "Everything Important, All In One Place",
                "linkFunction": "search"
            }
        ],
        "contacts": [
            {
                "individualName": null,
                "organizationName": "Super Important Organization",
                "role": "custodian"
            },
            {
                "individualName": null,
                "organizationName": "Super Important Organization",
                "role": "publisher"
            },
            {
                "individualName": "John Smith",
                "organizationName": "University of Awesome",
                "role": "pointOfContact"
            },
            {
                "individualName": null,
                "organizationName": "Global Change Data Center, Science and Exploration Directorate, Goddard Space Flight Center (GSFC) National Aeronautics and Space Administration (NASA)",
                "role": "custodian"
            },
            {
                "individualName": null,
                "organizationName": "Super Important Organization",
                "role": "sponsor"
            },
            {
                "individualName": null,
                "organizationName": "Other (Kind Of) Important Organization",
                "role": "sponsor"
            }
        ],
        "thumbnail": "https://www.example.com/exportImage?soCool=yes&format=png",
        "modifiedDate": "2016-12-25T11:12:13",
        "creationDate": null,
        "revisionDate": "2011-01-02",
        "publicationDate": "2010-11-15",
        "dsmmAccessibility": 4,
        "dsmmDataIntegrity": 0,
        "dsmmDataQualityAssessment": 2,
        "dsmmDataQualityAssurance": 3,
        "dsmmDataQualityControlMonitoring": 1,
        "dsmmPreservability": 5,
        "dsmmProductionSustainability": 4,
        "dsmmTransparencyTraceability": 2,
        "dsmmUsability": 3,
        "dsmmAverage": 2.6666666667,
        "stagedDate": 1507841471906
    }
}'''
    // def coopsCollection = searchResult.body.data.find({ it.attributes.fileIdentifier == 'gov.noaa.nodc:NDBC-COOPS' })
    // coopsCollection?.id instanceof String
    //
    // when:
    // def granuleRequst = RequestEntity.post("${searchApiBase}/search".toURI())
    //     .contentType(MediaType.APPLICATION_JSON)
    //     .body('{"filters":[{"type":"collection", "values":["' + coopsCollection.id + '"]}]}')
    // def granuleResult = restTemplate.exchange(granuleRequst, Map)
    //
    // then:
    // granuleResult.statusCode == HttpStatus.OK
    // granuleResult.body.data.size() == 2
    //
    // cleanup:
    // def deleteRequest = RequestEntity.delete("${metadataApiBase}/metadata/${coopsCollection.id}".toURI()).build()
    // def deleteResult = restTemplate.exchange(deleteRequest, Map)

  }

}
