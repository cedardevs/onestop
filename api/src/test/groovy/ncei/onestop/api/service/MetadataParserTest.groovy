package ncei.onestop.api.service

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
    parsedXml.doi == 'doi:10.5072/FK2TEST'
    parsedXml.title == 'Important Organization\'s Important File\'s Super Important Title'
    parsedXml.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    parsedXml.description == 'Wall of overly detailed, super informative, extra important text.'
    parsedXml.keywords == ['Air temperature', 'Water temperature', 'Wind speed', 'Wind direction'] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScience == [
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans > Salinity/Density > Salinity',
        'Volcanoes > This Keyword > Is Invalid'
    ] as Set
    parsedXml.gcmdLocations == [
        'Geographic Region > Arctic',
        'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
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
        type        : 'envelope',
        coordinates : [
            [-180, 90],
            [180, -90]
        ]
    ]
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
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'custodian'
        ],
        [
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'publisher'
        ],
        [
            individualName   : 'John Smith',
            organizationName : 'University of Awesome',
            role             : 'pointOfContact'
        ],
        [
            individualName   : null,
            organizationName : 'Global Change Data Center, Science and Exploration Directorate, Goddard Space Flight Center (GSFC) National Aeronautics and Space Administration (NASA)',
            role             : 'custodian'
        ],
        [
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'sponsor'
        ],
        [
            individualName   : null,
            organizationName : 'Other (Kind Of) Important Organization',
            role             : 'sponsor'
        ]
    ] as Set
    parsedXml.thumbnail == 'https://www.example.com/image.png'
    parsedXml.modifiedDate == '2016-12-25T11:12:13'
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

  }

  def "Identifier info is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def idInfo = MetadataParser.parseIdentifierInfo(document)

    then:
    idInfo.id == 'doi:10.5072-FK2TEST'
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
    citationInfo.doi == 'doi:10.5072/FK2TEST'
    citationInfo.title == 'Important Organization\'s Important File\'s Super Important Title'
    citationInfo.alternateTitle == 'Still (But Slightly Less) Important Alternate Title'
    citationInfo.description == 'Wall of overly detailed, super informative, extra important text.'
    citationInfo.thumbnail == 'https://www.example.com/image.png'
    citationInfo.modifiedDate == '2016-12-25T11:12:13'
    citationInfo.creationDate == null
    citationInfo.revisionDate == '2011-01-02'
    citationInfo.publicationDate == '2010-11-15'
  }

  def "Keywords and topics are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def parsedXml = MetadataParser.parseKeywordsAndTopics(document)

    then:
    parsedXml.keywords == ['Air temperature', 'Water temperature', 'Wind speed', 'Wind direction'] as Set
    parsedXml.topicCategories == ['environment', 'oceans'] as Set
    parsedXml.gcmdScience == [
        'Atmosphere > Atmospheric Temperature > Surface Temperature > Dew Point Temperature',
        'Oceans > Salinity/Density > Salinity',
        'Volcanoes > This Keyword > Is Invalid'
    ] as Set
    parsedXml.gcmdLocations == [
        'Geographic Region > Arctic',
        'Ocean > Atlantic Ocean > North Atlantic Ocean > Gulf Of Mexico',
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

  def "Spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def spatialBounding = MetadataParser.parseSpatialBounding(document)

    then:
    spatialBounding == [
        type        : 'envelope',
        coordinates : [
            [-180, 90],
            [180, -90]
        ]
    ]
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

  def "Contacts are correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text

    when:
    def contacts = MetadataParser.parseContacts(document)

    then:
    contacts == [
        [
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'custodian'
        ],
        [
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'publisher'
        ],
        [
            individualName   : 'John Smith',
            organizationName : 'University of Awesome',
            role             : 'pointOfContact'
        ],
        [
            individualName   : null,
            organizationName : 'Global Change Data Center, Science and Exploration Directorate, Goddard Space Flight Center (GSFC) National Aeronautics and Space Administration (NASA)',
            role             : 'custodian'
        ],
        [
            individualName   : null,
            organizationName : 'Super Important Organization',
            role             : 'sponsor'
        ],
        [
            individualName   : null,
            organizationName : 'Other (Kind Of) Important Organization',
            role             : 'sponsor'
        ]
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
}
