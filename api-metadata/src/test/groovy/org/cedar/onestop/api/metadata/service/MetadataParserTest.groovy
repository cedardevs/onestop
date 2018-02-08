package org.cedar.onestop.api.metadata.service

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MetadataParserTest extends Specification {

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
    parsedXml.keywords == [
        'Air temperature', 'Water temperature', 'Wind speed', 'Wind direction',
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans > Salinity/Density > Salinity',
        'Volcanoes > This Keyword > Is Invalid',
        'Geographic Region > Arctic',
        'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
        'Liquid Earth > This Keyword > Is Invalid',
        'SIO > Super Important Organization'
    ] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScience == [
        'Atmosphere',
        'Atmosphere > Atmospheric Temperature',
        'Atmosphere > Atmospheric Temperature > Surface Temperature',
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans',
        'Oceans > Salinity/Density',
        'Oceans > Salinity/Density > Salinity',
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
    parsedXml.gcmdDataCenters == ['SIO > Super Important Organization'] as Set
    parsedXml.gcmdDataResolution == [] as Set
    parsedXml.temporalBounding == [
        beginDate: '2005-05-09',
        beginIndeterminate: null,
        endDate: null,
        endIndeterminate: 'now',
        instant: null,
        instantIndeterminate: null
    ]
    parsedXml.spatialBounding == [
        type        : 'Polygon',
        coordinates : [
            [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]
        ]
    ]
    parsedXml.isGlobal == true
    parsedXml.acquisitionInstruments == [[
        instrumentIdentifier  : 'SII > Super Important Instrument',
        instrumentType        : 'sensor',
        instrumentDescription : 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
    ]] as Set
    parsedXml.acquisitionOperations == [[
        operationDescription : null,
        operationIdentifier  : 'Super Important Project',
        operationStatus      : null,
        operationType        : null
    ]] as Set
    parsedXml.dataFormats == [
        'NETCDF',
        'ASCII',
        'CSV'
    ] as Set
    parsedXml.acquisitionPlatforms == [[
        platformIdentifier  : 'TS-18 > TumbleSat-18',
        platformDescription : 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
        platformSponsor     : ['Super Important Organization', 'Other (Kind Of) Important Organization']
    ]] as Set
    parsedXml.links == [[
        linkName        : 'Super Important Access Link',
        linkProtocol    : 'HTTP',
        linkUrl         : 'http://www.example.com',
        linkDescription : 'Everything Important, All In One Place',
        linkFunction    : 'search'
    ]] as Set

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

  def "Descriptive info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def descriptiveInfo = MetadataParser.parseDescriptiveInfo(document)

    then:
    descriptiveInfo.fileIdentifier == 'gov.super.important:FILE-ID'
    descriptiveInfo.parentIdentifier == 'gov.super.important:PARENT-ID'
    descriptiveInfo.hierarchyLevelName == 'granule'
    descriptiveInfo.doi == 'doi:10.5072/FK2TEST'
    descriptiveInfo.purpose == 'Provide quality super important data to the user community.'
    descriptiveInfo.status == 'completed'
    descriptiveInfo.credit == null
    descriptiveInfo.title == 'Important Organization\'s Important File\'s Super Important Title'
    descriptiveInfo.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    descriptiveInfo.description == 'Wall of overly detailed, super informative, extra important text.'
    descriptiveInfo.thumbnail == 'https://www.example.com/exportImage?soCool=yes&format=png'
    descriptiveInfo.thumbnailDescription == 'Preview graphic'
    descriptiveInfo.creationDate == null
    descriptiveInfo.revisionDate == '2011-01-02'
    descriptiveInfo.publicationDate == '2010-11-15'
  }

  def "Keywords and topics are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseKeywordsAndTopics(document)

    then:
    parsedXml.keywords == [
        'Air temperature', 'Water temperature', 'Wind speed', 'Wind direction',
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans > Salinity/Density > Salinity',
        'Volcanoes > This Keyword > Is Invalid',
        'Geographic Region > Arctic',
        'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
        'Liquid Earth > This Keyword > Is Invalid',
        'SIO > Super Important Organization'
    ] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScience == [
        'Atmosphere',
        'Atmosphere > Atmospheric Temperature',
        'Atmosphere > Atmospheric Temperature > Surface Temperature',
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans',
        'Oceans > Salinity/Density',
        'Oceans > Salinity/Density > Salinity',
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
    parsedXml.gcmdDataCenters == ['SIO > Super Important Organization'] as Set
    parsedXml.gcmdDataResolution == [] as Set
  }

  def "Temporal bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def temporalBounding = MetadataParser.parseTemporalBounding(document)

    then:
    temporalBounding == [
        beginDate: '2005-05-09',
        beginIndeterminate: null,
        endDate: null,
        endIndeterminate: 'now',
        instant: null,
        instantIndeterminate: null
    ]
  }

  def "Polygon spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def result = MetadataParser.parseSpatialInfo(document)

    then:
    result == [
        spatialBounding: [
            type: 'Polygon',
            coordinates: [
                [[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]
            ]
        ],
        isGlobal: true
    ]
  }

  def "Point spatial bounding is correctly parsed"(){
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-point-cords-metadata.xml").text

    when:
    def spatialBounding = MetadataParser.parseSpatialInfo(document)

    then:
    spatialBounding == [
        spatialBounding: [
            type: 'Point',
            coordinates: [-105, 40]
        ],
        isGlobal: false
    ]
  }

  def "Null Spatial bounding is correctly parsed"(){
      given:
      def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-null-cords-metadata.xml").text

      when:
      def spatialBounding = MetadataParser.parseSpatialInfo(document)

      then:
      spatialBounding == [spatialBounding: null, isGlobal: false]
  }

  def "Acquisition info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseAcquisitionInfo(document)

    then:
    parsedXml.acquisitionInstruments == [[
                                             instrumentIdentifier  : 'SII > Super Important Instrument',
                                             instrumentType        : 'sensor',
                                             instrumentDescription : 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
                                         ]] as Set
    parsedXml.acquisitionOperations == [[
                                            operationDescription : null,
                                            operationIdentifier  : 'Super Important Project',
                                            operationStatus      : null,
                                            operationType        : null
                                        ]] as Set
    parsedXml.acquisitionPlatforms == [[
                                           platformIdentifier  : 'TS-18 > TumbleSat-18',
                                           platformDescription : 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.',
                                           platformSponsor     : ['Super Important Organization', 'Other (Kind Of) Important Organization']
                                       ]] as Set
  }

  def "Data formats are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def dataFormats = MetadataParser.parseDataFormats(document)

    then:
    dataFormats == [
        'NETCDF',
        'ASCII',
        'CSV'
    ] as Set
  }

  def "Links are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def links = MetadataParser.parseLinks(document)

    then:
    links == [[
                  linkName        : 'Super Important Access Link',
                  linkProtocol    : 'HTTP',
                  linkUrl         : 'http://www.example.com',
                  linkDescription : 'Everything Important, All In One Place',
                  linkFunction    : 'search'
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
        dsmm.TransparencyTraceability + dsmm.Usability) / ( dsmm.size() - 1 ) )
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
