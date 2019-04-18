package org.cedar.onestop.api.metadata.service

import groovy.json.JsonOutput
import spock.lang.Specification
import spock.lang.Unroll

import java.time.format.DateTimeParseException
import org.xml.sax.SAXParseException

@Unroll
class MetadataParserTest extends Specification {

  def "CVE-2018-1000840 use external docs hack"() {
    given: 'an xml which utilizes this vunerability'
    def document = ClassLoader.systemClassLoader.getResourceAsStream("attack.xml").text

    when: 'you attempt to parse the xml'
    def parsedXml = MetadataParser.parseXMLMetadataToMap(document)

    then: 'we throw an exception instead of parsing attack-vector xml'
    thrown(SAXParseException)
  }

  def "XML metadata record is correctly parsed in full"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseXMLMetadataToMap(document)

    then:
    parsedXml.fileIdentifier == 'gov.super.important:FILE-ID'
    parsedXml.parentIdentifier == 'gov.super.important:PARENT-ID'
    parsedXml.hierarchyLevelName == 'granule'
    parsedXml.doi == 'doi:10.5072/FK2TEST'
    parsedXml.purpose == 'Provide quality super important data to the user community.'
    parsedXml.status == 'completed'
    parsedXml.credit == null
    parsedXml.title == 'Important Organization\'s Important File\'s Super Important Title'
    parsedXml.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    parsedXml.description == 'Wall of overly detailed, super informative, extra important text.'
    // Deep equality check
    JsonOutput.toJson(parsedXml.keywords) == JsonOutput.toJson([
        [
            values: ['SIO > Super Important Organization','OSIO > Other Super Important Organization', 'SSIO > Super SIO (Super Important Organization)'],
            type: 'dataCenter',
            namespace: 'GCMD Keywords - Data Centers'
        ],
        [
            values: ['Environmental Advisories > Fire Advisories > Wildfires', 'This Keyword Is > Misplaced And Invalid', 'This Keyword > Is Just > WRONG'],
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
                'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
                'Oceans > Salinity/Density > Salinity',
                'Volcanoes > This Keyword > Is Invalid',
                'Spectral/Engineering > Microwave > Brightness Temperature',
                'Spectral/Engineering > Microwave > Temperature Anomalies'
            ],
            type: 'theme',
            namespace: 'GCMD Keywords - Science Keywords'
        ],
        [
            values: ['Geographic Region > Arctic', 'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico', 'Liquid Earth > This Keyword > Is Invalid'],
            type: 'place',
            namespace: 'GCMD Keywords - Locations'
            ],
        [
            values: ['Seasonal'],
            type: 'dataResolution',
            namespace: 'Global Change Master Directory Keywords - Temporal Data Resolution'
        ],
        [
            values: ['> 1 Km'],
            type: 'dataResolution',
            namespace: 'GCMD Keywords - Vertical Data Resolution'
        ]
    ] as Set)
    parsedXml.accessionValues == [
        '0038924',
        '0038947',
        '0038970'
    ] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScienceServices == [
        'Environmental Advisories',
        'Environmental Advisories > Fire Advisories',
        'Environmental Advisories > Fire Advisories > Wildfires'
    ] as Set
    parsedXml.gcmdScience == [
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
    parsedXml.gcmdLocations == [
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
    parsedXml.gcmdInstruments == [] as Set
    parsedXml.gcmdPlatforms == [] as Set
    parsedXml.gcmdProjects == [] as Set
    parsedXml.gcmdDataCenters == [
        'SIO > Super Important Organization',
        'OSIO > Other Super Important Organization',
        'SSIO > Super SIO (Super Important Organization)'
    ] as Set
    parsedXml.gcmdHorizontalResolution == [] as Set
    parsedXml.gcmdVerticalResolution == ['> 1 Km'] as Set
    parsedXml.gcmdTemporalResolution == ['Seasonal'] as Set
    parsedXml.temporalBounding == [
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
    parsedXml.spatialBounding == [
        type       : 'Polygon',
        coordinates: [
            [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]
        ]
    ]
    parsedXml.isGlobal == true
    parsedXml.acquisitionInstruments == [
        [
            instrumentIdentifier : 'SII > Super Important Instrument',
            instrumentType       : 'sensor',
            instrumentDescription: 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
        ]
    ] as Set
    parsedXml.acquisitionOperations == [
        [
            operationDescription: null,
            operationIdentifier : 'Super Important Project',
            operationStatus     : null,
            operationType       : null
        ]
    ] as Set
    parsedXml.dataFormats == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ] as Set
    parsedXml.acquisitionPlatforms == [
        [
            platformIdentifier : 'TS-18 > TumbleSat-18',
            platformDescription: 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
            platformSponsor    : ['Super Important Organization', 'Other (Kind Of) Important Organization']
        ]
    ] as Set
    parsedXml.links == [
        [
            linkName       : 'Super Important Access Link',
            linkProtocol   : 'HTTP',
            linkUrl        : 'http://www.example.com',
            linkDescription: 'Everything Important, All In One Place',
            linkFunction   : 'search'
        ]
    ] as Set

    parsedXml.contacts == [
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

    parsedXml.creators == [
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

    parsedXml.publishers == [
        [
            individualName  : null,
            organizationName: 'Super Important Organization',
            positionName    : null,
            role            : 'publisher',
            email           : 'email@sio.co',
            phone           : '555-123-4567'
        ],
    ] as Set

    parsedXml.thumbnail == 'https://www.example.com/exportImage?soCool=yes&format=png'
    parsedXml.thumbnailDescription == 'Preview graphic'
    parsedXml.creationDate == null
    parsedXml.revisionDate == '2011-01-02'
    parsedXml.publicationDate == '2010-11-15'
    parsedXml.citeAsStatements == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set

    parsedXml.crossReferences == [
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

    parsedXml.largerWorks == [
        [
            title: '[TITLE OF PROJECT]',
            date: '9999-10-10',
            links: []
        ]
    ] as Set

    parsedXml.useLimitation == '[NOAA LEGAL STATEMENT]'
    parsedXml.legalConstraints == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set
    parsedXml.accessFeeStatement == 'template fees'
    parsedXml.orderingInstructions == 'template ordering instructions'
    parsedXml.edition == '[EDITION]'

    parsedXml.dsmmAccessibility == 4
    parsedXml.dsmmDataIntegrity == 0
    parsedXml.dsmmDataQualityAssessment == 2
    parsedXml.dsmmDataQualityAssurance == 3
    parsedXml.dsmmDataQualityControlMonitoring == 1
    parsedXml.dsmmPreservability == 5
    parsedXml.dsmmProductionSustainability == 4
    parsedXml.dsmmTransparencyTraceability == 2
    parsedXml.dsmmUsability == 3
    parsedXml.updateFrequency == 'asNeeded'
    parsedXml.presentationForm == 'tableDigital'
    parsedXml.services in Set
    parsedXml.services.each { s ->
      s in String
    }

  }

  def "Identifier info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def idInfo = MetadataParser.parseIdentifierInfo(document)

    then:
    idInfo.fileId == 'gov.super.important:FILE-ID'
    idInfo.doi == 'doi:10.5072/FK2TEST'
    idInfo.parentId == 'gov.super.important:PARENT-ID'
  }

  def "Citation info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def citationInfo = MetadataParser.parseCitationInfo(document)

    then:
    citationInfo.fileIdentifier == 'gov.super.important:FILE-ID'
    citationInfo.parentIdentifier == 'gov.super.important:PARENT-ID'
    citationInfo.hierarchyLevelName == 'granule'
    citationInfo.doi == 'doi:10.5072/FK2TEST'
    citationInfo.purpose == 'Provide quality super important data to the user community.'
    citationInfo.status == 'completed'
    citationInfo.credit == null
    citationInfo.title == 'Important Organization\'s Important File\'s Super Important Title'
    citationInfo.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    citationInfo.description == 'Wall of overly detailed, super informative, extra important text.'
    citationInfo.thumbnail == 'https://www.example.com/exportImage?soCool=yes&format=png'
    citationInfo.thumbnailDescription == 'Preview graphic'
    citationInfo.creationDate == null
    citationInfo.revisionDate == '2011-01-02'
    citationInfo.publicationDate == '2010-11-15'
    citationInfo.citeAsStatements == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set
    citationInfo.crossReferences == [
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

    citationInfo.largerWorks == [
        [
            title: '[TITLE OF PROJECT]',
            date: '9999-10-10',
            links: []
        ]
    ] as Set

    citationInfo.useLimitation == '[NOAA LEGAL STATEMENT]'
    citationInfo.legalConstraints == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set
    citationInfo.accessFeeStatement == 'template fees'
    citationInfo.orderingInstructions == 'template ordering instructions'
    citationInfo.edition == '[EDITION]'
  }

  def "Keywords and topics are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseKeywordsAndTopics(document)

    then:
    // Deep equality check
    JsonOutput.toJson(parsedXml.keywords) == JsonOutput.toJson([
        [
            values: ['SIO > Super Important Organization','OSIO > Other Super Important Organization', 'SSIO > Super SIO (Super Important Organization)'],
            type: 'dataCenter',
            namespace: 'GCMD Keywords - Data Centers'
        ],
        [
            values: ['Environmental Advisories > Fire Advisories > Wildfires', 'This Keyword Is > Misplaced And Invalid', 'This Keyword > Is Just > WRONG'],
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
                'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
                'Oceans > Salinity/Density > Salinity',
                'Volcanoes > This Keyword > Is Invalid',
                'Spectral/Engineering > Microwave > Brightness Temperature',
                'Spectral/Engineering > Microwave > Temperature Anomalies'
            ],
            type: 'theme',
            namespace: 'GCMD Keywords - Science Keywords'
        ],
        [
            values: ['Geographic Region > Arctic', 'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico', 'Liquid Earth > This Keyword > Is Invalid'],
            type: 'place',
            namespace: 'GCMD Keywords - Locations'
        ],
        [
            values: ['Seasonal'],
            type: 'dataResolution',
            namespace: 'Global Change Master Directory Keywords - Temporal Data Resolution'
        ],
        [
            values: ['> 1 Km'],
            type: 'dataResolution',
            namespace: 'GCMD Keywords - Vertical Data Resolution'
        ]
    ] as Set)
    parsedXml.accessionValues == [
        '0038924',
        '0038947',
        '0038970'
    ] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScienceServices == [
        'Environmental Advisories',
        'Environmental Advisories > Fire Advisories',
        'Environmental Advisories > Fire Advisories > Wildfires'
    ] as Set
    parsedXml.gcmdScience == [
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
    parsedXml.gcmdLocations == [
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
    parsedXml.gcmdInstruments == [] as Set
    parsedXml.gcmdPlatforms == [] as Set
    parsedXml.gcmdProjects == [] as Set
    parsedXml.gcmdDataCenters == [
        'SIO > Super Important Organization',
        'OSIO > Other Super Important Organization',
        'SSIO > Super SIO (Super Important Organization)'
    ] as Set
    parsedXml.gcmdHorizontalResolution == [] as Set
    parsedXml.gcmdVerticalResolution == ['> 1 Km'] as Set
    parsedXml.gcmdTemporalResolution == ['Seasonal'] as Set
  }

  def "Temporal bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def temporalBounding = MetadataParser.parseTemporalBounding(document)

    then:
    temporalBounding == [
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
  }

  def "Very old temporal bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-paleo-dates-metadata.xml").text

    when:
    def temporalBounding = MetadataParser.parseTemporalBounding(document)

    then:
    temporalBounding == [
        beginDate           : null,
        beginIndeterminate  : null,
        beginYear           : -100000001, // Edge case!
        endDate             : '-1601050',
        endIndeterminate    : null,
        endYear             : -1601050,
        instant             : null,
        instantIndeterminate: null,
        description         : 'Start_Date: 6181000 cal yr BP; Stop_Date: 1603000 cal yr BP; '
    ]
  }

  def "Temporal bounding without time zone information is correctly parsed with UTC"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-no-timezone-dates-metadata.xml").text

    when:
    def temporalBounding = MetadataParser.parseTemporalBounding(document)

    then:
    temporalBounding == [
        beginDate           : '2005-05-09T00:00:00Z',
        beginIndeterminate  : null,
        beginYear           : 2005,
        endDate             : '2010-10-01T00:00:00Z',
        endIndeterminate    : null,
        endYear             : 2010,
        instant             : null,
        instantIndeterminate: null,
        description         : null
    ]
  }

  def "Invalid temporal bounding is prevented"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-invalid-dates-metadata.xml").text

    when:
    MetadataParser.parseTemporalBounding(document)

    then:
    thrown(DateTimeParseException)
  }

  def "Polygon spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def result = MetadataParser.parseSpatialInfo(document)

    then:
    result == [
        spatialBounding: [
            type       : 'Polygon',
            coordinates: [
                [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]
            ]
        ],
        isGlobal       : true
    ]
  }

  def "Spatial bounding is correctly parsed when it contains zeros"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-zero-bounding-box-metadata.xml").text

    when:
    def result = MetadataParser.parseSpatialInfo(document)

    then:
    result == [
        spatialBounding: [
            type       : 'Polygon',
            coordinates: [
                [[-177, 0], [-66, 0], [-66, 61], [-177, 61], [-177, 0]]
            ]
        ],
        isGlobal       : false
    ]
  }

  def "Point spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-point-cords-metadata.xml").text

    when:
    def spatialBounding = MetadataParser.parseSpatialInfo(document)

    then:
    spatialBounding == [
        spatialBounding: [
            type       : 'Point',
            coordinates: [-105, 40]
        ],
        isGlobal       : false
    ]
  }

  def "Null Spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-null-cords-metadata.xml").text

    when:
    def spatialBounding = MetadataParser.parseSpatialInfo(document)

    then:
    spatialBounding == [spatialBounding: null, isGlobal: false]
  }

  def "LineString spatial bounding is prevented"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-linestring-coords-metadata.xml").text

    when:
    def spatialBounding = MetadataParser.parseSpatialInfo(document)

    then:
    spatialBounding == [
        spatialBounding: [
            type       : 'LineString',
            coordinates: [[-80, -10],[80, -10]]
        ],
        isGlobal       : false
    ]
  }

  def "Acquisition info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseAcquisitionInfo(document)

    then:
    parsedXml.acquisitionInstruments == [
        [
            instrumentIdentifier : 'SII > Super Important Instrument',
            instrumentType       : 'sensor',
            instrumentDescription: 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
        ]
    ] as Set
    parsedXml.acquisitionOperations == [
        [
            operationDescription: null,
            operationIdentifier : 'Super Important Project',
            operationStatus     : null,
            operationType       : null
        ]
    ] as Set
    parsedXml.acquisitionPlatforms == [
        [
            platformIdentifier : 'TS-18 > TumbleSat-18',
            platformDescription: 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
            platformSponsor    : ['Super Important Organization', 'Other (Kind Of) Important Organization']
        ]
    ] as Set
  }

  def "Data formats are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def dataFormats = MetadataParser.parseDataFormats(document)

    then:
    dataFormats == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ] as Set
  }

  def "Links are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def links = MetadataParser.parseLinks(document)

    then:
    links == [[
                  linkName       : 'Super Important Access Link',
                  linkProtocol   : 'HTTP',
                  linkUrl        : 'http://www.example.com',
                  linkDescription: 'Everything Important, All In One Place',
                  linkFunction   : 'search'
              ]] as Set
  }

  def "Responsible parties are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def responsibleParties = MetadataParser.parseDataResponsibleParties(document)

    then:
    responsibleParties.contacts == [
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

    responsibleParties.creators == [
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

    responsibleParties.publishers == [
        [
            individualName  : null,
            organizationName: 'Super Important Organization',
            positionName    : null,
            role            : 'publisher',
            email           : 'email@sio.co',
            phone           : '555-123-4567'
        ],
    ] as Set
  }

  def "DSMM scores are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def dsmm = MetadataParser.parseDSMM(document)

    then:
    dsmm.Accessibility == 4
    dsmm.DataIntegrity == 0
    dsmm.DataQualityAssessment == 2
    dsmm.DataQualityAssurance == 3
    dsmm.DataQualityControlMonitoring == 1
    dsmm.Preservability == 5
    dsmm.ProductionSustainability == 4
    dsmm.TransparencyTraceability == 2
    dsmm.Usability == 3
    dsmm.average == ((dsmm.Accessibility + dsmm.DataIntegrity + dsmm.DataQualityAssessment + dsmm.DataQualityAssurance +
        dsmm.DataQualityControlMonitoring + dsmm.Preservability + dsmm.ProductionSustainability +
        dsmm.TransparencyTraceability + dsmm.Usability) / (dsmm.size() - 1))
  }

  def "Services are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def serviceTextBlobs = MetadataParser.parseServices(document)
    serviceTextBlobs.each { String s ->
      // blob of XML needs to be base64 encoded for elastic search to include is as 'binary' type
      // decoding here (for testing purposes) to see if the original string is XML or causes a parsing exception
      byte[] decodedXML = s.decodeBase64()
      String xmlString = new String(decodedXML)
      new XmlSlurper().parseText(xmlString)
    }

    then:
    notThrown(Exception)
    serviceTextBlobs.each { String s ->
      assert s in String
      byte[] decodedXML = s.decodeBase64()
      String xmlString = new String(decodedXML)
      def serviceNode = new XmlSlurper().parseText(xmlString)
      assert serviceNode.name() == 'SV_ServiceIdentification'
    }
  }

  def "Miscellaneous items are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def miscellaneous = MetadataParser.parseMiscellaneous(document)

    then:
    miscellaneous.updateFrequency == 'asNeeded'
    miscellaneous.presentationForm == 'tableDigital'
  }
}
