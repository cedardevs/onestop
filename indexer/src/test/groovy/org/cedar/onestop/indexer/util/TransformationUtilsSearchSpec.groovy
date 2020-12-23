package org.cedar.onestop.indexer.util

import org.cedar.onestop.mapping.search.SearchCollection
import org.cedar.onestop.mapping.search.SearchGranule
import org.cedar.schemas.analyze.Temporal
import org.cedar.schemas.avro.psi.Checksum
import org.cedar.schemas.avro.psi.ChecksumAlgorithm
import org.cedar.schemas.avro.psi.DataFormat
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.FileInformation
import org.cedar.schemas.avro.psi.Link
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Reference
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.psi.Service
import org.cedar.schemas.avro.psi.TemporalBounding
import org.cedar.schemas.avro.geojson.PolygonType
// import com.mapbox.geojson.Polygon
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class TransformationUtilsSearchSpec extends Specification {

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

  def "handles timestamp #label"() {
    when:
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(Discovery.newBuilder().build()).build()
    def indexedGranule = TransformationUtils.reformatGranuleForSearch(time, record)
    def indexedCollection = TransformationUtils.reformatCollectionForSearch(time, record)

    then:
    indexedGranule.getStagedDate() == time
    indexedCollection.getStagedDate() == time

    where:
    label | time
    'Thursday, July 29, 2010 5:32:16 PM' | 1280424736L
    'Saturday, January 1, 2000 12:00:00 PM' | 946728000L
  }

  def "produces checksums for granule record"() {
    when:
    FileInformation fileInfo = FileInformation.newBuilder().setChecksums([
      Checksum.newBuilder().setValue("1234abcd").setAlgorithm(ChecksumAlgorithm.MD5).build(),
      Checksum.newBuilder().setValue("qwerty").setAlgorithm(ChecksumAlgorithm.SHA1).build()
      ]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setFileInformation(fileInfo).setDiscovery(Discovery.newBuilder().build()).build()
    def search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getChecksums().size() == 2
  }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////

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

  def "populates search - internalParentIdentifier for granule"() {
    when:
    def testId = "ABC"
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(Discovery.newBuilder().build()).setRelationships([
        Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId(testId).build()
    ]).build()
    def search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getInternalParentIdentifier() == testId
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

  def "populates search - filename for granule"() {
    when:
    FileInformation fileInfo = FileInformation.newBuilder().setName("file_name.txt").build()
    ParsedRecord record = ParsedRecord.newBuilder().setFileInformation(fileInfo).setDiscovery(Discovery.newBuilder().build()).build()
    def search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getFilename() == "file_name.txt"
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////

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

  def "populates search link and service link protocols"() {
    when:
    def discovery = Discovery.newBuilder().setLinks([
        Link.newBuilder().setLinkProtocol("HTTP").build(),
        Link.newBuilder().setLinkProtocol("https").build()
      ]).setServices([
        Service.newBuilder().setOperations([
          Link.newBuilder().setLinkProtocol("FTP").build()
        ]).build()
      ]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getLinkProtocol().size() == 2
    search.getLinkProtocol() == ["HTTP", "HTTPS"] as Set
    search.getServiceLinkProtocol().size() == 1
    search.getServiceLinkProtocol() == ["FTP"] as Set

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getLinkProtocol().size() == 2
    search.getLinkProtocol() == ["HTTP", "HTTPS"] as Set
    search.getServiceLinkProtocol().size() == 1
    search.getServiceLinkProtocol() == ["FTP"] as Set
  }

  def "populates search service links"() {
    when:
    def discovery = Discovery.newBuilder().setServices([
        Service.newBuilder().setTitle("ABC").setAlternateTitle("the ABC service").setDescription("A service that serves ABC.").setOperations([
          Link.newBuilder().setLinkName("A").setLinkUrl("example.com/A").setLinkDescription("ABC - A").setLinkFunction("info").setLinkProtocol("http").build(),
          Link.newBuilder().setLinkName("B").setLinkUrl("example.com/B").setLinkDescription("ABC - B").setLinkFunction("info").setLinkProtocol("http").build(),
          Link.newBuilder().setLinkName("C").setLinkUrl("example.com/C").setLinkDescription("ABC - C").setLinkFunction("info").setLinkProtocol("http").build()
        ]).build(),
        Service.newBuilder().setTitle("123").setDescription("A 123 service.").setOperations([
          Link.newBuilder().setLinkName("123").setLinkUrl("example.com/123").setLinkFunction("download").setLinkProtocol("https").build()
        ]).build()
      ]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getServiceLinks().size() == 2
    search.getServiceLinks()[0].getTitle() == "ABC"
    search.getServiceLinks()[0].getAlternateTitle() == "the ABC service"
    search.getServiceLinks()[0].getDescription() == "A service that serves ABC."
    search.getServiceLinks()[0].getLinks().size() == 3
    search.getServiceLinks()[0].getLinks()[0].getLinkName() == "A"
    search.getServiceLinks()[0].getLinks()[0].getLinkUrl() == "example.com/A"
    search.getServiceLinks()[0].getLinks()[0].getLinkDescription() == "ABC - A"
    search.getServiceLinks()[0].getLinks()[0].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[0].getLinkProtocol() == "http"
    search.getServiceLinks()[0].getLinks()[1].getLinkName() == "B"
    search.getServiceLinks()[0].getLinks()[1].getLinkUrl() == "example.com/B"
    search.getServiceLinks()[0].getLinks()[1].getLinkDescription() == "ABC - B"
    search.getServiceLinks()[0].getLinks()[1].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[1].getLinkProtocol() == "http"
    search.getServiceLinks()[0].getLinks()[2].getLinkName() == "C"
    search.getServiceLinks()[0].getLinks()[2].getLinkUrl() == "example.com/C"
    search.getServiceLinks()[0].getLinks()[2].getLinkDescription() == "ABC - C"
    search.getServiceLinks()[0].getLinks()[2].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[2].getLinkProtocol() == "http"

    search.getServiceLinks()[1].getTitle() == "123"
    search.getServiceLinks()[1].getAlternateTitle() == null
    search.getServiceLinks()[1].getDescription() == "A 123 service."
    search.getServiceLinks()[1].getLinks().size() == 1
    search.getServiceLinks()[1].getLinks()[0].getLinkName() == "123"
    search.getServiceLinks()[1].getLinks()[0].getLinkUrl() == "example.com/123"
    search.getServiceLinks()[1].getLinks()[0].getLinkDescription() == null
    search.getServiceLinks()[1].getLinks()[0].getLinkFunction() == "download"
    search.getServiceLinks()[1].getLinks()[0].getLinkProtocol() == "https"

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getServiceLinks().size() == 2
    search.getServiceLinks()[0].getTitle() == "ABC"
    search.getServiceLinks()[0].getAlternateTitle() == "the ABC service"
    search.getServiceLinks()[0].getDescription() == "A service that serves ABC."
    search.getServiceLinks()[0].getLinks().size() == 3
    search.getServiceLinks()[0].getLinks()[0].getLinkName() == "A"
    search.getServiceLinks()[0].getLinks()[0].getLinkUrl() == "example.com/A"
    search.getServiceLinks()[0].getLinks()[0].getLinkDescription() == "ABC - A"
    search.getServiceLinks()[0].getLinks()[0].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[0].getLinkProtocol() == "http"
    search.getServiceLinks()[0].getLinks()[1].getLinkName() == "B"
    search.getServiceLinks()[0].getLinks()[1].getLinkUrl() == "example.com/B"
    search.getServiceLinks()[0].getLinks()[1].getLinkDescription() == "ABC - B"
    search.getServiceLinks()[0].getLinks()[1].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[1].getLinkProtocol() == "http"
    search.getServiceLinks()[0].getLinks()[2].getLinkName() == "C"
    search.getServiceLinks()[0].getLinks()[2].getLinkUrl() == "example.com/C"
    search.getServiceLinks()[0].getLinks()[2].getLinkDescription() == "ABC - C"
    search.getServiceLinks()[0].getLinks()[2].getLinkFunction() == "info"
    search.getServiceLinks()[0].getLinks()[2].getLinkProtocol() == "http"

    search.getServiceLinks()[1].getTitle() == "123"
    search.getServiceLinks()[1].getAlternateTitle() == null
    search.getServiceLinks()[1].getDescription() == "A 123 service."
    search.getServiceLinks()[1].getLinks().size() == 1
    search.getServiceLinks()[1].getLinks()[0].getLinkName() == "123"
    search.getServiceLinks()[1].getLinks()[0].getLinkUrl() == "example.com/123"
    search.getServiceLinks()[1].getLinks()[0].getLinkDescription() == null
    search.getServiceLinks()[1].getLinks()[0].getLinkFunction() == "download"
    search.getServiceLinks()[1].getLinks()[0].getLinkProtocol() == "https"
  }

  def "populates search links"() {
    when:
    def discovery = Discovery.newBuilder().setLinks([
        Link.newBuilder().setLinkName("1").setLinkUrl("example.com/1").setLinkFunction("info").setLinkProtocol("http").build(),
        Link.newBuilder().setLinkName("2").setLinkUrl("example.com/2").setLinkDescription("1st download").setLinkFunction("download").setLinkProtocol("http").build(),
        Link.newBuilder().setLinkName("3").setLinkUrl("example.com/3").setLinkDescription("2nd download").setLinkFunction("download").setLinkProtocol("https").build()
      ]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getLinks().size() == 3
    search.getLinks()[0].getLinkName() == "1"
    search.getLinks()[0].getLinkUrl() == "example.com/1"
    search.getLinks()[0].getLinkDescription() == null
    search.getLinks()[0].getLinkFunction() == "info"
    search.getLinks()[0].getLinkProtocol() == "http"
    search.getLinks()[1].getLinkName() == "2"
    search.getLinks()[1].getLinkUrl() == "example.com/2"
    search.getLinks()[1].getLinkDescription() == "1st download"
    search.getLinks()[1].getLinkFunction() == "download"
    search.getLinks()[1].getLinkProtocol() == "http"
    search.getLinks()[2].getLinkName() == "3"
    search.getLinks()[2].getLinkUrl() == "example.com/3"
    search.getLinks()[2].getLinkDescription() == "2nd download"
    search.getLinks()[2].getLinkFunction() == "download"
    search.getLinks()[2].getLinkProtocol() == "https"

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getLinks().size() == 3
    search.getLinks()[0].getLinkName() == "1"
    search.getLinks()[0].getLinkUrl() == "example.com/1"
    search.getLinks()[0].getLinkDescription() == null
    search.getLinks()[0].getLinkFunction() == "info"
    search.getLinks()[0].getLinkProtocol() == "http"
    search.getLinks()[1].getLinkName() == "2"
    search.getLinks()[1].getLinkUrl() == "example.com/2"
    search.getLinks()[1].getLinkDescription() == "1st download"
    search.getLinks()[1].getLinkFunction() == "download"
    search.getLinks()[1].getLinkProtocol() == "http"
    search.getLinks()[2].getLinkName() == "3"
    search.getLinks()[2].getLinkUrl() == "example.com/3"
    search.getLinks()[2].getLinkDescription() == "2nd download"
    search.getLinks()[2].getLinkFunction() == "download"
    search.getLinks()[2].getLinkProtocol() == "https"
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

  def "populates search data formats"() {
    when:
    def discovery = Discovery.newBuilder().setDataFormats([DataFormat.newBuilder().setName("netCDF").setVersion('4').build(), DataFormat.newBuilder().setName("netcdf").build()]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getDataFormat().size() == 2
    search.getDataFormat() == ["NETCDF", "NETCDF > 4"] as Set

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getDataFormat().size() == 2
    search.getDataFormat() == ["NETCDF", "NETCDF > 4"] as Set
  }

  ////////////////////////////
  // Etc                    //
  ////////////////////////////

  def "populates search identifiers parent: #parentId file: #fileId doi: #doi"() {
    when:
    def discovery = Discovery.newBuilder().setParentIdentifier(parentId).setFileIdentifier(fileId).setDoi(doi).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getParentIdentifier() == parentId
    search.getFileIdentifier() == fileId
    search.getDoi() == doi

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getParentIdentifier() == parentId
    search.getFileIdentifier() == fileId
    search.getDoi() == doi

    where:
    parentId | fileId | doi
    'gov.noaa.nodc:1234567' | null | null
    null | 'gov.noaa.nodc:9876543' | null
    null | null | 'doi:10.7289/V5V985ZM'
  }

  def "populates search title and description and thumbnail"() {
    def title = "a record title"
    def description = "some sort of descriptive paragraph, explaining the record"
    def thumbnail = "example.com"

    when:
    def discovery = Discovery.newBuilder().setTitle(title).setDescription(description).setThumbnail(thumbnail).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getTitle() == title
    search.getDescription() == description
    search.getThumbnail() == thumbnail

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getTitle() == title
    search.getDescription() == description
    search.getThumbnail() == thumbnail
  }

  def "populates search larger works & cross references"() {
    when:
    def discovery = Discovery.newBuilder().setLargerWorks([
      Reference.newBuilder().setTitle("ref 1").setDate("2001-02-01").setLinks([
        Link.newBuilder().setLinkUrl("doi:10.7289/V5V985ZM").build()
        ]).build(),
      Reference.newBuilder().setTitle("ref 2").setDate("2011-01-01").build()
      ]).setCrossReferences([
        Reference.newBuilder().setTitle("see also").setLinks([
          Link.newBuilder().setLinkUrl("example.com").build()
          ]).build()
        ]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)
    // note: granule doesn't have these fields

    then:
    search.getLargerWorks().size() == 2
    search.getLargerWorks()[0].getTitle() == "ref 1"
    search.getLargerWorks()[0].getDate() == "2001-02-01"
    search.getLargerWorks()[0].getLinks().size() == 1
    search.getLargerWorks()[1].getTitle() == "ref 2"
    search.getLargerWorks()[1].getDate() == "2011-01-01"
    search.getLargerWorks()[1].getLinks().size() == 0
    search.getCrossReferences().size == 1
    search.getCrossReferences()[0].getTitle() == "see also"
    search.getCrossReferences()[0].getDate() == null
    search.getCrossReferences()[0].getLinks().size() == 1
  }

  def "populates search spatial #label"() {
    when:
    def placeholderBoundingObject = new HashMap<String,Object>()
    placeholderBoundingObject.put("type", PolygonType.Polygon)
    placeholderBoundingObject.put("coordinates", [[[-140.3989, 59.3811], [-139.4611, 59.3811], [-139.4611, 60.0611], [-140.3989, 60.0611], [-140.3989, 59.3811]]])
    def discovery = Discovery.newBuilder().setIsGlobal(isGlobal)
    // TODO switch schemas and EsMapping.kt to depend on mapbox Polygon for geojson?
//     .setSpatialBounding(Polygon.fromJson("""{
//     "type": "Polygon",
//     "coordinates": [
//         [
//             [
//                 100,
//                 0
//             ],
//             [
//                 101,
//                 0
//             ],
//             [
//                 101,
//                 1
//             ],
//             [
//                 100,
//                 1
//             ],
//             [
//                 100,
//                 0
//             ]
//         ]
//     ]
// }"""))
    .setSpatialBounding(placeholderBoundingObject)
    .build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getIsGlobal() == isGlobal
    search.getSpatialBounding() == placeholderBoundingObject

    when: 'repeat for granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getIsGlobal() == isGlobal
    search.getSpatialBounding() == placeholderBoundingObject

    where:
    label | isGlobal
    "global" | true
    "small" | false
  }

  def "populates search - misc fields"() {
    when:
    def discovery = Discovery.newBuilder().setDsmmAverage(3.14).setEdition("1.0").setOrderingInstructions("hello").setAccessFeeStatement("world").setUseLimitation("use").setLegalConstraints(["legal", "constraint"]).setCiteAsStatements(["citations"]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getDsmmAverage() - 3.14f < 0.001
    search.getEdition() == "1.0"
    search.getOrderingInstructions() == "hello"
    search.getAccessFeeStatement() == "world"
    search.getLegalConstraints() == ["legal", "constraint"]
    search.getUseLimitation() == "use"
    search.getCiteAsStatements() == ["citations"]

    when: 'granule'
    search = TransformationUtils.reformatGranuleForSearch(12341234L, record)
    // note: granule only has cite as statements, of these fields

    then:
    search.getCiteAsStatements() == ["citations"]
  }

  def "produces filesize for granule record correctly"() {
    def filesize = 1234567890
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord)
            .setType(RecordType.granule)
            .setFileInformation(FileInformation.newBuilder().setSize(filesize).build())
            .build()

    expect:
    TransformationUtils.prepareFilesize(record) == filesize
  }

  def "populates search - filesize for granule"() {
    when:
    FileInformation fileInfo = FileInformation.newBuilder().setSize(1234567890).build()
    ParsedRecord record = ParsedRecord.newBuilder().setFileInformation(fileInfo).setDiscovery(Discovery.newBuilder().build()).build()
    def search = TransformationUtils.reformatGranuleForSearch(12341234L, record)

    then:
    search.getFilesize() == 1234567890
  }

  ////////////////////////////
  // Responsible Parties    //
  ////////////////////////////
  def "prepares responsible party names"() {
    when:
    def record = TestUtils.inputCollectionRecord
    def search = new SearchCollection()
    TransformationUtils.prepareResponsibleParties(search, record)

    then:
    search.getIndividualNames() == [
        'John Smith',
        'Jane Doe',
        'Jarianna Whackositz',
        'Dr. Quinn McClojure Man',
        'Zebulon Pike',
        'Little Rhinoceros',
        'Skeletor McSkittles',
    ] as Set
    search.getOrganizationNames() == [
        'University of Awesome',
        'Secret Underground Society',
        'Soap Boxes Inc.',
        'Pikes Peak Inc.',
        'Alien Infested Spider Monkey Rescue',
        'The Underworld',
        'Super Important Organization',
    ] as Set
  }

  ////////////////////////////
  // Dates                  //
  ////////////////////////////

  def "when #label, expected temporal bounding generated"() {
    when:
    def discovery = Discovery.newBuilder().setTemporalBounding(input).build()
    def search = new SearchCollection()
    TransformationUtils.prepareDates(search, Temporal.analyzeBounding(discovery))

    then:
    search.beginDate == beginDate
    search.beginYear == beginYear
    search.beginDayOfYear == beginDayOfYear
    search.beginDayOfMonth == beginDayOfMonth
    search.beginMonth == beginMonth
    search.endDate == endDate
    search.endYear == endYear
    search.endDayOfYear == endDayOfYear
    search.endDayOfMonth == endDayOfMonth
    search.endMonth == endMonth

    when: 'repeat for granule'
    search = new SearchGranule()
    TransformationUtils.prepareDates(search, Temporal.analyzeBounding(discovery))

    then:
    search.beginDate == beginDate
    search.beginYear == beginYear
    search.beginDayOfYear == beginDayOfYear
    search.beginDayOfMonth == beginDayOfMonth
    search.beginMonth == beginMonth
    search.endDate == endDate
    search.endYear == endYear
    search.endDayOfYear == endDayOfYear
    search.endDayOfMonth == endDayOfMonth
    search.endMonth == endMonth

    where:
    label | input | beginDate | beginYear | beginDayOfYear | beginDayOfMonth | beginMonth | endDate | endYear | endDayOfYear | endDayOfMonth | endMonth

    "undefined range" | TemporalBounding.newBuilder().build() | null | null | null | null | null | null | null | null | null | null
    "non-paleo bounded range with day and year precision" | TemporalBounding.newBuilder().setBeginDate('1900-01-01').setEndDate('2009').build() | '1900-01-01T00:00:00Z' | 1900 | 1 | 1 | 1 | '2009-12-31T23:59:59.999Z' | 2009 | 365 | 31 | 12
    "paleo bounded range" | TemporalBounding.newBuilder().setBeginDate('-2000000000').setEndDate('-1000000000').build() | null | -2000000000 | null | null | null | null | -1000000000 | null | null | null
    "ongoing range with second precision for begin" | TemporalBounding.newBuilder().setBeginDate('1975-06-15T12:30:00Z').build() | "1975-06-15T12:30:00Z" | 1975 | 166 | 15 | 6 | null | null | null | null | null
    // INSTANTS:
    "instant leapyear" | TemporalBounding.newBuilder().setInstant('2004').build() | '2004-01-01T00:00:00Z' | 2004 | 1 | 1 | 1 | '2004-12-31T23:59:59.999Z' | 2004 | 366 | 31 | 12
    "instant with month precision" | TemporalBounding.newBuilder().setInstant('1999-02').build() | '1999-02-01T00:00:00Z' | 1999 | 32 | 1 | 2 | '1999-02-28T23:59:59.999Z' | 1999 | 59 | 28 | 2
    "instant on leapyear with month precision" | TemporalBounding.newBuilder().setInstant('2004-02').build() | '2004-02-01T00:00:00Z' | 2004 | 32 | 1 | 2 | '2004-02-29T23:59:59.999Z' | 2004 | 60 | 29 | 2
    "instant set with begin and end date matching" | TemporalBounding.newBuilder().setBeginDate('1994-07-20T13:22:00Z').setEndDate('1994-07-20T13:22:00Z').build() | '1994-07-20T13:22:00Z' | 1994 | 201 | 20 | 7 | '1994-07-20T13:22:00Z' | 1994 | 201 | 20 | 7
    "non-paleo instant with years precision" | TemporalBounding.newBuilder().setInstant('1999').build() | '1999-01-01T00:00:00Z' | 1999 | 1 | 1 | 1 | '1999-12-31T23:59:59.999Z' | 1999 | 365 | 31 | 12
    "non-paleo instant with days precision" | TemporalBounding.newBuilder().setInstant('1999-12-31').build() | '1999-12-31T00:00:00Z' | 1999 | 365 | 31 | 12 | '1999-12-31T23:59:59.999Z' | 1999 | 365 | 31 | 12
    "paleo instant with years precision" | TemporalBounding.newBuilder().setInstant('-1000000000').build() | null | -1000000000 | null | null | null | null | -1000000000 | null | null | null
    "non-paleo instant with nanos precision" | TemporalBounding.newBuilder().setInstant('2008-04-01T00:00:00Z').build() | '2008-04-01T00:00:00Z' | 2008 | 92 | 1 | 4 | '2008-04-01T00:00:00Z' | 2008 | 92 | 1 | 4
  }

  def "temporal bounding with #testCase dates is prepared correctly"() {
    given:
    def bounding = TemporalBounding.newBuilder().setBeginDate(begin).setEndDate(end).build()
    def analysis = Temporal.analyzeBounding(Discovery.newBuilder().setTemporalBounding(bounding).build())

    when:
    def search = new SearchCollection()
    TransformationUtils.prepareDates(search, analysis)

    then:
    search.beginDate == beginDate
    search.beginYear == beginYear
    search.endYear == endYear
    search.endDate == endDate

    when: 'repeat for granule'
    search = new SearchGranule()
    TransformationUtils.prepareDates(search, analysis)

    then:
    search.beginDate == beginDate
    search.beginYear == beginYear
    search.endYear == endYear
    search.endDate == endDate

    where:
    testCase      | begin                  | end                     | beginDate | beginYear | endDate | endYear
    'typical'     | '2005-05-09T00:00:00Z' | '2010-10-01'            | '2005-05-09T00:00:00Z' | 2005 | '2010-10-01T23:59:59.999Z' | 2010
    'no timezone' | '2005-05-09T00:00:00'  | '2010-10-01T00:00:00'   | '2005-05-09T00:00:00Z' | 2005 | '2010-10-01T00:00:00Z' | 2010
    'paleo'       | '-100000001'           | '-1601050'              | null | -100000001 | '-1601050-12-31T23:59:59.999Z' | -1601050
    'invalid'     | '1984-04-31'           | '1985-505-09T00:00:00Z' | null | null | null | null
  }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  def "Create GCMD keyword lists"() {
    when:
    def search = new SearchCollection()
    TransformationUtils.prepareGcmdKeyword(search, TestUtils.inputAvroRecord.discovery)

    then:
    search.getGcmdScienceServices() == expectedGcmdKeywords.gcmdScienceServices
    search.getGcmdScience() == expectedGcmdKeywords.gcmdScience
    search.getGcmdLocations() == expectedGcmdKeywords.gcmdLocations
    search.getGcmdInstruments() == expectedGcmdKeywords.gcmdInstruments
    search.getGcmdPlatforms() == expectedGcmdKeywords.gcmdPlatforms
    search.getGcmdProjects() == expectedGcmdKeywords.gcmdProjects
    search.getGcmdDataCenters() == expectedGcmdKeywords.gcmdDataCenters
    search.getGcmdHorizontalResolution() == expectedGcmdKeywords.gcmdHorizontalResolution
    search.getGcmdVerticalResolution() == expectedGcmdKeywords.gcmdVerticalResolution
    search.getGcmdTemporalResolution() == expectedGcmdKeywords.gcmdTemporalResolution

    and: "should recreate keywords without accession values"
    search.getKeywords().size() == expectedKeywords.size() // note the accession numbers are not included

    when: 'repeat for granule'
    search = new SearchGranule()
    TransformationUtils.prepareGcmdKeyword(search, TestUtils.inputAvroRecord.discovery)

    then:
    search.getGcmdScienceServices() == expectedGcmdKeywords.gcmdScienceServices
    search.getGcmdScience() == expectedGcmdKeywords.gcmdScience
    search.getGcmdLocations() == expectedGcmdKeywords.gcmdLocations
    search.getGcmdInstruments() == expectedGcmdKeywords.gcmdInstruments
    search.getGcmdPlatforms() == expectedGcmdKeywords.gcmdPlatforms
    search.getGcmdProjects() == expectedGcmdKeywords.gcmdProjects
    search.getGcmdDataCenters() == expectedGcmdKeywords.gcmdDataCenters
    search.getGcmdHorizontalResolution() == expectedGcmdKeywords.gcmdHorizontalResolution
    search.getGcmdVerticalResolution() == expectedGcmdKeywords.gcmdVerticalResolution
    search.getGcmdTemporalResolution() == expectedGcmdKeywords.gcmdTemporalResolution

    and: "should recreate keywords without accession values"
    search.getKeywords().size() == expectedKeywords.size() // note the accession numbers are not included
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
    def search = new SearchCollection()
    def discovery = TestUtils.inputCollectionRecord.discovery
    TransformationUtils.prepareGcmdKeyword(search, discovery)

    then:
    search.getGcmdScience() == expectedKeywordsFromIso.science
    search.getGcmdScienceServices() == expectedKeywordsFromIso.scienceService

    when: 'repeat for granule'
    search = new SearchGranule()
    TransformationUtils.prepareGcmdKeyword(search, discovery)

    then:
    search.getGcmdScience() == expectedKeywordsFromIso.science
    search.getGcmdScienceServices() == expectedKeywordsFromIso.scienceService
  }

}
