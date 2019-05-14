package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
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
    !InventoryManagerToOneStopUtil.validateMessage('dummy id', record)?.valid
  }

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
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    Map parsedXML = InventoryManagerToOneStopUtil.xmlToParsedRecord(document)
    Map validationResult = InventoryManagerToOneStopUtil.validateMessage('parsed_record_test_id', parsedXML.parsedRecord)
    Map stagingDoc = InventoryManagerToOneStopUtil.reformatMessageForSearch(parsedXML.parsedRecord)
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
    //todo confirm this is what we want
    stagingDoc.accessionValues == []
    //result from metadata parser
//    stagingDoc.accessionValues == [
//        '0038924',
//        '0038947',
//        '0038970'
//    ] as Set
    stagingDoc.topicCategories == ['environment', 'oceans']
    stagingDoc.gcmdScienceServices == [
        'Environmental Advisories',
        'Environmental Advisories > Fire Advisories',
        'Environmental Advisories > Fire Advisories > Wildfires'
    ] as Set
    stagingDoc.gcmdScience == [
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
        'This Keyword Is',
        'This Keyword Is > Misplaced And Invalid',
        'Volcanoes',
        'Volcanoes > This Keyword',
        'Volcanoes > This Keyword > Is Invalid'
    ] as Set
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
        beginDate           : '2005-05-09T00:00:00Z',
        beginIndeterminate  : null,
        beginYear           : 2005,
        endDate             : '2010-10-01',
        endIndeterminate    : null,
        endYear             : 2010,
        instant             : null,
        instantIndeterminate: null,
        description         : null
    ]
    stagingDoc.spatialBounding == [
        type       : 'Polygon',
        coordinates: [
            [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]
        ]
    ]
    stagingDoc.isGlobal == true
    stagingDoc.acquisitionInstruments == [
        [
            instrumentIdentifier : 'SII > Super Important Instrument',
            instrumentType       : 'sensor',
            instrumentDescription: 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
        ]
    ] as Set
    stagingDoc.acquisitionOperations == [
        [
            operationDescription: null,
            operationIdentifier : 'Super Important Project',
            operationStatus     : null,
            operationType       : null
        ]
    ] as Set
    stagingDoc.dataFormats == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ] as Set
    stagingDoc.acquisitionPlatforms == [
        [
            platformIdentifier : 'TS-18 > TumbleSat-18',
            platformDescription: 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
            platformSponsor    : ['Super Important Organization', 'Other (Kind Of) Important Organization']
        ]
    ] as Set
    stagingDoc.links == [
        [
            linkName       : 'Super Important Access Link',
            linkProtocol   : 'HTTP',
            linkUrl        : 'http://www.example.com',
            linkDescription: 'Everything Important, All In One Place',
            linkFunction   : 'search'
        ]
    ] as Set

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
    stagingDoc.citeAsStatements == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set

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
    ] as Set

    stagingDoc.largerWorks == [
        [
            title: '[TITLE OF PROJECT]',
            date: '9999-10-10',
            links: []
        ]
    ] as Set

    stagingDoc.useLimitation == '[NOAA LEGAL STATEMENT]'
    stagingDoc.legalConstraints == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set
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
    stagingDoc.services in Set
    stagingDoc.services.each { s ->
      s in String
    }
  }
}
