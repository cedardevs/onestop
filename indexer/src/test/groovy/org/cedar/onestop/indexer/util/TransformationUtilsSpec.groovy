package org.cedar.onestop.indexer.util

import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.analyze.Temporal
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import org.cedar.schemas.avro.psi.Checksum
import org.cedar.schemas.avro.psi.ChecksumAlgorithm
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.FileInformation
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.psi.TemporalBounding
import java.time.temporal.ChronoUnit
import spock.lang.Specification
import spock.lang.Unroll

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.cedar.schemas.avro.util.AvroUtils

import static org.cedar.schemas.avro.util.TemporalTestData.getSituations

import org.cedar.onestop.kafka.common.util.DataUtils;

@Unroll
class TransformationUtilsSpec extends Specification {

  static Set<String> collectionSearchFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet()
  static Set<String> granuleSearchFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet()
  static Set<String> granuleAnalysisErrorFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS).keySet()
  static Set<String> collectionAnalysisErrorFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS).keySet()

  static expectedKeywords = [
      "SIO > Super Important Organization",
      "OSIO > Other Super Important Organization",
      "SSIO > Super SIO (Super Important Organization)",
      "EARTH SCIENCE > OCEANS > OCEAN TEMPERATURE > SEA SURFACE TEMPERATURE",
      "Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature",
      "Oceans > Salinity/Density > Salinity",
      "Volcanoes > This Keyword > Is Invalid",
      "Spectral/Engineering > Microwave > Brightness Temperature",
      "Spectral/Engineering > Microwave > Temperature Anomalies",
      "Geographic Region > Arctic",
      "Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico",
      "Liquid Earth > This Keyword > Is Invalid",
      "Seasonal",
      "> 1 Km"
  ] as Set

  static expectedGcmdKeywords = [
      gcmdScienceServices     : null,
      gcmdScience             : [
          'Oceans',
          'Oceans > Ocean Temperature',
          'Oceans > Ocean Temperature > Sea Surface Temperature'
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
      gcmdInstruments         : null,
      gcmdPlatforms           : null,
      gcmdProjects            : null,
      gcmdDataCenters         : [
          'SIO > Super Important Organization',
          'OSIO > Other Super Important Organization',
          'SSIO > Super SIO (Super Important Organization)'
      ] as Set,
      gcmdHorizontalResolution: null,
      gcmdVerticalResolution  : ['> 1 Km'] as Set,
      gcmdTemporalResolution  : ['Seasonal'] as Set
  ]

  ///////////////////////////////
  // Generic Indexed Fields    //
  ///////////////////////////////

  def "reformatMessageForAnalysis populates with correct fields for #label"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
      .setFileInformation(
        FileInformation.newBuilder()
        .setChecksums(
          [
          Checksum.newBuilder()
          .setAlgorithm(ChecksumAlgorithm.MD5)
          .setValue('abc')
          .build()
          ]
        ).build()
      )
      .setAnalysis(
        Analysis.newBuilder().setTemporalBounding(
        TemporalBoundingAnalysis.newBuilder()
            .setBeginDescriptor(ValidDescriptor.VALID)
            .setBeginIndexable(true)
            .setBeginPrecision(ChronoUnit.DAYS.toString())
            .setBeginZoneSpecified(null)
            .setBeginUtcDateTimeString("2000-02-01")
            .setBeginYear(2000)
            .setBeginMonth(2)
            .setBeginDayOfYear(32)
            .setBeginDayOfMonth(1)
            .build()
          ).build()
        )
      .build()

    def indexedRecord = TransformationUtils.reformatMessageForAnalysis(record, fields, RecordType.granule)

    then:

    println(label)
    println(JsonOutput.toJson(AvroUtils.avroToMap(record.getAnalysis(), true)))
    println(JsonOutput.toJson(indexedRecord))
    indexedRecord.keySet().contains("checksums") == shouldIncludeChecksums
    indexedRecord.keySet().contains("internalParentIdentifier") == shouldIncludeParentIdentifier
    (indexedRecord.keySet().contains("temporalBounding") && indexedRecord.get("temporalBounding").keySet().contains("beginMonth")) == false
    (indexedRecord.keySet().contains("temporalBounding") && indexedRecord.get("temporalBounding").keySet().contains("beginIndexable")) == shouldIncludeTemporalAnalysis

    where:
    label | fields | shouldIncludeChecksums | shouldIncludeTemporalAnalysis | shouldIncludeParentIdentifier
    'analysis and errors collections' | collectionAnalysisErrorFields | false | true  | false
    'analysis and errors granules'    | granuleAnalysisErrorFields    | false | true  | true

  }


  def "reformatMessageForSearch populates with correct fields for #label"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
      .setFileInformation(
        FileInformation.newBuilder()
        .setChecksums(
          [
          Checksum.newBuilder()
          .setAlgorithm(ChecksumAlgorithm.MD5)
          .setValue('abc')
          .build()
          ]
        ).build()
      )
      .setAnalysis(
        Analysis.newBuilder().setTemporalBounding(
        TemporalBoundingAnalysis.newBuilder()
            .setBeginDescriptor(ValidDescriptor.VALID)
            .setBeginIndexable(true)
            .setBeginPrecision(ChronoUnit.DAYS.toString())
            .setBeginZoneSpecified(null)
            .setBeginUtcDateTimeString("2000-02-01")
            .setBeginYear(2000)
            .setBeginMonth(2)
            .setBeginDayOfYear(32)
            .setBeginDayOfMonth(1)
            .build()
          ).build()
        )
      .build()

    def indexedRecord = TransformationUtils.reformatMessageForSearch(record, fields)

    then:

    println(label)
    println(JsonOutput.toJson(AvroUtils.avroToMap(record.getAnalysis(), true)))
    println(JsonOutput.toJson(indexedRecord))
    indexedRecord.keySet().contains("checksums") == shouldIncludeChecksums
    indexedRecord.keySet().contains("internalParentIdentifier") == shouldIncludeParentIdentifier
    (indexedRecord.keySet().contains("temporalBounding") && indexedRecord.get("temporalBounding").keySet().contains("beginMonth")) == false
    (indexedRecord.keySet().contains("temporalBounding") && indexedRecord.get("temporalBounding").keySet().contains("beginIndexable")) == shouldIncludeTemporalAnalysis

    where:
    label | fields | shouldIncludeChecksums | shouldIncludeTemporalAnalysis | shouldIncludeParentIdentifier
    'search collections'              | collectionSearchFields        | false | false | false
    'search granules'                 | granuleSearchFields           | true  | false | true
  }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////
  def "produces internalParentIdentifier for collection record correctly"() {
    expect:
    TransformationUtils.prepareInternalParentIdentifier(TestUtils.inputAvroRecord) == null
  }

  def "produces internalParentIdentifier for granule record correctly"() {
    def testId = "ABC"
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .setRelationships([
            Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId(testId).build()
        ])
        .build()

    expect:
    TransformationUtils.prepareInternalParentIdentifier(record) == testId
  }

  def "produces filename for collection record correctly"() {
    expect:
    TransformationUtils.prepareFilename(TestUtils.inputAvroRecord) == null
  }

  def "produces filename for granule record correctly when record has data"() {
    def filename = "ABC"
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .setFileInformation(FileInformation.newBuilder().setName(filename).build())
        .build()

    expect:
    TransformationUtils.prepareFilename(record) == filename
  }

  def "produces filename for granule record correctly when record does not have data"() {
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
        .setType(RecordType.granule)
        .build()

    expect:
    TransformationUtils.prepareFilename(record) == null
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  def "prepares service links"() {
    when:
    def discovery = TestUtils.inputGranuleRecord.discovery
    def result = TransformationUtils.prepareServiceLinks(discovery)

    then:
    result.size() == 1
    result[0].title == "Multibeam Bathymetric Surveys ArcGIS Map Service"
    result[0].alternateTitle == "Alternate Title for Testing"
    result[0].description == "NOAA's National Centers for Environmental Information (NCEI) is the U.S. national archive for multibeam bathymetric data and presently holds over 2400 surveys received from sources worldwide, including the U.S. academic fleet via the Rolling Deck to Repository (R2R) program. In addition to deep-water data, the multibeam database also includes hydrographic multibeam survey data from the National Ocean Service (NOS). This map service shows navigation for multibeam bathymetric surveys in NCEI's archive. Older surveys are colored orange, and more recent recent surveys are green."
    result[0].links as Set == [
        [
            linkProtocol   : 'http',
            linkUrl        : 'https://maps.ngdc.noaa.gov/arcgis/services/web_mercator/multibeam_dynamic/MapServer/WMSServer?request=GetCapabilities&service=WMS',
            linkName       : 'Multibeam Bathymetric Surveys Web Map Service (WMS)',
            linkDescription: 'The Multibeam Bathymetric Surveys ArcGIS cached map service provides rapid display of ship tracks from global scales down to zoom level 9 (approx. 1:1,200,000 scale).',
            linkFunction   : 'search'
        ],
        [
            linkProtocol   : 'http',
            linkUrl        : 'https://maps.ngdc.noaa.gov/arcgis/rest/services/web_mercator/multibeam/MapServer',
            linkName       : 'Multibeam Bathymetric Surveys ArcGIS Cached Map Service',
            linkDescription: 'Capabilities document for Open Geospatial Consortium Web Map Service for Multibeam Bathymetric Surveys',
            linkFunction   : 'search'
        ]
    ] as Set
  }

  def "prepares service link protocols"() {
    Set protocols = ['HTTP']
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareServiceLinkProtocols(discovery) == protocols
  }

  def "prepares link protocols"() {
    Set protocols = ['HTTP']
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareLinkProtocols(discovery) == protocols
  }

  ////////////////////////////
  // Data Formats           //
  ////////////////////////////
  def "prepares data formats"() {
    def discovery = TestUtils.inputGranuleRecord.discovery

    expect:
    TransformationUtils.prepareDataFormats(discovery) == [
        "ASCII",
        "CSV",
        "NETCDF",
        "NETCDF > 4",
        "NETCDF > CLASSIC",
    ] as Set
  }

  ////////////////////////////
  // Responsible Parties    //
  ////////////////////////////
  def "prepares responsible party names"() {
    when:
    def record = TestUtils.inputCollectionRecord
    def result = TransformationUtils.prepareResponsibleParties(record)

    then:
    result.individualNames == [
        'John Smith',
        'Jane Doe',
        'Jarianna Whackositz',
        'Dr. Quinn McClojure Man',
        'Zebulon Pike',
        'Little Rhinoceros',
        'Skeletor McSkittles',
    ] as Set
    result.organizationNames == [
        'University of Awesome',
        'Secret Underground Society',
        'Soap Boxes Inc.',
        'Pikes Peak Inc.',
        'Alien Infested Spider Monkey Rescue',
        'The Underworld',
        'Super Important Organization',
    ] as Set
  }

  def "does not prepare responsible party names for granules"() {
    when:
    def record = TestUtils.inputGranuleRecord
    def result = TransformationUtils.prepareResponsibleParties(record)

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  def "party names are not included in granule search info"() {
    when:
    def record = TestUtils.inputGranuleRecord // <-- granule!
    def result = TransformationUtils.reformatMessageForSearch(record, collectionSearchFields) // <-- top level reformat method!

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  ////////////////////////////
  // Dates                  //
  ////////////////////////////
  def "when #label, expected temporal bounding generated"() {
    when:
    def discovery = Discovery.newBuilder().setTemporalBounding(input).build()
    def newTimeMetadata = TransformationUtils.prepareDates(input, Temporal.analyzeBounding(discovery))
    println("debug"+label)
    println(Temporal.analyzeBounding(discovery))
    then:
    newTimeMetadata.beginDate == beginDate
    newTimeMetadata.beginYear == beginYear
    newTimeMetadata.beginDayOfYear == beginDayOfYear
    newTimeMetadata.beginDayOfMonth == beginDayOfMonth
    newTimeMetadata.beginMonth == beginMonth
    newTimeMetadata.endDate == endDate
    newTimeMetadata.endYear == endYear
    newTimeMetadata.endDayOfYear == endDayOfYear
    newTimeMetadata.endDayOfMonth == endDayOfMonth
    newTimeMetadata.endMonth == endMonth

    where:
    label | input | beginDate | beginYear | beginDayOfYear | beginDayOfMonth | beginMonth | endDate | endYear | endDayOfYear | endDayOfMonth | endMonth

    "undefined range" | TemporalBounding.newBuilder().build() | null | null | null | null | null | null | null | null | null | null
    "non-paleo bounded range with day and year precision" | TemporalBounding.newBuilder().setBeginDate('1900-01-01').setEndDate('2009').build() | '1900-01-01T00:00:00Z' | 1900 | 1 | 1 | 1 | '2009-12-31T23:59:59.999Z' | 2009 | 365 | 31 | 12 // TODO does this assumption re end date make sense, really? TODO had to add the .999 to endDate - why and is that good/bad/other?
    "paleo bounded range" | TemporalBounding.newBuilder().setBeginDate('-2000000000').setEndDate('-1000000000').build() | null | -2000000000 | null | null | null | null | -1000000000 | null | null | null
    "ongoing range with second precision for begin" | TemporalBounding.newBuilder().setBeginDate('1975-06-15T12:30:00Z').build() | "1975-06-15T12:30:00Z" | 1975 | 166 | 15 | 6 | null | null | null | null | null
    // INSTANTS:
    "non-paleo instant with years precision" | TemporalBounding.newBuilder().setInstant('1999').build() | '1999-01-01T00:00:00Z' | 1999 | 1 | 1 | 1 | '1999-12-31T23:59:59Z' | 1999 | 365 | 31 | 12
    "non-paleo instant with days precision" | TemporalBounding.newBuilder().setInstant('1999-12-31').build() | '1999-12-31T00:00:00Z' | 1999 | 365 | 31 | 12 | '1999-12-31T23:59:59Z' | 1999 | 365 | 31 | 12
    "paleo instant with years precision" | TemporalBounding.newBuilder().setInstant('-1000000000').build() | null | -1000000000 | null | null | null | null | -1000000000 | null | null | null // TODO I think this is a bug in analysis, that it doesn't populate instantYears
    "non-paleo instant with nanos precision" | TemporalBounding.newBuilder().setInstant('2008-04-01T00:00:00Z').build() | '2008-04-01T00:00:00Z' | 2008 | 92 | 1 | 4 | '2008-04-01T00:00:00Z' | 2008 | 92 | 1 | 4
  }

  // def "When #situation.description, expected temporal bounding generated"() {
  //   when:
  //   def discovery = Discovery.newBuilder().setTemporalBounding(situation.bounding).build()
  //   def newTimeMetadata = TransformationUtils.prepareDates(situation.bounding, Temporal.analyzeBounding(discovery))
  //
  //   then:
  //   newTimeMetadata.sort() == expectedResult
  //
  //   where:
  //   situation               | expectedResult
  //   // situations.instantDay   | [beginDate: '1999-12-31T00:00:00Z', beginYear: 1999, beginDayOfYear: 365, beginDayOfMonth: 31, beginMonth: 12, endDate: '1999-12-31T23:59:59Z', endYear: 1999, endDayOfYear:365, endDayOfMonth:31, endMonth:12].sort()
  //   // situations.instantYear  | [beginDate: '1999-01-01T00:00:00Z', beginYear: 1999, beginDayOfYear: 1, beginDayOfMonth:1, beginMonth: 1, endDate: '1999-12-31T23:59:59Z', endYear: 1999, endDayOfMonth:31, endDayOfYear:365, endMonth:12].sort()
  //   // situations.instantPaleo | [beginDate: null, endDate: null, beginYear: -1000000000, endYear: -1000000000, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
  //   // situations.instantNano  | [beginDate: '2008-04-01T00:00:00Z', beginYear: 2008, beginDayOfYear: 92, beginDayOfMonth:1, beginMonth: 4, endDate: '2008-04-01T00:00:00Z', endYear: 2008,  endDayOfYear: 92, endDayOfMonth:1, endMonth:4].sort()
  //   // situations.bounded      | [beginDate: '1900-01-01T00:00:00Z',  beginYear: 1900, beginDayOfYear: 1, beginDayOfMonth:1, beginMonth: 1, endDate: '2009-12-31T23:59:59Z', endYear: 2009, endDayOfYear:365, endDayOfMonth:31, endMonth:12].sort() // TODO does this assumption re end date make sense, really?
  //   // situations.paleoBounded | [beginDate: null, endDate: null, beginYear: -2000000000, endYear: -1000000000, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
  //   // situations.ongoing      | [beginDate: "1975-06-15T12:30:00Z", beginDayOfMonth:15, beginDayOfYear:166, beginMonth:6, beginYear:1975, endDate:null, endYear:null, endDayOfYear: null, endDayOfMonth: null, endMonth: null].sort()
  //   situations.empty        | [beginDate: null, endDate: null, beginYear: null, endYear: null, beginDayOfYear: null, beginDayOfMonth:null, beginMonth: null, endDayOfYear: null, endDayOfMonth:null, endMonth:null].sort()
  // }

  def "temporal bounding with #testCase dates is prepared correctly"() {
    given:
    def bounding = TemporalBounding.newBuilder().setBeginDate(begin).setEndDate(end).build()
    def analysis = Temporal.analyzeBounding(Discovery.newBuilder().setTemporalBounding(bounding).build())

    when:
    def result = TransformationUtils.prepareDates(bounding, analysis)

    then:
    expected.forEach({ k, v ->
      assert result.get(k) == v
    })

    where:
    testCase      | begin                  | end                     | expected
    'typical'     | '2005-05-09T00:00:00Z' | '2010-10-01'            | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T23:59:59.999Z', beginYear: 2005, endYear: 2010]
    'no timezone' | '2005-05-09T00:00:00'  | '2010-10-01T00:00:00'   | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T00:00:00Z', beginYear: 2005, endYear: 2010]
    'paleo'       | '-100000001'           | '-1601050'              | [beginDate: null, endDate: '-1601050-12-31T23:59:59.999Z', beginYear: -100000001, endYear: -1601050]
    'invalid'     | '1984-04-31'           | '1985-505-09T00:00:00Z' | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  def "Create GCMD keyword lists"() {
    when:
    Map parsedKeywords = TransformationUtils.prepareGcmdKeyword(TestUtils.inputAvroRecord.discovery)

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

    and: "should recreate keywords without accession values"
    parsedKeywords.keywords.size() == expectedKeywords.size()
  }

  def "science keywords are parsed as expected from iso"() {
    def expectedKeywordsFromIso = [
        science       : [
            'Atmosphere > Atmospheric Pressure',
            'Atmosphere',
            'Atmosphere > Atmospheric Temperature',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity > Relative Humidity',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators > Humidity',
            'Atmosphere > Atmospheric Water Vapor > Water Vapor Indicators',
            'Atmosphere > Atmospheric Water Vapor',
            'Atmosphere > Atmospheric Winds > Surface Winds > Wind Direction',
            'Atmosphere > Atmospheric Winds > Surface Winds',
            'Atmosphere > Atmospheric Winds',
            'Atmosphere > Atmospheric Winds > Surface Winds > Wind Speed',
            'Oceans > Bathymetry/Seafloor Topography > Seafloor Topography',
            'Oceans > Bathymetry/Seafloor Topography',
            'Oceans',
            'Oceans > Bathymetry/Seafloor Topography > Bathymetry',
            'Oceans > Bathymetry/Seafloor Topography > Water Depth',
            'Land Surface > Topography > Terrain Elevation',
            'Land Surface > Topography',
            'Land Surface',
            'Land Surface > Topography > Topographical Relief Maps',
            'Oceans > Coastal Processes > Coastal Elevation',
            'Oceans > Coastal Processes'
        ] as Set,
        scienceService: [
            'Data Analysis And Visualization > Calibration/Validation > Calibration',
            'Data Analysis And Visualization > Calibration/Validation',
            'Data Analysis And Visualization'
        ] as Set
    ]

    when:
    def discovery = TestUtils.inputCollectionRecord.discovery
    def parsedKeywords = TransformationUtils.prepareGcmdKeyword(discovery)

    then:
    parsedKeywords.gcmdScience == expectedKeywordsFromIso.science
    parsedKeywords.gcmdScienceServices == expectedKeywordsFromIso.scienceService
  }

  def "accession values are not included"() {
    when:
    def result = TransformationUtils.reformatMessageForSearch(TestUtils.inputAvroRecord, collectionSearchFields)

    then:
    result.accessionValues == null
  }
}
