package org.cedar.onestop.indexer.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.cedar.onestop.mapping.analysis.AnalysisErrorGranule
import org.cedar.onestop.mapping.analysis.AnalysisErrorCollection
import org.cedar.onestop.mapping.search.SearchCollection
import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.analyze.Temporal
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.IdentificationAnalysis
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import org.cedar.schemas.avro.psi.TimeRangeDescriptor
import org.cedar.schemas.avro.psi.Checksum
import org.cedar.schemas.avro.psi.ChecksumAlgorithm
import org.cedar.schemas.avro.psi.DataFormat
import org.cedar.schemas.avro.psi.DataAccessAnalysis
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.FileInformation
import org.cedar.schemas.avro.psi.Link
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.Reference
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.psi.Service
import org.cedar.schemas.avro.psi.SpatialBoundingAnalysis
import org.cedar.schemas.avro.psi.TemporalBounding
import org.cedar.schemas.avro.psi.ThumbnailAnalysis
import java.time.temporal.ChronoUnit
import spock.lang.Specification
import spock.lang.Unroll

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.cedar.schemas.avro.util.AvroUtils

import org.cedar.onestop.kafka.common.util.DataUtils;

@Unroll
class TransformationUtilsSpec extends Specification {
  //
  // static Set<String> collectionSearchFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_SEARCH_INDEX_ALIAS).keySet()
  // static Set<String> granuleSearchFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_SEARCH_INDEX_ALIAS).keySet()
  // static Set<String> granuleAnalysisErrorFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.GRANULE_ERROR_AND_ANALYSIS_INDEX_ALIAS).keySet()
  // static Set<String> collectionAnalysisErrorFields = TestUtils.esConfig.indexedProperties(TestUtils.esConfig.COLLECTION_ERROR_AND_ANALYSIS_INDEX_ALIAS).keySet()

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

  def "reformatCollectionForAnalysis identification file #fileId doi #doi"() {
    String identifier = 'gov.noaa.nodc:0173643'
    when:
    def discovery = Discovery.newBuilder().setDoi(doi).setFileIdentifier(fileId).setHierarchyLevelName(hierarchy).build()
    def analysis = Analysis.newBuilder().setIdentification(Analyzers.analyzeIdentifiers(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getIdentification().getFileIdentifierExists() == fileIdExists
    indexedRecord.getIdentification().getDoiExists() == doiExists

    where:
    fileId | doi | hierarchy    | fileIdExists | doiExists | hierarchyExists
    'abc' | null | 'collection' | true | false | true
    null | '123' | 'collection' | false | true | true
    'abc' | '123' | null        | true | true | false
  }

  def "reformatCollectionForAnalysis data access, thumbnail etc #label"() {
    when:
    def analysis = Analysis.newBuilder().setDataAccess(
      DataAccessAnalysis.newBuilder().setDataAccessExists(dataAccessExists).build()
    ).setThumbnail(
      ThumbnailAnalysis.newBuilder().setThumbnailExists(thumbnailExists).build()
    ).build() //TODO not sure exactly where on discovery record this is populated rom, so populate directly
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getDataAccess().getDataAccessExists() == dataAccessExists
    indexedRecord.getThumbnail().getThumbnailExists() == thumbnailExists

    where:
    label | dataAccessExists | thumbnailExists
    'has data access & thumbnail' | true | true
    'no data access or thumbnail' | false | false
  }

  def "reformatCollectionForAnalysis description #desc"() {
    when:

    def discovery = Discovery.newBuilder().setDescription(desc).build()
    def analysis = Analysis.newBuilder().setDescription(Analyzers.analyzeDescription(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getDescription().getDescriptionExists() == exists
    indexedRecord.getDescription().getDescriptionCharacters() == length

    where:
    desc | exists | length
    'test description' | true | 16
    null | false | 0
  }

  def "reformatCollectionForAnalysis title #title"() {
    when:

    def discovery = Discovery.newBuilder().setTitle(title).setAlternateTitle(altTitle).build()
    def analysis = Analysis.newBuilder().setTitles(Analyzers.analyzeTitles(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getTitles().getTitleExists() == exists
    indexedRecord.getTitles().getTitleCharacters() == length
    indexedRecord.getTitles().getAlternateTitleExists() == altExists
    indexedRecord.getTitles().getAlternateTitleCharacters() == altLength

    where:
    title | altTitle | exists | length | altExists | altLength
    null | null | false | 0 | false | 0
    "title only" | null | true | 10 | false | 0
    "title and alt" | "alt" | true | 13 | true | 3
  }

  def "reformatCollectionForAnalysis temporal #label"() {
    when:

    def analysis = Analysis.newBuilder().setTemporalBounding(
      TemporalBoundingAnalysis.newBuilder()
      .setBeginDescriptor(beginDesc)
      .setBeginIndexable(beginIndexable)
      .setBeginPrecision(beginPrecision)
      .setBeginUtcDateTimeString(beginString)
      .setBeginZoneSpecified(beginZone)
      .setEndDescriptor(endDesc)
      .setEndIndexable(endIndexable)
      .setEndPrecision(endPrecision)
      .setEndUtcDateTimeString(endString)
      .setEndZoneSpecified(endZone)
      .setInstantDescriptor(instantDesc)
      .setInstantIndexable(instantIndexable)
      .setInstantPrecision(instantPrecision)
      .setInstantUtcDateTimeString(instantString)
      .setInstantZoneSpecified(instantZone)
      .setRangeDescriptor(rangeDesc)
      .build()).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getTemporalBounding().getBeginDescriptor() == expectedBeginDesc
    indexedRecord.getTemporalBounding().getBeginIndexable() == beginIndexable
    indexedRecord.getTemporalBounding().getBeginPrecision() == beginPrecision
    indexedRecord.getTemporalBounding().getBeginUtcDateTimeString() == beginString
    indexedRecord.getTemporalBounding().getBeginZoneSpecified() == beginZone
    indexedRecord.getTemporalBounding().getEndDescriptor() == expectedEndDesc
    indexedRecord.getTemporalBounding().getEndIndexable() == endIndexable
    indexedRecord.getTemporalBounding().getEndPrecision() == endPrecision
    indexedRecord.getTemporalBounding().getEndUtcDateTimeString() == endString
    indexedRecord.getTemporalBounding().getEndZoneSpecified() == endZone
    indexedRecord.getTemporalBounding().getInstantDescriptor() == expectedInstantDesc
    indexedRecord.getTemporalBounding().getInstantIndexable() == instantIndexable
    indexedRecord.getTemporalBounding().getInstantPrecision() == instantPrecision
    indexedRecord.getTemporalBounding().getInstantUtcDateTimeString() == instantString
    indexedRecord.getTemporalBounding().getInstantZoneSpecified() == instantZone
    indexedRecord.getTemporalBounding().getRangeDescriptor() == expectedRangeDesc

    where:
    label | beginDesc | expectedBeginDesc | beginIndexable | beginPrecision | beginString | beginZone | endDesc | expectedEndDesc | endIndexable | endPrecision | endString | endZone | instantDesc | expectedInstantDesc | instantIndexable | instantPrecision | instantString | instantZone | rangeDesc | expectedRangeDesc
    'bounded' | ValidDescriptor.VALID | 'VALID' | true | 'Days' | '2001-01-01T00:00.00Z' | 'Z' | ValidDescriptor.VALID | 'VALID' | true | 'Nanos' | '2001-05-05T12:12:12.000Z' | 'Z' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | TimeRangeDescriptor.BOUNDED | 'BOUNDED'
    'instant' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.INVALID | 'INVALID' | true | 'Month' | '2001-02' | 'Z' | TimeRangeDescriptor.INSTANT | 'INSTANT' // Note the timezone values are a total random guess. Everything else is approximately accurate to the best of my knowledge

  }

  def "reformatCollectionForAnalysis spatial #label"() {
    when:

    def analysis = Analysis.newBuilder().setSpatialBounding(
      SpatialBoundingAnalysis.newBuilder()
      .setSpatialBoundingExists(boundsExist)
      .setIsValid(isValid)
      .setValidationError(error)
      .build()).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getSpatialBounding().getSpatialBoundingExists() == boundsExist
    indexedRecord.getSpatialBounding().getIsValid() == isValid
    indexedRecord.getSpatialBounding().getValidationError() == error

    where:
    label | boundsExist | isValid | error
    'happy space' | true | true | null
    'sad space' | true | false | 'unable to parse'
    'empty space' | false | true | null
  }

  def "reformatCollectionForAnalysis errors #numErrors"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(Analysis.newBuilder().build()).setErrors(errors).build()

    def indexedRecord = TransformationUtils.reformatCollectionForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getErrors().size() == numErrors
    indexedRecord.getErrors()[0].getTitle() == 'one'
    indexedRecord.getErrors()[0].getDetail() == 'first error'
    where:
    errors | numErrors
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build(), ErrorEvent.newBuilder().setTitle('two').setDetail('second error').build()] | 2
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build()] | 1
  }


  def "reformatGranuleForAnalysis identification file #fileId doi #doi"() {
    when:
    def discovery = Discovery.newBuilder().setDoi(doi).setFileIdentifier(fileId).setHierarchyLevelName(hierarchy).setParentIdentifier(parent).build()
    def analysis = Analysis.newBuilder().setIdentification(Analyzers.analyzeIdentifiers(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    // TODO FIXME where on the parsed record do I set this? indexedRecord.getInternalParentIdentifier() == uuid
    indexedRecord.getIdentification().getFileIdentifierExists() == fileIdExists
    indexedRecord.getIdentification().getDoiExists() == doiExists
    indexedRecord.getIdentification().getHierarchyLevelNameExists() == hierarchyExists
    indexedRecord.getIdentification().getParentIdentifierExists() == parentIdExists

    where:
    fileId | doi | parent | hierarchy  | fileIdExists | doiExists | hierarchyExists | parentIdExists
    'abc' | null | 'doi of parent'     | 'granule' | true | false | true | true
    null | '123' | 'file id of parent' | 'granule' | false | true | true | true
    'abc' | '123' | null               | null | true | true | false | false
  }

  def "reformatGranuleForAnalysis data access, thumbnail etc #label"() {
    when:
    def analysis = Analysis.newBuilder().setDataAccess(
      DataAccessAnalysis.newBuilder().setDataAccessExists(dataAccessExists).build()
    ).setThumbnail(
      ThumbnailAnalysis.newBuilder().setThumbnailExists(thumbnailExists).build()
    ).build() //TODO not sure exactly where on discovery record this is populated rom, so populate directly
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getDataAccess().getDataAccessExists() == dataAccessExists
    indexedRecord.getThumbnail().getThumbnailExists() == thumbnailExists

    where:
    label | dataAccessExists | thumbnailExists
    'has data access & thumbnail' | true | true
    'no data access or thumbnail' | false | false
  }

  def "reformatGranuleForAnalysis description #desc"() {
    when:

    def discovery = Discovery.newBuilder().setDescription(desc).build()
    def analysis = Analysis.newBuilder().setDescription(Analyzers.analyzeDescription(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getDescription().getDescriptionExists() == exists
    indexedRecord.getDescription().getDescriptionCharacters() == length

    where:
    desc | exists | length
    'test description' | true | 16
    null | false | 0
  }

  def "reformatGranuleForAnalysis title #title"() {
    when:

    def discovery = Discovery.newBuilder().setTitle(title).setAlternateTitle(altTitle).build()
    def analysis = Analysis.newBuilder().setTitles(Analyzers.analyzeTitles(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getTitles().getTitleExists() == exists
    indexedRecord.getTitles().getTitleCharacters() == length
    indexedRecord.getTitles().getAlternateTitleExists() == altExists
    indexedRecord.getTitles().getAlternateTitleCharacters() == altLength

    where:
    title | altTitle | exists | length | altExists | altLength
    null | null | false | 0 | false | 0
    "title only" | null | true | 10 | false | 0
    "title and alt" | "alt" | true | 13 | true | 3
  }

  def "reformatGranuleForAnalysis temporal #label"() {
    when:

    def analysis = Analysis.newBuilder().setTemporalBounding(
      TemporalBoundingAnalysis.newBuilder()
      .setBeginDescriptor(beginDesc)
      .setBeginIndexable(beginIndexable)
      .setBeginPrecision(beginPrecision)
      .setBeginUtcDateTimeString(beginString)
      .setBeginZoneSpecified(beginZone)
      .setEndDescriptor(endDesc)
      .setEndIndexable(endIndexable)
      .setEndPrecision(endPrecision)
      .setEndUtcDateTimeString(endString)
      .setEndZoneSpecified(endZone)
      .setInstantDescriptor(instantDesc)
      .setInstantIndexable(instantIndexable)
      .setInstantPrecision(instantPrecision)
      .setInstantUtcDateTimeString(instantString)
      .setInstantZoneSpecified(instantZone)
      .setRangeDescriptor(rangeDesc)
      .build()).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getTemporalBounding().getBeginDescriptor() == expectedBeginDesc
    indexedRecord.getTemporalBounding().getBeginIndexable() == beginIndexable
    indexedRecord.getTemporalBounding().getBeginPrecision() == beginPrecision
    indexedRecord.getTemporalBounding().getBeginUtcDateTimeString() == beginString
    indexedRecord.getTemporalBounding().getBeginZoneSpecified() == beginZone
    indexedRecord.getTemporalBounding().getEndDescriptor() == expectedEndDesc
    indexedRecord.getTemporalBounding().getEndIndexable() == endIndexable
    indexedRecord.getTemporalBounding().getEndPrecision() == endPrecision
    indexedRecord.getTemporalBounding().getEndUtcDateTimeString() == endString
    indexedRecord.getTemporalBounding().getEndZoneSpecified() == endZone
    indexedRecord.getTemporalBounding().getInstantDescriptor() == expectedInstantDesc
    indexedRecord.getTemporalBounding().getInstantIndexable() == instantIndexable
    indexedRecord.getTemporalBounding().getInstantPrecision() == instantPrecision
    indexedRecord.getTemporalBounding().getInstantUtcDateTimeString() == instantString
    indexedRecord.getTemporalBounding().getInstantZoneSpecified() == instantZone
    indexedRecord.getTemporalBounding().getRangeDescriptor() == expectedRangeDesc

    where:
    label | beginDesc | expectedBeginDesc | beginIndexable | beginPrecision | beginString | beginZone | endDesc | expectedEndDesc | endIndexable | endPrecision | endString | endZone | instantDesc | expectedInstantDesc | instantIndexable | instantPrecision | instantString | instantZone | rangeDesc | expectedRangeDesc
    'bounded' | ValidDescriptor.VALID | 'VALID' | true | 'Days' | '2001-01-01T00:00.00Z' | 'Z' | ValidDescriptor.VALID | 'VALID' | true | 'Nanos' | '2001-05-05T12:12:12.000Z' | 'Z' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | TimeRangeDescriptor.BOUNDED | 'BOUNDED'
    'instant' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.INVALID | 'INVALID' | true | 'Month' | '2001-02' | 'Z' | TimeRangeDescriptor.INSTANT | 'INSTANT' // Note the timezone values are a total random guess. Everything else is approximately accurate to the best of my knowledge

  }

  def "reformatGranuleForAnalysis spatial #label"() {
    when:

    def analysis = Analysis.newBuilder().setSpatialBounding(
      SpatialBoundingAnalysis.newBuilder()
      .setSpatialBoundingExists(boundsExist)
      .setIsValid(isValid)
      .setValidationError(error)
      .build()).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getSpatialBounding().getSpatialBoundingExists() == boundsExist
    indexedRecord.getSpatialBounding().getIsValid() == isValid
    indexedRecord.getSpatialBounding().getValidationError() == error

    where:
    label | boundsExist | isValid | error
    'happy space' | true | true | null
    'sad space' | true | false | 'unable to parse'
    'empty space' | false | true | null
  }

  def "reformatGranuleForAnalysis errors #numErrors"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(Analysis.newBuilder().build()).setErrors(errors).build()

    def indexedRecord = TransformationUtils.reformatGranuleForAnalysis(12341234L, record) // TODO 12341234 is an arbitrary "timestamp" for now...

    then:
    indexedRecord.getErrors().size() == numErrors
    indexedRecord.getErrors()[0].getTitle() == 'one'
    indexedRecord.getErrors()[0].getDetail() == 'first error'
    where:
    errors | numErrors
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build(), ErrorEvent.newBuilder().setTitle('two').setDetail('second error').build()] | 2
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build()] | 1
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

  def "search - populated link and service link protocols"() {
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
  }

  def "search - populated service links"() {
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
  }

  def "search - populated links"() {
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

  def "search - populated data formats"() {
    when:
    def discovery = Discovery.newBuilder().setDataFormats([DataFormat.newBuilder().setName("netCDF").setVersion('4').build(), DataFormat.newBuilder().setName("netcdf").build()]).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getDataFormat().size() == 2
    search.getDataFormat() == ["NETCDF", "NETCDF > 4"] as Set
  }

  def "search - various identifiers parent #parentId file #fileId doi #doi"() {
    when:
    def discovery = Discovery.newBuilder().setParentIdentifier(parentId).setFileIdentifier(fileId).setDoi(doi).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

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

  def "search - title and description and thumbnail"() {
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
  }

  def "search - larger works & cross references"() {
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

  def "search - spatial #label"() {
    when:
    def discovery = Discovery.newBuilder().setIsGlobal(isGlobal).build()
    ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(discovery).build()
    def search = TransformationUtils.reformatCollectionForSearch(12341234L, record)

    then:
    search.getIsGlobal() == isGlobal

    where:
    label | isGlobal
    "global" | true
    "small" | false
  }

  def "search - misc fields"() {
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

  def "does not prepare responsible party names for granules"() {
    when:
    def record = TestUtils.inputGranuleRecord
    def search = new SearchCollection()
    TransformationUtils.prepareResponsibleParties(search, record)

    then:
    search.getIndividualNames() == [] as Set
    search.getOrganizationNames() == [] as Set
  }

  def "party names are not included in granule search info"() {
    when:
    def record = TestUtils.inputGranuleRecord // <-- granule!
    def result = TransformationUtils.reformatCollectionForSearch(12341234L, record) // <-- top level reformat method!

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
    def search = new SearchCollection()
    TransformationUtils.prepareDates(search, Temporal.analyzeBounding(discovery))

    println("debug " + label + ": " + Temporal.analyzeBounding(discovery))

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
    def search = new SearchCollection()

    when:
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
  }



}
