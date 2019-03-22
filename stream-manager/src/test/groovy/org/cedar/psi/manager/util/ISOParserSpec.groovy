package org.cedar.psi.manager.util

import org.cedar.schemas.avro.geojson.LineStringType
import org.cedar.schemas.avro.geojson.PointType
import org.cedar.schemas.avro.geojson.PolygonType
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ISOParserSpec extends Specification {

  def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-metadata.xml").text
  def metadata = new XmlSlurper().parseText(document)

  def "Citation info is correctly parsed"() {
    when:
    def citationInfo = ISOParser.parseCitationInfo(metadata)

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

    citationInfo.crossReferences.every { it instanceof Reference }
    citationInfo.crossReferences.collect { AvroUtils.avroToMap(it) } == [
        [
            title: '[TITLE OF PUBLICATION]',
            date : '9999-01-01',
            links: [new Link(
                linkName: null,
                linkProtocol: null,
                linkUrl: 'HTTPS://WWW.EXAMPLE.COM',
                linkDescription: '[DESCRIPTION OF URL]',
                linkFunction: 'information'
            )]
        ]
    ]

    citationInfo.largerWorks.every { it instanceof Reference }
    citationInfo.largerWorks.collect { AvroUtils.avroToMap(it) } == [
        [
            title: '[TITLE OF PROJECT]',
            date : '9999-10-10',
            links: []
        ]
    ]

    citationInfo.useLimitation == '[NOAA LEGAL STATEMENT]'
    citationInfo.legalConstraints == ['[CITE AS STATEMENT 1]', '[CITE AS STATEMENT 2]'] as Set
    citationInfo.accessFeeStatement == 'template fees'
    citationInfo.orderingInstructions == 'template ordering instructions'
    citationInfo.edition == '[EDITION]'
  }

  def "Keywords and topics are correctly parsed"() {
    when:
    def parsedXml = ISOParser.parseKeywordsAndTopics(metadata)

    then:
    // Deep equality check
    def keywordMaps = parsedXml.keywords.collect { AvroUtils.avroToMap(it) }
    keywordMaps == [
        [
            "values"   : [
                "SIO > Super Important Organization",
                "OSIO > OTHER SUPER IMPORTANT ORGANIZATION",
                "SSIO > Super SIO (Super Important Organization)"
            ],
            "type"     : "dataCenter",
            "namespace": "GCMD Keywords - Data Centers"
        ],
        [
            "values"   : [
                "0038924",
                "0038947",
                "0038970"
            ],
            "type"     : null,
            "namespace": "NCEI ACCESSION NUMBER"
        ],
        [
            "values"   : [
                "EARTH SCIENCE SERVICES > ENVIRONMENTAL ADVISORIES > FIRE ADVISORIES > WILDFIRES",
                "EARTH SCIENCE > This Keyword is > Misplaced and Invalid",
                "This Keyword > Is Just > WRONG"
            ],
            "type"     : "service",
            "namespace": "Global Change Master Directory Science and Services Keywords"
        ],
        [
            "values"   : [
                "Air temperature",
                "Water temperature"
            ],
            "type"     : "theme",
            "namespace": "Miscellaneous keyword type"
        ],
        [
            "values"   : [
                "Wind speed",
                "Wind direction"
            ],
            "type"     : "theme",
            "namespace": "Miscellaneous keyword type"
        ],
        [
            "values"   : [
                "EARTH SCIENCE > ATMOSPHERE > ATMOSPHERIC TEMPERATURE > SURFACE TEMPERATURE > DEW POINT TEMPERATURE",
                "EARTH SCIENCE > OCEANS > SALINITY/DENSITY > SALINITY",
                "EARTH SCIENCE > VOLCANOES > THIS KEYWORD > IS INVALID",
                "Earth Science > Spectral/Engineering > microwave > Brightness Temperature",
                "Earth Science > Spectral/Engineering > microwave > Temperature Anomalies"
            ],
            "type"     : "theme",
            "namespace": "GCMD Keywords - Science Keywords"
        ],
        [
            "values"   : [
                "GEOGRAPHIC REGION > ARCTIC",
                "OCEAN > ATLANTIC OCEAN > NORTH ATLANTIC OCEAN > GULF OF MEXICO",
                "LIQUID EARTH > THIS KEYWORD > IS INVALID"
            ],
            "type"     : "place",
            "namespace": "GCMD Keywords - Locations"
        ],
        [
            "values"   : [
                "SEASONAL"
            ],
            "type"     : "dataResolution",
            "namespace": "Global Change Master Directory Keywords - Temporal Data Resolution"
        ],
        [
            "values"   : [
                "> 1 km"
            ],
            "type"     : "dataResolution",
            "namespace": "GCMD Keywords - Vertical Data Resolution"
        ]
    ]
    parsedXml.topicCategories == ['environment', 'oceans']

  }

  def "Temporal bounding is correctly parsed"() {
    given:
    def output = TemporalBounding.newBuilder()
        .setBeginDate('2005-05-09T00:00:00Z')
        .setBeginIndeterminate(null)
        .setEndDate('2010-10-01')
        .setEndIndeterminate(null)
        .setInstant(null)
        .setInstantIndeterminate(null)
        .setDescription(null)
        .build()

    when:
    def temporalBounding = ISOParser.parseTemporalBounding(metadata)

    then:
    temporalBounding == output
  }

  def "Polygon spatial bounding is correctly parsed"() {
    when:
    def result = ISOParser.parseSpatialInfo(metadata)

    then:
    result.spatialBounding.coordinates == [[[-180, -90], [180, -90], [180, 90], [-180, 90], [-180, -90]]]
    result.spatialBounding.type == PolygonType.Polygon
    !result.isGlobal
  }


  def "Spatial bounding is correctly parsed when it contains zeros"() {

    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-zero-coords-metadata.xml").text
    def metadata = new XmlSlurper().parseText(document)
    // given:
    // def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-zero-coords-metadata.xml").text
    // def metadata = new XmlSlurper().parseText(document)

    when:
    def result = ISOParser.parseSpatialInfo(metadata)

    then:
    result.spatialBounding.coordinates == [[[-177, 0], [-66, 0], [-66, 61], [-177, 61], [-177, 0]]]
    result.spatialBounding.type == PolygonType.Polygon
    !result.isGlobal
  }

  def "Point spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-point-coords-metadata.xml").text
    def metadata = new XmlSlurper().parseText(document)

    when:
    def spatialBounding = ISOParser.parseSpatialInfo(metadata)

    then:
    spatialBounding.spatialBounding.coordinates == [-105, 40]
    spatialBounding.spatialBounding.type == PointType.Point
    !spatialBounding.isGlobal
  }

  def "Null Spatial bounding is correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-null-cords-metadata.xml").text
    def metadata = new XmlSlurper().parseText(document)

    when:
    def spatialBounding = ISOParser.parseSpatialInfo(metadata)

    then:
    spatialBounding == [spatialBounding: null, isGlobal: false]
  }

  def "LineString spatial bounding correctly parsed"() {
    given:
    def document = ClassLoader.systemClassLoader.getResourceAsStream("test-iso-linestring-coords-metadata.xml").text
    def metadata = new XmlSlurper().parseText(document)

    when:
    def spatialBounding = ISOParser.parseSpatialInfo(metadata)

    then:
    spatialBounding.spatialBounding.coordinates == [[-80, -10], [80, -10]]
    spatialBounding.spatialBounding.type == LineStringType.LineString
    !spatialBounding.isGlobal
  }

  def "AcquisitionInstruments info is correctly parsed"() {
    when:
    def result = ISOParser.acquisitionInstruments(metadata)

    then:
    result instanceof List
    result.every { it instanceof Instruments }
    result.size() == 1
    result[0].instrumentIdentifier == 'SII > Super Important Instrument'
    result[0].instrumentType == 'sensor'
    result[0].instrumentDescription == 'The Super Important Organization\'s (SIO) Super Important Instrument (SII) is a really impressive sensor designed to provide really important information from the TumbleSat system.'
  }

  def "AcquisitionOperations info is correctly parsed"() {
    when:
    def result = ISOParser.acquisitionOperations(metadata)

    then:
    result instanceof List
    result.every { it instanceof Operation }
    result.size() == 1
    result[0].operationDescription == null
    result[0].operationIdentifier == 'Super Important Project'
    result[0].operationStatus == null
    result[0].operationType == null
  }

  def "AcquisitionPlatforms info is correctly parsed"() {
    when:
    def result = ISOParser.acquisitionPlatforms(metadata)

    then:
    result instanceof List
    result.every { it instanceof Platform }
    result.size() == 1
    result[0].platformIdentifier == 'TS-18 > TumbleSat-18'
    result[0].platformDescription == 'The TumbleSat satellite system offers the advantage of daily surprise coverage, with morning and afternoon orbits that collect and deliver data in every direction. The information received includes brief glimpses of earth, other satellites, and the universe beyond, as the system spirals out of control.'
    result[0].platformSponsor == ['Super Important Organization', 'Other (Kind Of) Important Organization']
  }

  def "Data formats are correctly parsed"() {
    when:
    def dataFormats = ISOParser.parseDataFormats(metadata)

    then:
    dataFormats instanceof List
    dataFormats.every { it instanceof DataFormat }
    def mapResults = dataFormats.collect({ AvroUtils.avroToMap(it) }) as Set
    mapResults == [
        [name: 'NETCDF', version: 'classic'],
        [name: 'NETCDF', version: '4'],
        [name: 'ASCII', version: null],
        [name: 'CSV', version: null]
    ] as Set
  }

  def "Links are correctly parsed"() {
    when:
    def links = ISOParser.parseLinks(metadata)

    then:
    links instanceof List
    links.every { it instanceof Link }
    links.size() == 1
    links[0].linkName == 'Super Important Access Link'
    links[0].linkProtocol == 'HTTP'
    links[0].linkUrl == 'http://www.example.com'
    links[0].linkDescription == 'Everything Important, All In One Place'
    links[0].linkFunction == 'search'
  }

  def "Responsible parties are correctly parsed"() {
    when:
    def responsibleParties = ISOParser.parseResponsibleParties(metadata)

    then:
    responsibleParties instanceof List
    responsibleParties.every { it instanceof ResponsibleParty }
    def mapResults = responsibleParties.collect { AvroUtils.avroToMap(it) }
    mapResults == [
        [
            individualName  : null,
            organizationName: 'Super Important Organization',
            positionName    : null,
            role            : 'publisher',
            email           : 'email@sio.co',
            phone           : '555-123-4567'
        ],
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
        [
            individualName  : null,
            organizationName: 'Global Change Data Center, Science and Exploration Directorate, Goddard Space Flight Center (GSFC) National Aeronautics and Space Administration (NASA)',
            positionName    : null,
            role            : 'custodian',
            email           : null,
            phone           : null
        ]
    ]
  }

  def "DSMM scores are correctly parsed"() {
    when:
    def dsmm = ISOParser.parseDSMM(metadata)

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
    Map expectedResult = [
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
        operations    : [
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
    ]
    when:
    def result = ISOParser.parseServices(metadata)

    then:
    noExceptionThrown()
    result instanceof List
    result.size() == 1
    result[0] instanceof Service
    AvroUtils.avroToMap(result[0], true) == expectedResult
  }

  def "Miscellaneous items are correctly parsed"() {
    when:
    def miscellaneous = ISOParser.parseMiscellaneous(metadata)

    then:
    miscellaneous.updateFrequency == 'asNeeded'
    miscellaneous.presentationForm == 'tableDigital'
  }
}
