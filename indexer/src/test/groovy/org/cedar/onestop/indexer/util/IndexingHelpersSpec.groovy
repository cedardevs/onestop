package org.cedar.onestop.indexer.util


import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.IdentificationAnalysis
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.TemporalBounding
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.TitleAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.parse.ISOParser
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.util.TemporalTestData.getSituations


@Unroll
class IndexingHelpersSpec extends Specification {

  ////////////////////////////
  // Shared Values          //
  ////////////////////////////
  static inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json')
  // from schemas repo
  static inputRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)

  static inputStreamKeywords = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-keywords.xml')

  static expectedResponsibleParties = [
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

  static expectedOrganizationNames = [
      'University of Boulder',
      'US NASA; Jet Propulsion Laboratory (JPL)',
      'Secret Underground Society',
      'Super Important Organization',
  ] as Set

  static expectedIndividualNames = [
      'John Smith',
      'Edward M. Armstrong',
      'Jarianna Whackositz',
  ] as Set

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

  static expectedKeywordsFromIso = [
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

  static expectedTemporalBounding = [
      beginDate: '2002-06-01T00:00:00Z',
      beginYear: 2002,
      endDate  : '2011-10-04T23:59:59Z',
      endYear  : 2011
  ]

  ////////////////////////////
  // Validate Message Tests //
  ////////////////////////////
  def "valid message passes validation check"() {
    expect:
    IndexingHelpers.validateMessage('dummy id', inputRecord)
  }

  // FIXME verify each validation check in isolation
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
    !IndexingHelpers.validateMessage('dummy id', record)?.valid
  }


  ///////////////////////////////
  // XML To ParsedRecord Tests //
  ///////////////////////////////
  // FIXME remove this big test? should be in schemas not here
  def 'xml to ParsedRecord to staging doc (test from old MetadataParserSpec)'() {
    given:
    def expectedKeywords = [
        'SIO > Super Important Organization',
        'OSIO > OTHER SUPER IMPORTANT ORGANIZATION',
        'SSIO > Super SIO (Super Important Organization)',
        'EARTH SCIENCE SERVICES > ENVIRONMENTAL ADVISORIES > FIRE ADVISORIES > WILDFIRES',
        'EARTH SCIENCE > This Keyword is > Misplaced and Invalid',
        'This Keyword > Is Just > WRONG',
        'Air temperature', 'Water temperature',
        'Wind speed', 'Wind direction',
        "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC TEMPERATURE > SURFACE TEMPERATURE > DEW POINT TEMPERATURE",
        "EARTH SCIENCE > OCEANS > SALINITY/DENSITY > SALINITY",
        "EARTH SCIENCE > VOLCANOES > THIS KEYWORD > IS INVALID",
        "Earth Science > Spectral/Engineering > microwave > Brightness Temperature",
        "Earth Science > Spectral/Engineering > microwave > Temperature Anomalies",
        "GEOGRAPHIC REGION > ARCTIC",
        "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > GULF OF MEXICO",
        "LIQUID EARTH > THIS KEYWORD > IS INVALID",
        'SEASONAL',
        '> 1 km'
    ]
    List serviceLinks = [
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
    ]
    List expectedServices = [[
                                 title         : 'Multibeam Bathymetric Surveys ArcGIS Map Service',
                                 alternateTitle: 'Alternate Title for Testing',
                                 description   : "NOAA's National Centers for Environmental Information (NCEI) is the U.S. national archive for multibeam bathymetric data and presently holds over 2400 surveys received from sources worldwide, including the U.S. academic fleet via the Rolling Deck to Repository (R2R) program. In addition to deep-water data, the multibeam database also includes hydrographic multibeam survey data from the National Ocean Service (NOS). This map service shows navigation for multibeam bathymetric surveys in NCEI's archive. Older surveys are colored orange, and more recent recent surveys are green.",
                                 date          : '2012-01-01',
                                 dateType      : 'creation',
                                 pointOfContact: [
                                     individualName  : '[AT LEAST ONE OF ORGANISATION, INDIVIDUAL OR POSITION]',
                                     organizationName: '[AT LEAST ONE OF ORGANISATION, INDIVIDUAL OR POSITION]',
                                     positionName    : '[AT LEAST ONE OF ORGANISATION, INDIVIDUAL OR POSITION]',
                                     role            : 'pointOfContact',
                                     email           : 'TEMPLATE@EMAIL.GOV',
                                     phone           : null
                                 ],
                                 operations    : serviceLinks
                             ]]
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    ParsedRecord record = buildRecordFromXML(document)
    Map validationResult = IndexingHelpers.validateMessage('parsed_record_test_id', record)
    Map discoveryMap = AvroUtils.avroToMap(record.discovery, true)
    Map stagingDoc = IndexingHelpers.reformatMessageForSearch(record)

    then:
    assert validationResult.valid
    stagingDoc.fileIdentifier == 'gov.super.important:FILE-ID'
    stagingDoc.parentIdentifier == 'gov.super.important:PARENT-ID'
    stagingDoc.hierarchyLevelName == null // removed from search index
    stagingDoc.doi == 'doi:10.5072/FK2TEST'
    stagingDoc.purpose == null // removed from search index
    stagingDoc.status == null // removed from search index
    stagingDoc.credit == null
    stagingDoc.title == 'Important Organization\'s Important File\'s Super Important Title'
    stagingDoc.alternateTitle == null // removed from search index
    stagingDoc.description == 'Wall of overly detailed, super informative, extra important text.'
    // Deep equality check
    stagingDoc.keywords as Set == expectedKeywords as Set
    stagingDoc.accessionValues == null
    stagingDoc.topicCategories == null
    stagingDoc.gcmdLocations == [
        'Geographic Region',
        'Geographic Region > Arctic',
        'Ocean',
        'Ocean > Atlantic Ocean',
        'Ocean > Atlantic Ocean > North Atlantic Ocean',
        'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
        'Liquid Earth',
        'Liquid Earth > This Keyword',
        'Liquid Earth > This Keyword > Is Invalid'
    ] as Set
    stagingDoc.gcmdInstruments == null
    stagingDoc.gcmdPlatforms == null
    stagingDoc.gcmdProjects == null
    stagingDoc.gcmdDataCenters == [
        'SIO > Super Important Organization',
        'OSIO > Other Super Important Organization',
        'SSIO > Super SIO (Super Important Organization)'
    ] as Set
    stagingDoc.gcmdHorizontalResolution == null
    stagingDoc.gcmdVerticalResolution == ['> 1 Km'] as Set
    stagingDoc.gcmdTemporalResolution == ['Seasonal'] as Set
    stagingDoc.beginDate == '2005-05-09T00:00:00Z'
    stagingDoc.endDate == '2010-10-01T23:59:59Z'
    stagingDoc.beginYear == 2005
    stagingDoc.endYear == 2010
    stagingDoc.spatialBounding as String == [
        type       : 'Polygon',
        coordinates: [
            [
                [-180.0, -90.0],
                [180.0, -90.0],
                [180.0, 90.0],
                [-180.0, 90.0],
                [-180.0, -90.0]
            ]
        ]
    ] as String
    stagingDoc.isGlobal == true
    stagingDoc.acquisitionInstruments == null
    stagingDoc.acquisitionOperations == null
    stagingDoc.acquisitionPlatforms == null
    stagingDoc.dataFormats.sort() == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ].sort()
    stagingDoc.links == [
        [
            linkName       : 'Super Important Access Link',
            linkProtocol   : 'HTTP',
            linkUrl        : 'http://www.example.com',
            linkDescription: 'Everything Important, All In One Place',
            linkFunction   : 'search'
        ]
    ]

    // TODO - add tests for collection-level contact parsing
    stagingDoc.individualNames == null
//    stagingDoc.individualNames == [
//        'John Smith',
//        'Jane Doe',
//        'Jarianna Whackositz',
//        'Dr. Quinn McClojure Man',
//        'Zebulon Pike',
//        'Little Rhinoceros',
//        'Skeletor McSkittles',
//    ] as Set

    stagingDoc.organizationNames == null
//    stagingDoc.organizationNames == [
//        'University of Awesome',
//        'Secret Underground Society',
//        'Soap Boxes Inc.',
//        'Pikes Peak Inc.',
//        'Alien Infested Spider Monkey Rescue',
//        'The Underworld',
//        'Super Important Organization',
//    ] as Set

    stagingDoc.thumbnail == 'https://www.example.com/exportImage?soCool=yes&format=png'
    stagingDoc.thumbnailDescription == null
    stagingDoc.creationDate == null
    stagingDoc.revisionDate == null
    stagingDoc.publicationDate == null
    stagingDoc.citeAsStatements.sort() == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]']

    stagingDoc.crossReferences == null
    stagingDoc.largerWorks == null

    stagingDoc.useLimitation == null
    stagingDoc.legalConstraints == null
    stagingDoc.accessFeeStatement == null
    stagingDoc.orderingInstructions == null
    stagingDoc.edition == null

    stagingDoc.dsmmAccessibility == null
    stagingDoc.dsmmDataIntegrity == null
    stagingDoc.dsmmDataQualityAssessment == null
    stagingDoc.dsmmDataQualityAssurance == null
    stagingDoc.dsmmDataQualityControlMonitoring == null
    stagingDoc.dsmmPreservability == null
    stagingDoc.dsmmProductionSustainability == null
    stagingDoc.dsmmTransparencyTraceability == null
    stagingDoc.dsmmUsability == null
    stagingDoc.updateFrequency == null
    stagingDoc.presentationForm == null

    discoveryMap.services == expectedServices
//todo metadata team requested this structure for API, which is different than how we currently parse it
    stagingDoc.serviceLinks[0].title == 'Multibeam Bathymetric Surveys ArcGIS Map Service'
    stagingDoc.serviceLinks[0].links == serviceLinks.sort()
    stagingDoc.services == null
  }

  def "science keywords are parsed as expected from iso"() {
    when:
    Discovery discovery = ISOParser.parseXMLMetadataToDiscovery(inputStreamKeywords.text)
    Map parsedKeywords = IndexingHelpers.prepareGcmdKeyword(discovery)

    then:
    parsedKeywords.gcmdScience == expectedKeywordsFromIso.science
    parsedKeywords.gcmdScienceServices == expectedKeywordsFromIso.scienceService
  }

  ///////////////////////////////////////
  // Reformat Message For Search Tests //
  ///////////////////////////////////////

  // FIXME less reliance on loading XML here; test with a ParsedRecord
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

  def "Create contacts, publishers and creators from responsibleParties"() {
    when:
    Map partiesMap = IndexingHelpers.prepareResponsibleParties(inputRecord.discovery.responsibleParties)

    then:
    partiesMap.individualNames == expectedIndividualNames
    partiesMap.organizationNames == expectedOrganizationNames
  }

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

  def "new record is ready for onestop"() {
    when:
    def result = IndexingHelpers.reformatMessageForSearch(inputRecord)

    then:
    result.serviceLinks == []
    result.accessionValues == null

    expectedTemporalBounding.forEach({ k, v ->
      result.get(k) == v
    })

    result.keywords as Set == expectedKeywords as Set
    expectedGcmdKeywords.each { k, v ->
      assert result[k] == v
    }

    result.responsibleParties == null
    result.individualNames == expectedIndividualNames
    result.organizationNames == expectedOrganizationNames
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
    'typical'     | '2005-05-09T00:00:00Z' | '2010-10-01'            | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T23:59:59Z', beginYear: 2005, endYear: 2010]
    'no timezone' | '2005-05-09T00:00:00'  | '2010-10-01T00:00:00'   | [beginDate: '2005-05-09T00:00:00Z', endDate: '2010-10-01T00:00:00Z', beginYear: 2005, endYear: 2010]
    'paleo'       | '-100000001'           | '-1601050'              | [beginDate: null, endDate: '-1601050-12-31T23:59:59Z', beginYear: -100000001, endYear: -1601050]
    'invalid'     | '1984-04-31'           | '1985-505-09T00:00:00Z' | [beginDate: null, endDate: null, beginYear: null, endYear: null]
  }


  private static ParsedRecord buildRecordFromXML(String xml) {
    def discovery = ISOParser.parseXMLMetadataToDiscovery(xml)
    def analysis = Analyzers.analyze(discovery)
    def builder = ParsedRecord.newBuilder().setDiscovery(discovery).setAnalysis(analysis)

    // Determine RecordType (aka granule or collection) from Discovery & Analysis info
    String parentIdentifier = discovery.parentIdentifier
    String hierarchyLevelName = discovery.hierarchyLevelName
    if (hierarchyLevelName == null || hierarchyLevelName != 'granule' || !parentIdentifier) {
      builder.setType(RecordType.collection)
    }
    else {
      builder.setType(RecordType.granule)
    }

    return builder.build()
  }

}
