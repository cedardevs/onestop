package org.cedar.onestop.api.admin.service

import groovy.json.JsonOutput
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import org.cedar.schemas.parse.ISOParser
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.util.TemporalTestData.situations

@Unroll
class IndexerTest extends Specification {

  def inputStream = ClassLoader.systemClassLoader.getResourceAsStream('example-record-avro.json') // from schemas repo
  def inputRecord = AvroUtils.<ParsedRecord> jsonToAvro(inputStream, ParsedRecord.classSchema)

  def inputStreamKeywords = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-keywords.xml')

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


  def expectedKeywordsFromIso = [
    science : [
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
    scienceService : [
      'Data Analysis And Visualization > Calibration/Validation > Calibration',
      'Data Analysis And Visualization > Calibration/Validation',
      'Data Analysis And Visualization'
      ] as Set
  ]

  def expectedGcmdKeywords = [
      gcmdScienceServices     : [
      ] as Set,
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

  ////////////////////////////
  // Validate Message Tests //
  ////////////////////////////
  def "valid message passes validation check"() {
    expect:
    Indexer.validateMessage('dummy id', inputRecord)
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
    !Indexer.validateMessage('dummy id', record)?.valid
  }


  ///////////////////////////////
  // XML To ParsedRecord Tests //
  ///////////////////////////////
  // FIXME remove this big test? should be in schemas not here
  def 'xml to ParsedRecord to staging doc (test from old MetadataParserSpec)'(){
    given:
    String expectedKeywords = JsonOutput.toJson([
        [
            values: ['SIO > Super Important Organization','OSIO > OTHER SUPER IMPORTANT ORGANIZATION', 'SSIO > Super SIO (Super Important Organization)'],
            type: 'dataCenter',
            namespace: 'GCMD Keywords - Data Centers'
        ],
        [
            values: ['EARTH SCIENCE SERVICES > ENVIRONMENTAL ADVISORIES > FIRE ADVISORIES > WILDFIRES', 'EARTH SCIENCE > This Keyword is > Misplaced and Invalid', 'This Keyword > Is Just > WRONG'],
            type: 'service',
            namespace: 'Global Change Master Directory Science and Services Keywords'
        ],
        [
            values: ['Air temperature', 'Water temperature'],
            type: 'theme',
            namespace: 'Miscellaneous keyword type'
        ],
        [
            values: ['Wind speed', 'Wind direction'],
            type: 'theme',
            namespace: 'Miscellaneous keyword type'
        ],
        [
            values: [
                "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC TEMPERATURE > SURFACE TEMPERATURE > DEW POINT TEMPERATURE",
                "EARTH SCIENCE > OCEANS > SALINITY/DENSITY > SALINITY",
                "EARTH SCIENCE > VOLCANOES > THIS KEYWORD > IS INVALID",
                "Earth Science > Spectral/Engineering > microwave > Brightness Temperature",
                "Earth Science > Spectral/Engineering > microwave > Temperature Anomalies"
            ],
            type: 'theme',
            namespace: 'GCMD Keywords - Science Keywords'
        ],
        [
            values: [
                "GEOGRAPHIC REGION > ARCTIC",
                "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > GULF OF MEXICO",
                "LIQUID EARTH > THIS KEYWORD > IS INVALID"
            ],
            type: 'place',
            namespace: 'GCMD Keywords - Locations'
        ],
        [
            values: ['SEASONAL'],
            type: 'dataResolution',
            namespace: 'Global Change Master Directory Keywords - Temporal Data Resolution'
        ],
        [
            values: ['> 1 km'],
            type: 'dataResolution',
            namespace: 'GCMD Keywords - Vertical Data Resolution'
        ]
    ] as Set)
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
    List expectedServices =[ [
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
    Map parsedXML = Indexer.xmlToParsedRecord(document)
    Map validationResult = Indexer.validateMessage('parsed_record_test_id', parsedXML.parsedRecord)
    Map discoveryMap = AvroUtils.avroToMap(parsedXML.parsedRecord.discovery, true)
    Map stagingDoc = Indexer.reformatMessageForSearch(parsedXML.parsedRecord)
    def generatedKeywords = JsonOutput.toJson(stagingDoc.keywords)

    then:
    assert validationResult.valid
    stagingDoc.fileIdentifier == 'gov.super.important:FILE-ID'
    stagingDoc.parentIdentifier == 'gov.super.important:PARENT-ID'
    stagingDoc.hierarchyLevelName == 'granule'
    stagingDoc.doi == 'doi:10.5072/FK2TEST'
    stagingDoc.purpose == 'Provide quality super important data to the user community.'
    stagingDoc.status == 'completed'
    stagingDoc.credit == null
    stagingDoc.title == 'Important Organization\'s Important File\'s Super Important Title'
    stagingDoc.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    stagingDoc.description == 'Wall of overly detailed, super informative, extra important text.'
    // Deep equality check
    generatedKeywords == expectedKeywords
    stagingDoc.accessionValues == []
    stagingDoc.topicCategories == ['environment', 'oceans']
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
    stagingDoc.gcmdInstruments == [] as Set
    stagingDoc.gcmdPlatforms == [] as Set
    stagingDoc.gcmdProjects == [] as Set
    stagingDoc.gcmdDataCenters == [
        'SIO > Super Important Organization',
        'OSIO > Other Super Important Organization',
        'SSIO > Super SIO (Super Important Organization)'
    ] as Set
    stagingDoc.gcmdHorizontalResolution == [] as Set
    stagingDoc.gcmdVerticalResolution == ['> 1 Km'] as Set
    stagingDoc.gcmdTemporalResolution == ['Seasonal'] as Set
    stagingDoc.temporalBounding == [
        beginDate:'2005-05-09T00:00:00Z',
        endDate:'2010-10-01T23:59:59Z',
        beginYear:2005,
        endYear:2010
    ]
    stagingDoc.spatialBounding  as String == [
        type:'Polygon',
        coordinates:[
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
    stagingDoc.acquisitionInstruments == [
        [
            instrumentIdentifier : 'SII > Super Important Instrument',
            instrumentType       : 'sensor',
            instrumentDescription: 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
        ]
    ]
    stagingDoc.acquisitionOperations == [
        [
            operationDescription: null,
            operationIdentifier : 'Super Important Project',
            operationStatus     : null,
            operationType       : null
        ]
    ]
    stagingDoc.dataFormats.sort() == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ].sort()
    stagingDoc.acquisitionPlatforms == [
        [
            platformIdentifier : 'TS-18 > TumbleSat-18',
            platformDescription: 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
            platformSponsor    : ['Super Important Organization', 'Other (Kind Of) Important Organization']
        ]
    ]
    stagingDoc.links == [
        [
            linkName       : 'Super Important Access Link',
            linkProtocol   : 'HTTP',
            linkUrl        : 'http://www.example.com',
            linkDescription: 'Everything Important, All In One Place',
            linkFunction   : 'search'
        ]
    ]

    stagingDoc.contacts == [
        [
            individualName  : 'John Smith',
            organizationName: 'University of Awesome',
            positionName    : 'Chief Awesomeness Officer',
            role            : 'pointOfContact',
            email           : 'john.smith@uoa.edu',
            phone           : '555-555-5555'
        ],
        [
            individualName  : 'Jane Doe',
            organizationName: 'University of Awesome',
            positionName    : 'VP of Awesome Behavior',
            role            : 'distributor',
            email           : 'jane.doe@uoa.edu',
            phone           : '555-555-5556'
        ],
    ] as Set

    stagingDoc.creators == [
        [
            individualName  : 'Jarianna Whackositz',
            organizationName: 'Secret Underground Society',
            positionName    : 'Software Developer',
            role            : 'resourceProvider',
            email           : 'jw@mock-creator-email.org',
            phone           : '555-555-5558'
        ],
        [
            individualName  : 'Dr. Quinn McClojure Man',
            organizationName: 'Soap Boxes Inc.',
            positionName    : 'Software Developer',
            role            : 'originator',
            email           : 'dqmm@mock-creator-email.org',
            phone           : '555-555-5559'
        ],
        [
            individualName  : 'Zebulon Pike',
            organizationName: 'Pikes Peak Inc.',
            positionName    : 'Software Developer',
            role            : 'principalInvestigator',
            email           : 'zp@mock-creator-email.org',
            phone           : '555-555-5560'
        ],
        [
            individualName  : 'Little Rhinoceros',
            organizationName: 'Alien Infested Spider Monkey Rescue',
            positionName    : 'Software Developer',
            role            : 'author',
            email           : 'lr@mock-creator-email.org',
            phone           : '555-555-5561'
        ],
        [
            individualName  : 'Skeletor McSkittles',
            organizationName: 'The Underworld',
            positionName    : 'Bringer of Skittles',
            role            : 'collaborator',
            email           : 'sm@mock-creator-email.org',
            phone           : '555-555-5562'
        ],
    ] as Set

    stagingDoc.publishers == [
        [
            individualName  : null,
            organizationName: 'Super Important Organization',
            positionName    : null,
            role            : 'publisher',
            email           : 'email@sio.co',
            phone           : '555-123-4567'
        ],
    ] as Set

    stagingDoc.thumbnail == 'https://www.example.com/exportImage?soCool=yes&format=png'
    stagingDoc.thumbnailDescription == 'Preview graphic'
    stagingDoc.creationDate == null
    stagingDoc.revisionDate == '2011-01-02'
    stagingDoc.publicationDate == '2010-11-15'
    stagingDoc.citeAsStatements.sort() == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]']

    stagingDoc.crossReferences == [
        [
            title: '[TITLE OF PUBLICATION]',
            date: '9999-01-01',
            links: [[
                        linkName: null,
                        linkProtocol: null,
                        linkUrl: 'HTTPS://WWW.EXAMPLE.COM',
                        linkDescription: '[DESCRIPTION OF URL]',
                        linkFunction: 'information'
                    ]]
        ]
    ]

    stagingDoc.largerWorks == [
        [
            title: '[TITLE OF PROJECT]',
            date: '9999-10-10',
            links: []
        ]
    ]

    stagingDoc.useLimitation == '[NOAA LEGAL STATEMENT]'
    stagingDoc.legalConstraints == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]']
    stagingDoc.accessFeeStatement == 'template fees'
    stagingDoc.orderingInstructions == 'template ordering instructions'
    stagingDoc.edition == '[EDITION]'

    stagingDoc.dsmmAccessibility == 4
    stagingDoc.dsmmDataIntegrity == 0
    stagingDoc.dsmmDataQualityAssessment == 2
    stagingDoc.dsmmDataQualityAssurance == 3
    stagingDoc.dsmmDataQualityControlMonitoring == 1
    stagingDoc.dsmmPreservability == 5
    stagingDoc.dsmmProductionSustainability == 4
    stagingDoc.dsmmTransparencyTraceability == 2
    stagingDoc.dsmmUsability == 3
    stagingDoc.updateFrequency == 'asNeeded'
    stagingDoc.presentationForm == 'tableDigital'

    discoveryMap.services == expectedServices
//todo metadata team requested this structure for API, which is different than how we currently parse it
    stagingDoc.serviceLinks[0].title == 'Multibeam Bathymetric Surveys ArcGIS Map Service'
    stagingDoc.serviceLinks[0].links == serviceLinks.sort()
    stagingDoc.services == ""
  }

  def 'Valid XML file translated to valid ParsedRecord'() {
    // TODO Simple happy case test; no need to check fields here
  }

  def 'Malformed XML throws error, no ParsedRecord created'() {
    // TODO
  }

  def "When creating ParsedRecord from XML, record type is #type when #situation"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream(path).text

    when:
    def result = Indexer.xmlToParsedRecord(document)

    then:
    !result.containsKey('error')

    and:
    ParsedRecord parsedRecord = result.parsedRecord
    parsedRecord.type == type

    where:
    type                  | path                             | situation
    RecordType.granule    | 'test-iso-granule-type.xml'      | 'hln is granule and pid present'
    RecordType.collection | 'test-iso-collection-type-1.xml' | 'hln is present but not granule'
    RecordType.collection | 'test-iso-collection-type-2.xml' | 'hln is null'
    null                  | 'test-iso-error-type.xml'        | 'hln is granule but no pid present'
  }

  def "science keywords are parsed as expected from iso" () {
    when:
    Discovery discovery = ISOParser.parseXMLMetadataToDiscovery(inputStreamKeywords.text)
    Map parsedKeywords = Indexer.createGcmdKeyword(discovery)

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
    Map parsedKeywords = Indexer.createGcmdKeyword(inputRecord.discovery)

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
    Map partiesMap = Indexer.parseResponsibleParties(inputRecord.discovery.responsibleParties)

    then:
    partiesMap.contacts == expectedResponsibleParties.contacts
    partiesMap.creators == expectedResponsibleParties.creators
    partiesMap.publishers == expectedResponsibleParties.publishers
  }

  def "When #situation.description, expected temporal bounding generated"() {
    when:
    def newTimeMetadata = Indexer.readyDatesForSearch(situation.bounding, situation.analysis)

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
    def result = Indexer.reformatMessageForSearch(inputRecord)

    then:
    result.serviceLinks == []
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

  def "Temporal bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    Map parsedXML = Indexer.xmlToParsedRecord(document)

    Map stagingDoc = Indexer.reformatMessageForSearch(parsedXML.parsedRecord)

    then:
    stagingDoc.temporalBounding == [
        beginDate:'2005-05-09T00:00:00Z',
        endDate:'2010-10-01T23:59:59Z',
        beginYear:2005,
        endYear:2010
    ]
  }

  def "Very old temporal bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-paleo-dates-metadata.xml").text

    when:
    Map parsedXML = Indexer.xmlToParsedRecord(document)

    Map stagingDoc = Indexer.reformatMessageForSearch(parsedXML.parsedRecord)

    then:
    stagingDoc.temporalBounding == [
        beginDate:null,
        endDate:'-1601050-12-31T23:59:59Z',
        beginYear:-100000001,
        endYear:-1601050
    ]
  }

  def "Temporal bounding without time zone information is correctly parsed with UTC"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-no-timezone-dates-metadata.xml").text

    when:
    Map parsedXML = Indexer.xmlToParsedRecord(document)

    Map stagingDoc = Indexer.reformatMessageForSearch(parsedXML.parsedRecord)

    then:
    stagingDoc.temporalBounding == [
        beginDate:'2005-05-09T00:00:00Z',
        endDate:'2010-10-01T00:00:00Z',
        beginYear:2005,
        endYear:2010
    ]
  }

  def "Invalid temporal bounding is prevented"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-invalid-dates-metadata.xml").text

    when:
    Map parsedXML = Indexer.xmlToParsedRecord(document)
    Map stagingDoc = Indexer.reformatMessageForSearch(parsedXML.parsedRecord)

    then:
    parsedXML.parsedRecord.analysis.temporalBounding.beginDescriptor as String == 'INVALID'
    parsedXML.parsedRecord.analysis.temporalBounding.endDescriptor as String == 'INVALID'
    stagingDoc.temporalBounding == [beginDate:null, endDate:null, beginYear:null, endYear:null]
  }


  //////////////////////////
  // Secure Parsing Tests //
  //////////////////////////
  def "CVE-2018-1000840 use external docs hack"() {
    given: 'an xml which utilizes this vunerability'
    def document = ClassLoader.systemClassLoader.getResourceAsStream("attack.xml").text

    when: 'you attempt to parse the xml'

    def parsedXml = Indexer.xmlToParsedRecord(document)

    then: 'we throw an exception instead of parsing attack-vector xml'
    parsedXml.error.title == 'Load request failed due to malformed XML.'
    parsedXml.error.detail.contains('SAXParseException')
  }
}
