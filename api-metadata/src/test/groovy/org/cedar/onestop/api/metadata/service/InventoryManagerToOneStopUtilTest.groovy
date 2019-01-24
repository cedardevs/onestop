package org.cedar.onestop.api.metadata.service

import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.util.TemporalTestData.situations

@Unroll
class InventoryManagerToOneStopUtilTest extends Specification {

  def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
  def inputRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)

  def expectedResponsibleParties = [
      contacts  : [
          [
              individualName  : 'John Smith',
              organizationName: 'University of Boulder',
              positionName    : 'Chief Awesomeness Officer',
              role            : 'pointOfContact',
              email           : "NCEI.Info@noaa.gov",
              phone           : '555-555-5555'
          ]] as Set,
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
          ]] as Set,
      publishers: [
          [
              individualName  : null,
              organizationName: 'Super Important Organization',
              positionName    : null,
              role            : 'publisher',
              email           : 'email@sio.co',
              phone           : '555-123-4567'
          ]
      ] as Set]

  def expectedKeywords = [
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
          "namespace": "GCMD Keywords - Earth science services Keywords"
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
  ]

  def expectedGcmdKeywords = [
      gcmdScienceServices     : [
          'Oceans',
          'Oceans > Ocean Temperature',
          'Oceans > Ocean Temperature > Sea Surface Temperature'
      ] as Set,
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
      ] as Set,
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
      ] as Set,
      gcmdInstruments         : [] as Set,
      gcmdPlatforms           : [] as Set,
      gcmdProjects            : [] as Set,
      gcmdDataCenters         : [
          'SIO > Super Important Organization',
          'OSIO > Other Super Important Organization',
          'SSIO > Super SIO (Super Important Organization)'
      ] as Set,
      gcmdHorizontalResolution: [] as Set,
      gcmdVerticalResolution  : ['> 1 Km'] as Set,
      gcmdTemporalResolution  : ['Seasonal'] as Set
  ]

  def expectedTemporalBounding = [
      beginDate: '2002-06-01T00:00:00Z',
      beginYear: 2002,
      endDate  : '2011-10-04T23:59:59Z',
      endYear  : 2011
  ]

  def "Create GCMD keyword lists"() {
    when:
    Map parsedKeywords = InventoryManagerToOneStopUtil.createGcmdKeyword(inputRecord.discovery)

    then:
    parsedKeywords.gcmdScienceServices == expectedGcmdKeywords.gcmdScienceServices
    parsedKeywords.gcmdScience == expectedGcmdKeywords.gcmdScience
    parsedKeywords.gcmdLocations == expectedGcmdKeywords.gcmdLocations
    parsedKeywords.gcmdInstruments == expectedGcmdKeywords.gcmdInstruments
    parsedKeywords.gcmdPlatforms == expectedGcmdKeywords.gcmdPlatforms
    parsedKeywords.gcmdProjects == expectedGcmdKeywords.gcmdProjects
    parsedKeywords.gcmdDataCenters == expectedGcmdKeywords.gcmdDataCenters
    parsedKeywords.gcmdHorizontalResolution == expectedGcmdKeywords.gcmdHorizontalResolution
    parsedKeywords.gcmdVerticalResolution == expectedGcmdKeywords.gcmdVerticalResolution
    parsedKeywords.gcmdTemporalResolution == expectedGcmdKeywords.gcmdTemporalResolution

    and: "should recreate keywords with out accession values"
    parsedKeywords.keywords.namespace.every { it != 'NCEI ACCESSION NUMBER' }
    parsedKeywords.keywords.size() == expectedKeywords.size()
  }

  def "Create contacts, publishers and creators from responsibleParties"() {
    when:
    Map partiesMap = InventoryManagerToOneStopUtil.parseResponsibleParties(inputRecord.discovery.responsibleParties)

    then:
    partiesMap.contacts == expectedResponsibleParties.contacts
    partiesMap.creators == expectedResponsibleParties.creators
    partiesMap.publishers == expectedResponsibleParties.publishers
  }

  def "When #situation.description, expected temporal bounding generated"() {
    when:
    def newTimeMetadata = InventoryManagerToOneStopUtil.readyDatesForSearch(situation.bounding, situation.analysis)

    then:
    newTimeMetadata == expectedResult

    // Only include data that will be checked to cut down on size of below tables
    where:
    situation                | expectedResult
    situations.instantDay   | [beginDate: '1999-12-31T00:00:00Z', endDate: '1999-12-31T23:59:59Z', beginYear: 1999, endYear: 1999]
    situations.instantYear  | [beginDate: '1999-01-01T00:00:00Z', endDate: '1999-12-31T23:59:59Z', beginYear: 1999, endYear: 1999]
    situations.instantPaleo | [beginDate: null, endDate: null, beginYear: -1000000000, endYear: -1000000000]
    situations.instantNano  | [beginDate: '2008-04-01T00:00:00Z', endDate: '2008-04-01T00:00:00Z', beginYear: 2008, endYear: 2008]
    situations.bounded      | [beginDate: '1900-01-01T00:00:00Z', endDate: '2009-12-31T23:59:59Z', beginYear: 1900, endYear: 2009]
    situations.paleoBounded | [beginDate: null, endDate: null, beginYear: -2000000000, endYear: -1000000000]
    situations.ongoing      | [beginDate: '1975-06-15T12:30:00Z', endDate: null, beginYear: 1975, endYear: null]
    situations.empty        | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }

  def "new record is ready for onestop"() {
    when:
    def result = InventoryManagerToOneStopUtil.reformatMessageForSearch(inputRecord)

    then:
    result.services == []
    result.accessionValues == []

    result.temporalBounding == expectedTemporalBounding

    result.keywords == expectedKeywords
    expectedGcmdKeywords.each { k, v ->
      assert result[k] == v
    }

    result.responsibleParties == null
    expectedResponsibleParties.each { k, v ->
      assert result[k] == v
    }
  }

  def "valid message passes validation check"() {
    expect:
    InventoryManagerToOneStopUtil.validateMessage('dummy id', inputRecord)
  }

  def "invalid message fails validation check"() {
    given:
    def titleAnalysis = TitleAnalysis.newBuilder(inputRecord.analysis.titles)
        .setTitleExists(false)
        .build()
    def idAnalysis = IdentificationAnalysis.newBuilder(inputRecord.analysis.identification)
        .setFileIdentifierExists(false)
        .setParentIdentifierExists(false)
        .build()
    def timeAnalysis = TemporalBoundingAnalysis.newBuilder(inputRecord.analysis.temporalBounding)
        .setBeginDescriptor(ValidDescriptor.INVALID)
        .setBeginUtcDateTimeString(null)
        .setEndDescriptor(ValidDescriptor.INVALID)
        .setEndUtcDateTimeString(null)
        .setInstantDescriptor(ValidDescriptor.UNDEFINED)
        .setInstantUtcDateTimeString(null)
        .build()
    def analysis = Analysis.newBuilder(inputRecord.analysis)
        .setTitles(titleAnalysis)
        .setIdentification(idAnalysis)
        .setTemporalBounding(timeAnalysis)
        .build()
    def record = ParsedRecord.newBuilder(inputRecord)
        .setAnalysis(analysis)
        .build()

    expect:
    !InventoryManagerToOneStopUtil.validateMessage('dummy id', record)
  }
}
