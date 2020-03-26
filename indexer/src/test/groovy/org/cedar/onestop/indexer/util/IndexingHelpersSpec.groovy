package org.cedar.onestop.indexer.util


import org.apache.kafka.streams.state.ValueAndTimestamp
import org.cedar.onestop.elastic.common.ElasticsearchConfig
import org.cedar.onestop.elastic.common.ElasticsearchVersion
import org.cedar.onestop.indexer.stream.BulkIndexingConfig
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.parse.ISOParser
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

import static org.cedar.schemas.avro.psi.ValidDescriptor.INVALID
import static org.cedar.schemas.avro.psi.ValidDescriptor.VALID
import static org.cedar.schemas.avro.util.TemporalTestData.getSituations
import static org.elasticsearch.action.DocWriteRequest.OpType.DELETE
import static org.elasticsearch.action.DocWriteRequest.OpType.INDEX

@Unroll
class IndexingHelpersSpec extends Specification {

  ////////////////////////////
  // Shared Values          //
  ////////////////////////////
  // from schemas repo
  static inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
  static inputRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)

  static inputCollectionXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-collection.xml').text
  static inputCollectionDiscovery = ISOParser.parseXMLMetadataToDiscovery(inputCollectionXml)
  static inputCollectionAnalysis = Analyzers.analyze(inputCollectionDiscovery)
  static inputGranuleXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-granule.xml').text

  static testTopic = 'testtopic'
  static testIndex = 'testindex'
  static indexingConfig = BulkIndexingConfig.newBuilder()
      .withStoreName('teststore')
      .withMaxPublishInterval(Duration.ofSeconds(10))
      .withMaxPublishBytes(10000)
      .addIndexMapping(testTopic, INDEX, testIndex)
      .addIndexMapping(testTopic, DELETE, testIndex)
      .build()
  static esConfig = new ElasticsearchConfig(
      new ElasticsearchVersion("7.5.1"),
      "SearchIndexTopologySpec-",
      1,
      1,
      1,
      1,
      false
  )
  static collectionFields = esConfig.indexedProperties(esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet()
  static granuleFields = esConfig.indexedProperties(esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet()

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

  ////////////////////////////
  // Validate Message Tests //
  ////////////////////////////
  def "valid message passes validation check"() {
    expect:
    IndexingHelpers.addValidationErrors(inputRecord).errors.isEmpty()
  }

  def "validation passes tombstones through"() {
    expect:
    IndexingHelpers.addValidationErrors(null) == null
  }

  def "invalid records have errors added"() {
    given:
    def titleAnalysis = TitleAnalysis.newBuilder(inputRecord.analysis.titles)
        .setTitleExists(false)
        .build()
    def idAnalysis = IdentificationAnalysis.newBuilder(inputRecord.analysis.identification)
        .setFileIdentifierExists(false)
        .setParentIdentifierExists(false)
        .build()
    def timeAnalysis = TemporalBoundingAnalysis.newBuilder(inputRecord.analysis.temporalBounding)
        .setBeginDescriptor(INVALID)
        .setBeginUtcDateTimeString(null)
        .setEndDescriptor(INVALID)
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

    when:
    def validated = IndexingHelpers.addValidationErrors(record)

    then:
    !validated.errors.isEmpty()
  }

  def "validates titles when #testCase"() {
    def titleAnalysis = TitleAnalysis.newBuilder(inputRecord.analysis.titles).setTitleExists(titleExists).build()
    def analysis = Analysis.newBuilder(inputRecord.analysis).setTitles(titleAnalysis).build()
    def record = ParsedRecord.newBuilder(inputRecord).setAnalysis(analysis).build()

    when:
    def validated = IndexingHelpers.addValidationErrors(record)

    then:
    validated.errors.isEmpty() == isValid

    where:
    testCase                | isValid | titleExists
    "title is missing"      | false   | false
    "title is not missing"  | true    | true
  }

  def "validates identification when #testCase"() {
    def identificationAnalysis = IdentificationAnalysis.newBuilder(inputRecord.analysis.identification)
        .setFileIdentifierExists(hasFileId)
        .setDoiExists(hasDoi)
        .setMatchesIdentifiers(matches)
        .build()
    def analysis = Analysis.newBuilder(inputRecord.analysis).setIdentification(identificationAnalysis).build()
    def record = ParsedRecord.newBuilder(inputRecord).setAnalysis(analysis).build()

    when:
    def validated = IndexingHelpers.addValidationErrors(record)

    then:
    validated.errors.size() == errors

    where:
    testCase                | errors  | hasFileId | hasDoi  | matches
    "has only fileId"       | 0       | true      | false   | true
    "has only doi"          | 0       | false     | true    | true
    "has no fileId nor doi" | 1       | false     | false   | true
    "has mismatched type"   | 1       | true      | true    | false
    "no id and mismatched"  | 2       | false     | false   | false
  }

  def "validates temporal bounds when #testCase"() {
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder(inputRecord.analysis.temporalBounding)
        .setBeginDescriptor(beginValid ? VALID : INVALID)
        .setEndDescriptor(endValid ? VALID : INVALID)
        .setInstantDescriptor(instantValid ? VALID : INVALID)
        .build()
    def analysis = Analysis.newBuilder(inputRecord.analysis).setTemporalBounding(temporalAnalysis).build()
    def record = ParsedRecord.newBuilder(inputRecord).setAnalysis(analysis).build()

    when:
    def validated = IndexingHelpers.addValidationErrors(record)

    then:
    validated.errors.size() == errors

    where:
    testCase                    | errors  | beginValid| endValid| instantValid
    "has valid bounds"          | 0       | true      | true    | true
    "has invalid start"         | 1       | false     | true    | true
    "has invalid end"           | 1       | true      | false   | true
    "has invalid start and end" | 2       | false     | false   | true
    "is invalid instant"        | 1       | true      | true    | false
    "is completely invalid"     | 3       | false     | false   | false
  }

  def "validates spatial bounds when #testCase"() {
    def spatialAnalysis = SpatialBoundingAnalysis.newBuilder(inputRecord.analysis.spatialBounding)
        .setSpatialBoundingExists(exists)
        .setIsValid(valid)
        .build()
    def analysis = Analysis.newBuilder(inputRecord.analysis).setSpatialBounding(spatialAnalysis).build()
    def record = ParsedRecord.newBuilder(inputRecord).setAnalysis(analysis).build()

    when:
    def validated = IndexingHelpers.addValidationErrors(record)

    then:
    validated.errors.size() == errors

    where:
    testCase                | errors  | exists  | valid
    "bounds are valid"      | 0       | true    | true
    "bounds are invalid"    | 1       | true    | false
    "bounds not not exist"  | 0       | false   | false
  }

  ////////////////////////////
  // Transform ES Requests  //
  ////////////////////////////
  def "tombstones create delete requests"() {
    def testKey = "ABC"

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, null, indexingConfig, esConfig))

    then:
    results.size() == 1
    results[0] instanceof DeleteRequest
    results[0].index() == testIndex
    results[0].id() == testKey
  }

  def "tombstones create multiple delete requests when configured"() {
    def testKey = 'ABC'
    def multipleDeleteConfig = BulkIndexingConfig.newBuilder()
        .withStoreName('teststore')
        .withMaxPublishInterval(Duration.ofSeconds(10))
        .withMaxPublishBytes(10000)
        .addIndexMapping(testTopic, DELETE, 'a')
        .addIndexMapping(testTopic, DELETE, 'b')
        .build()

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, null, multipleDeleteConfig, esConfig))

    then:
    results.size() == 2
    results.every { it instanceof DeleteRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "creates multiple index requests when configured"() {
    def testKey = 'ABC'
    def testRecord = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputCollectionAnalysis)
        .setDiscovery(inputCollectionDiscovery)
        .setPublishing(Publishing.newBuilder().build()).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())
    def multipleIndexConfig = BulkIndexingConfig.newBuilder()
        .withStoreName('teststore')
        .withMaxPublishInterval(Duration.ofSeconds(10))
        .withMaxPublishBytes(10000)
        .addIndexMapping(testTopic, INDEX, 'a')
        .addIndexMapping(testTopic, INDEX, 'b')
        .build()

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, multipleIndexConfig, esConfig))

    then:
    results.size() == 2
    results.every { it instanceof IndexRequest }
    results.every { it.id() == testKey }
    results*.index() == ['a', 'b']
  }

  def "record that is #testCase creates delete request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder().setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def results = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, indexingConfig, esConfig))

    then:
    results.size() == 1
    results[0].id() == testKey
    results[0].index() == testIndex
    results[0] instanceof DeleteRequest

    where:
    testCase               | publishingObject
    "private with no time" | Publishing.newBuilder().setIsPrivate(true).build()
    "private until future" | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() + 60000).build()
    "public until past"    | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() - 60000).build()
  }

  def "record that is #testCase creates index request"() {
    def testKey = "ABC"
    def testRecord = ParsedRecord.newBuilder()
        .setType(RecordType.collection)
        .setAnalysis(inputCollectionAnalysis)
        .setDiscovery(inputCollectionDiscovery)
        .setPublishing(publishingObject).build()
    def testValue = ValueAndTimestamp.make(testRecord, System.currentTimeMillis())

    when:
    def result = IndexingHelpers.mapRecordToRequests(new IndexingInput(testTopic, testKey, testValue, indexingConfig, esConfig))

    then:
    result[0].id() == testKey
    result[0].index() == testIndex
    result[0] instanceof IndexRequest

    where:
    testCase              | publishingObject
    "private until past"  | Publishing.newBuilder().setIsPrivate(true).setUntil(System.currentTimeMillis() - 60000).build()
    "public with no time" | Publishing.newBuilder().setIsPrivate(false).build()
    "public until future" | Publishing.newBuilder().setIsPrivate(false).setUntil(System.currentTimeMillis() + 60000).build()
  }

  ///////////////////////////////
  // Generic Indexed Fields    //
  ///////////////////////////////
  def "only mapped #type fields are indexed"() {
    when:
    def xml = ClassLoader.systemClassLoader.getResourceAsStream(dataSource).text
    def record = TestUtils.buildRecordFromXML(xml)
    def result = IndexingHelpers.reformatMessageForSearch(record, fields)

    then:
    result.keySet().each({ assert fields.contains(it) })

    where:
    type          | fields            | dataSource
    'collection'  | collectionFields  | 'test-iso-collection.xml'
    'granule'     | granuleFields     | 'test-iso-granule.xml'
  }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////
  def "produces internalParentIdentifier for collection record correctly"() {
    expect:
    IndexingHelpers.prepareInternalParentIdentifier(inputRecord) == null
  }

  def "produces internalParentIdentifier for granule record correctly"() {
    def testId = "ABC"
    def record = ParsedRecord.newBuilder(inputRecord)
        .setType(RecordType.granule)
        .setRelationships([
            Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId(testId).build()
        ])
        .build()

    expect:
    IndexingHelpers.prepareInternalParentIdentifier(record) == testId
  }

  def "produces filename for collection record correctly"() {
    expect:
    IndexingHelpers.prepareFilename(inputRecord) == null
  }

  def "produces filename for granule record correctly when record has data"() {
    def filename = "ABC"
    def record = ParsedRecord.newBuilder(inputRecord)
        .setType(RecordType.granule)
        .setFileInformation(FileInformation.newBuilder().setName(filename).build())
        .build()

    expect:
    IndexingHelpers.prepareFilename(record) == filename
  }

  def "produces filename for granule record correctly when record does not have data"() {
    def record = ParsedRecord.newBuilder(inputRecord)
        .setType(RecordType.granule)
        .build()

    expect:
    IndexingHelpers.prepareFilename(record) == null
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  def "prepares service links"() {
    when:
    def discovery = TestUtils.buildRecordFromXML(inputGranuleXml).discovery
    def result = IndexingHelpers.prepareServiceLinks(discovery)

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
    def discovery = TestUtils.buildRecordFromXML(inputGranuleXml).discovery

    expect:
    IndexingHelpers.prepareServiceLinkProtocols(discovery) == protocols
  }

  def "prepares link protocols"() {
    Set protocols = ['HTTP']
    def discovery = TestUtils.buildRecordFromXML(inputGranuleXml).discovery

    expect:
    IndexingHelpers.prepareLinkProtocols(discovery) == protocols
  }

  ////////////////////////////
  // Data Formats           //
  ////////////////////////////
  def "prepares data formats"() {
    def discovery = TestUtils.buildRecordFromXML(inputGranuleXml).discovery

    expect:
    IndexingHelpers.prepareDataFormats(discovery) == [
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
    def record = TestUtils.buildRecordFromXML(inputCollectionXml)
    def result = IndexingHelpers.prepareResponsibleParties(record)

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
    def record = TestUtils.buildRecordFromXML(inputGranuleXml)
    def result = IndexingHelpers.prepareResponsibleParties(record)

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  def "party names are not included in granule search info"() {
    when:
    def record = TestUtils.buildRecordFromXML(inputGranuleXml) // <-- granule!
    def result = IndexingHelpers.reformatMessageForSearch(record, collectionFields) // <-- top level reformat method!

    then:
    result.individualNames == [] as Set
    result.organizationNames == [] as Set
  }

  ////////////////////////////
  // Dates                  //
  ////////////////////////////
  def "When #situation.description, expected temporal bounding generated"() {
    when:
    def newTimeMetadata = IndexingHelpers.prepareDates(situation.bounding, situation.analysis)

    then:
    newTimeMetadata == expectedResult

    // Only include data that will be checked to cut down on size of below tables
    where:
    situation               | expectedResult
    situations.instantDay   | [beginDate: '1999-12-31T00:00:00Z', endDate: '1999-12-31T23:59:59Z', beginYear: 1999, endYear: 1999]
    situations.instantYear  | [beginDate: '1999-01-01T00:00:00Z', endDate: '1999-12-31T23:59:59Z', beginYear: 1999, endYear: 1999]
    situations.instantPaleo | [beginDate: null, endDate: null, beginYear: -1000000000, endYear: -1000000000]
    situations.instantNano  | [beginDate: '2008-04-01T00:00:00Z', endDate: '2008-04-01T00:00:00Z', beginYear: 2008, endYear: 2008]
    situations.bounded      | [beginDate: '1900-01-01T00:00:00Z', endDate: '2009-12-31T23:59:59Z', beginYear: 1900, endYear: 2009]
    situations.paleoBounded | [beginDate: null, endDate: null, beginYear: -2000000000, endYear: -1000000000]
    situations.ongoing      | [beginDate: '1975-06-15T12:30:00Z', endDate: null, beginYear: 1975, endYear: null]
    situations.empty        | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }

  def "temporal bounding with #testCase dates is prepared correctly"() {
    given:
    def bounding = TemporalBounding.newBuilder().setBeginDate(begin).setEndDate(end).build()
    def analysis = Analyzers.analyzeTemporalBounding(Discovery.newBuilder().setTemporalBounding(bounding).build())

    when:
    def result = IndexingHelpers.prepareDates(bounding, analysis)

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
    Map parsedKeywords = IndexingHelpers.prepareGcmdKeyword(inputRecord.discovery)

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
    Discovery discovery = ISOParser.parseXMLMetadataToDiscovery(inputCollectionXml)
    Map parsedKeywords = IndexingHelpers.prepareGcmdKeyword(discovery)

    then:
    parsedKeywords.gcmdScience == expectedKeywordsFromIso.science
    parsedKeywords.gcmdScienceServices == expectedKeywordsFromIso.scienceService
  }

  def "accession values are not included"() {
    when:
    def result = IndexingHelpers.reformatMessageForSearch(inputRecord, esConfig.parsedMapping(esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet())

    then:
    result.accessionValues == null
  }

}
