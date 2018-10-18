package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

@Unroll
class InventoryManagerToOneStopUtilTest extends Specification {
  def inputMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-iso.json').text
  def inputMap = [discovery: new JsonSlurper().parseText(inputMsg)] as Map
  def expectedResponsibleParties = [
      contacts  : [
          [
              individualName  : 'John Smith',
              organizationName: 'University of Boulder',
              positionName    : 'Chief Awesomeness Officer',
              role            : 'pointOfContact',
              email           : "NCEI.Info@noaa.gov",
              phone           : '555-555-5555'
          ]],
      creators  : [
          [
              individualName  : 'Edward M. Armstrong',
              organizationName: 'US NASA; Jet Propulsion Laboratory (JPL)',
              positionName    : null,
              role            : 'originator',
              email           : 'edward.m.armstrong@jpl.nasa.gov',
              phone           : '555-555-5559'
          ],
          [
              individualName  : 'Jarianna Whackositz',
              organizationName: 'Secret Underground Society',
              positionName    : 'Software Developer',
              role            : 'resourceProvider',
              email           : 'jw@mock-creator-email.org',
              phone           : '555-555-5558'
          ]],
      publishers: [
          [
              individualName  : null,
              organizationName: 'Super Important Organization',
              positionName    : null,
              role            : 'publisher',
              email           : 'email@sio.co',
              phone           : '555-123-4567'
          ]
      ]]
  
  def expectedKeywords = [
      keywords: [
          [
              "values" : [
                  "SIO > Super Important Organization",
                  "OSIO > Other Super Important Organization",
                  "SSIO > Super SIO (Super Important Organization)"
              ],
              type     : "dataCenter",
              namespace: "Global Change Master Directory (GCMD) Data Center Keywords"
          ], [
              "values"   : [
                  "EARTH SCIENCE > OCEANS > OCEAN TEMPERATURE > SEA SURFACE TEMPERATURE"
              ],
              "type"     : "theme",
              "namespace": "Global Change Master Directory (GCMD) Science and Services Keywords"
          ], [
              "values"   : [
                  "Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature",
                  "Oceans > Salinity/Density > Salinity",
                  "Volcanoes > This Keyword > Is Invalid",
                  "Spectral/Engineering > Microwave > Brightness Temperature",
                  "Spectral/Engineering > Microwave > Temperature Anomalies"
              ],
              "type"     : "theme",
              "namespace": "GCMD Keywords - Earth Science Keywords"
          ],
          [
              "values"   : [
                  "Geographic Region > Arctic",
                  "Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico",
                  "Liquid Earth > This Keyword > Is Invalid"
              ],
              "type"     : "place",
              "namespace": "GCMD Keywords - Locations"
          ],
          [
              "values"   : [
                  "Seasonal"
              ],
              "type"     : "dataResolution",
              "namespace": "Global Change Master Directory Keywords - Temporal Data Resolution"
          ],
          [
              "values"   : [
                  "> 1 Km"
              ],
              "type"     : "dataResolution",
              "namespace": "GCMD Keywords - Vertical Data Resolution"
          ]
      ]]
  
  def expectedGcmdKeywords = [
      gcmdScienceServices     : [
          'Oceans',
          'Oceans > Ocean Temperature',
          'Oceans > Ocean Temperature > Sea Surface Temperature'
      ],
      gcmdScience             : [
          'Atmosphere',
          'Atmosphere > Atmospheric Temperature',
          'Atmosphere > Atmospheric Temperature > Surface Temperature',
          'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
          'Oceans',
          'Oceans > Salinity/Density',
          'Oceans > Salinity/Density > Salinity',
          'Spectral/Engineering',
          'Spectral/Engineering > Microwave',
          'Spectral/Engineering > Microwave > Brightness Temperature',
          'Spectral/Engineering > Microwave > Temperature Anomalies',
          'Volcanoes',
          'Volcanoes > This Keyword',
          'Volcanoes > This Keyword > Is Invalid'
      ],
      gcmdLocations           : [
          'Geographic Region',
          'Geographic Region > Arctic',
          'Ocean',
          'Ocean > Atlantic Ocean',
          'Ocean > Atlantic Ocean > North Atlantic Ocean',
          'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
          'Liquid Earth',
          'Liquid Earth > This Keyword',
          'Liquid Earth > This Keyword > Is Invalid'
      ],
      gcmdInstruments         : [],
      gcmdPlatforms           : [],
      gcmdProjects            : [],
      gcmdDataCenters         : [
          'SIO > Super Important Organization',
          'OSIO > Other Super Important Organization',
          'SSIO > Super SIO (Super Important Organization)'
      ],
      gcmdHorizontalResolution: [],
      gcmdVerticalResolution  : ['> 1 Km'],
      gcmdTemporalResolution  : ['Seasonal']
  ]
  
  def "Create GCMD keyword lists" () {
    given:
    def gcmdKeywordMap = inputMap.discovery as Map
  
    when:
    Map parsedKeywords = InventoryManagerToOneStopUtil.createGcmdKeyword(gcmdKeywordMap)
    
    then:
    parsedKeywords.gcmdScienceServices == expectedGcmdKeywords.gcmdScienceServices as Set
    parsedKeywords.gcmdScience == expectedGcmdKeywords.gcmdScience as Set
    parsedKeywords.gcmdLocations == expectedGcmdKeywords.gcmdLocations  as Set
    parsedKeywords.gcmdInstruments == expectedGcmdKeywords.gcmdInstruments  as Set
    parsedKeywords.gcmdPlatforms == expectedGcmdKeywords.gcmdPlatforms  as Set
    parsedKeywords.gcmdProjects == expectedGcmdKeywords.gcmdProjects  as Set
    parsedKeywords.gcmdDataCenters == expectedGcmdKeywords.gcmdDataCenters  as Set
    parsedKeywords.gcmdHorizontalResolution == expectedGcmdKeywords.gcmdHorizontalResolution  as Set
    parsedKeywords.gcmdVerticalResolution == expectedGcmdKeywords.gcmdVerticalResolution  as Set
    parsedKeywords.gcmdTemporalResolution == expectedGcmdKeywords.gcmdTemporalResolution  as Set

    and: "should recreate keywords with out accession values"
    parsedKeywords.keywords.namespace != 'NCEI ACCESSION NUMBER'
    parsedKeywords.keywords.size() == expectedKeywords.keywords.size()
  }
  
  def "Create contacts, publishers and creators from responsibleParties" () {
    given:
    def partyMap = inputMap.discovery.responsibleParties as Map
    
    when:
    Map partiesMap = InventoryManagerToOneStopUtil.parseDataResponsibleParties(partyMap)
    
    then:
    partiesMap.contacts == expectedResponsibleParties.contacts as Set
    partiesMap.creators == expectedResponsibleParties.creators as Set
    partiesMap.publishers == expectedResponsibleParties.publishers as Set
  }

  def "When #situation, expected temporal bounding generated"() {
    given:
    def parsedTime = timeMetadata
    def analyzedTime = timeAnalysis

    when:
    def newTimeMetadata = InventoryManagerToOneStopUtil.readyDatesForSearch(parsedTime, analyzedTime)

    then:
    JsonOutput.toJson(newTimeMetadata) == JsonOutput.toJson(expectedResult)

    // Only include data that will be checked to cut down on size of below tables
    where:
    situation                                | timeMetadata                                                  | timeAnalysis                                                                                                                                                                 | expectedResult
    'instant with days precision'            | [beginDate: '', endDate: '', instant: '1999-12-31']           | [instant: [exists: true, precision: ChronoUnit.DAYS.toString(), validSearchFormat: true, utcDateTimeString: '1999-12-31T00:00:00Z'], range:[descriptor: 'INSTANT']]          | [beginDate: '1999-12-31T00:00:00Z', endDate: '1999-12-31T23:59:59Z', instant: '1999-12-31', beginYear: 1999, endYear: 1999]
    'non-paleo instant with years precision' | [beginDate: '', endDate: '', instant: '2000']                 | [instant: [exists: true, precision: ChronoUnit.YEARS.toString(), validSearchFormat: true, utcDateTimeString: '2000-01-01T00:00:00Z'], range:[descriptor: 'INSTANT']]         | [beginDate: '2000-01-01T00:00:00Z', endDate: '2000-12-31T23:59:59Z', instant: '2000', beginYear: 2000, endYear: 2000]
    'paleo instant'                          | [beginDate: '', endDate: '', instant: '-1000000000']          | [instant: [exists: true, precision: ChronoUnit.YEARS.toString(), validSearchFormat: false, utcDateTimeString: '-1000000000-01-01T00:00:00Z'], range:[descriptor: 'INSTANT']] | [beginDate: null, endDate: null, instant: '-1000000000', beginYear: -1000000000, endYear: -1000000000]
    'instant with nanos precision'           | [beginDate: '', endDate: '', instant: '2008-04-01T00:00:00Z'] | [instant: [exists: true, precision: ChronoUnit.NANOS.toString(), validSearchFormat: true, utcDateTimeString: '2008-04-01T00:00:00Z'], range:[descriptor: 'INSTANT']]         | [beginDate: '2008-04-01T00:00:00Z', endDate: '2008-04-01T00:00:00Z', instant: '2008-04-01T00:00:00Z', beginYear: 2008, endYear: 2008]
    'non-paleo bounded range'                | [beginDate: '1900-01-01', endDate: '2009']                    | [begin: [exists: true, precision: ChronoUnit.DAYS.toString(), validSearchFormat: true, utcDateTimeString: '1900-01-01T00:00:00Z'], end: [exists: true, precision: ChronoUnit.YEARS.toString(), validSearchFormat: true, utcDateTimeString: '2009-12-31T23:59:59Z'], range:[descriptor: 'BOUNDED']]                  | [beginDate: '1900-01-01T00:00:00Z', endDate: '2009-12-31T23:59:59Z', beginYear: 1900, endYear: 2009]
    'paleo bounded range'                    | [beginDate: '-2000000000', endDate: '-1000000000']            | [begin: [exists: true, precision: ChronoUnit.YEARS.toString(), validSearchFormat: false, utcDateTimeString: '-2000000000-01-01T00:00:00Z'], end: [exists: true, precision: ChronoUnit.YEARS.toString(), validSearchFormat: false, utcDateTimeString: '-1000000000-12-31T23:59:59Z'], range:[descriptor: 'BOUNDED']] | [beginDate: null, endDate: null, beginYear: -2000000000, endYear: -1000000000]
    'ongoing range'                          | [beginDate: '1975-06-15T12:30:00Z', endDate: '']              | [begin: [exists: true, precision: ChronoUnit.NANOS.toString(), validSearchFormat: true, utcDateTimeString: '1975-06-15T12:30:00Z'], end: [exists: false, precision: 'UNDEFINED', validSearchFormat: 'UNDEFINED', utcDateTimeString: 'UNDEFINED'], range:[descriptor: 'ONGOING']]                                    | [beginDate: '1975-06-15T12:30:00Z', endDate: null, beginYear: 1975, endYear: null]
    'undefined range'                        | [beginDate: null, endDate: null]                              | [begin: [exists: false, validSearchFormat: 'UNDEFINED', utcDateTimeString: 'UNDEFINED'], end: [exists: false, validSearchFormat: 'UNDEFINED', utcDateTimeString: 'UNDEFINED'], range:[descriptor: 'UNDEFINED']]                                                                                                     | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }
  
  def "new record is ready for onestop" () {
    given:
    def analysisMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-analysis.json').text
    def analysisMap = new JsonSlurper().parseText(analysisMsg) as Map
    def discovery = inputMap.discovery as Map
    def analysis = analysisMap as Map
    def expectedMap = inputMap.discovery as Map
    
    when:
    def metadata = InventoryManagerToOneStopUtil.reformatMessageForSearch(discovery, analysis)
    expectedMap.remove("keywords")
    expectedMap.remove("services")
    expectedMap.remove("responsibleParties")
    expectedMap << expectedResponsibleParties << expectedGcmdKeywords << expectedKeywords
  
    then:
    metadata == expectedMap
    
    and: "drop service and responsibleParties"
    metadata.services == null
    metadata.responsibleParties == null
  }
}
