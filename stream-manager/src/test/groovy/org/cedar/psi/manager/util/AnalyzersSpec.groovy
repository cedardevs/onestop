package org.cedar.psi.manager.util

import org.cedar.schemas.avro.geojson.Point
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

import static org.cedar.schemas.avro.psi.TimeRangeDescriptor.*

@Unroll
class AnalyzersSpec extends Specification {

  final String analysisAvro = ClassLoader.systemClassLoader.getResourceAsStream('avro/psi/analysis.avsc').text

  def 'adds an analysis into a parsed record'() {
    def record = ParsedRecord.newBuilder().setType(RecordType.collection).setDiscovery(Discovery.newBuilder().build()).build()

    when:
    def result = Analyzers.addAnalysis(record)

    then:
    result instanceof ParsedRecord
    result.analysis instanceof Analysis
    result.discovery == record.discovery
  }

  def 'analyzing null discovery returns null'() {
    expect:
    Analyzers.analyze(null) == null
  }

  def 'analyzing a default discovery object returns all expected analysis'() {
    def discovery = Discovery.newBuilder().build()

    when:
    def analysis = Analyzers.analyze(discovery)

    then:
    analysis instanceof Analysis
    analysis.identification instanceof IdentificationAnalysis
    analysis.temporalBounding instanceof TemporalBoundingAnalysis
    analysis.spatialBounding instanceof SpatialBoundingAnalysis
    analysis.titles instanceof TitleAnalysis
    analysis.description instanceof DescriptionAnalysis
    analysis.thumbnail instanceof ThumbnailAnalysis
    analysis.dataAccess instanceof DataAccessAnalysis
  }

  def "All valid fields return expected response from service"() {
    given:
    def inputXml = ClassLoader.systemClassLoader.getResourceAsStream('test-iso-metadata.xml').text
    def discovery = ISOParser.parseXMLMetadataToDiscovery(inputXml)

    def expectedAnalysisMap = [
        identification  : [
            fileIdentifierExists    : true,
            fileIdentifierString    : 'gov.super.important:FILE-ID',
            doiExists               : true,
            doiString               : 'doi:10.5072/FK2TEST',
            parentIdentifierExists  : true,
            parentIdentifierString  : 'gov.super.important:PARENT-ID',
            hierarchyLevelNameExists: true,
            matchesIdentifiers      : true
        ],
        temporalBounding: [
            beginDescriptor         : ValidDescriptor.VALID,
            // For why below value is not seconds, see:
            // https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalQueries.html#precision--
            beginPrecision          : ChronoUnit.NANOS.toString(),
            beginIndexable          : true,
            beginZoneSpecified      : 'Z',
            beginUtcDateTimeString  : '2005-05-09T00:00:00Z',
            endDescriptor           : ValidDescriptor.VALID,
            endPrecision            : ChronoUnit.DAYS.toString(),
            endIndexable            : true,
            endZoneSpecified        : null,
            endUtcDateTimeString    : '2010-10-01T23:59:59Z',
            instantDescriptor       : ValidDescriptor.UNDEFINED,
            instantPrecision        : null,
            instantIndexable        : true,
            instantZoneSpecified    : null,
            instantUtcDateTimeString: null,
            rangeDescriptor         : BOUNDED,
        ],
        spatialBounding : [
            spatialBoundingExists: true
        ],
        titles          : [
            titleExists             : true,
            titleCharacters         : 63,
            alternateTitleExists    : true,
            alternateTitleCharacters: 51,
            titleFleschReadingEaseScore:-41.98428571066,
            alternateTitleFleschReadingEaseScore:42.61571428934,
            titleFleschKincaidReadingGradeLevel:20,
            alternateTitleFleschKincaidReadingGradeLevel:9,
        ],
        description     : [
            descriptionExists    : true,
            descriptionCharacters: 65
        ],
        thumbnail       : [
            thumbnailExists: true,
        ],
        dataAccess      : [
            dataAccessExists: true
        ]
    ]

    when:
    def analysisObj = Analyzers.analyze(discovery)
    def analysisMap = AvroUtils.avroToMap(analysisObj)

    then:
    AvroUtils.avroToMap(analysisMap.identification) == expectedAnalysisMap.identification
    AvroUtils.avroToMap(analysisMap.titles) == expectedAnalysisMap.titles
    AvroUtils.avroToMap(analysisMap.description) == expectedAnalysisMap.description
    AvroUtils.avroToMap(analysisMap.dataAccess) == expectedAnalysisMap.dataAccess
    AvroUtils.avroToMap(analysisMap.thumbnail) == expectedAnalysisMap.thumbnail
    AvroUtils.avroToMap(analysisMap.temporalBounding) == expectedAnalysisMap.temporalBounding
    AvroUtils.avroToMap(analysisMap.spatialBounding) == expectedAnalysisMap.spatialBounding
  }

  def 'extracts date info from date strings'() {
    when:
    def result = Analyzers.dateInfo(input, start)

    then:
    result.descriptor == descriptor
    result.precision == precision
    result.indexable == indexable
    result.zoneSpecified == zone
    result.utcDateTimeString == string

    where:
    input                  | start || descriptor                | precision | indexable | zone | string
    '2042-04-02T00:42:42Z' | false || ValidDescriptor.VALID     | 'Nanos'   | true      | 'Z'  | '2042-04-02T00:42:42Z'
    '2042-04-02T00:42:42'  | false || ValidDescriptor.VALID     | 'Nanos'   | true      | null | '2042-04-02T00:42:42Z'
    '2042-04-02'           | false || ValidDescriptor.VALID     | 'Days'    | true      | null | '2042-04-02T23:59:59Z'
    '2042-04-02'           | true  || ValidDescriptor.VALID     | 'Days'    | true      | null | '2042-04-02T00:00:00Z'
    '2042'                 | true  || ValidDescriptor.VALID     | 'Years'   | true      | null | '2042-01-01T00:00:00Z'
    '-5000'                | true  || ValidDescriptor.VALID     | 'Years'   | true      | null | '-5000-01-01T00:00:00Z'
    '-100000001'           | true  || ValidDescriptor.VALID     | 'Years'   | false     | null | '-100000001-01-01T00:00:00Z'
    'ABC'                  | true  || ValidDescriptor.INVALID   | null      | false     | null | null
    ''                     | true  || ValidDescriptor.UNDEFINED | null      | true      | null | null
    null                   | true  || ValidDescriptor.UNDEFINED | null      | true      | null | null
  }

  def "#descriptor date range correctly identified when #situation"() {
    given:
    def bounding = TemporalBounding.newBuilder()
        .setBeginDate(begin)
        .setEndDate(end)
        .setInstant(instant)
        .build()
    def discovery = Discovery.newBuilder().setTemporalBounding(bounding).build()

    when:
    def result = Analyzers.analyzeTemporalBounding(discovery)

    then:
    result.rangeDescriptor == descriptor

    where:
    descriptor | situation                                                   | begin                  | end                    | instant
    ONGOING    | 'start date exists but not end date'                        | '2010-01-01'           | ''                     | null
    BOUNDED    | 'start and end date exist and are valid'                    | '2000-01-01T00:00:00Z' | '2001-01-01T00:00:00Z' | null
    UNDEFINED  | 'neither start nor end date exist'                          | ''                     | ''                     | null
    INSTANT    | 'neither start nor end date exist but valid instant does'   | ''                     | ''                     | '2001-01-01'
    INVALID    | 'end date exists but not start date'                        | ''                     | '2010'                 | null
    BACKWARDS  | 'start and end date exist but start after end'              | '2100-01-01T00:00:00Z' | '2002-01-01'           | null
    INVALID    | 'neither start nor end date exist but invalid instant does' | ''                     | ''                     | '2001-01-32'
    INVALID    | 'has valid start, end, and instant'                         | '2010-01-01'           | '2001-01-01T00:00:00Z' | '2001-01-32'
  }

  def "Begin date LTE end date check is #value when #situation"() {
    given:
    def bounding = TemporalBounding.newBuilder()
        .setBeginDate(begin)
        .setEndDate(end)
        .build()
    def discovery = Discovery.newBuilder().setTemporalBounding(bounding).build()

    when:
    def result = Analyzers.analyzeTemporalBounding(discovery)

    then:
    result.rangeDescriptor == value

    where:
    value     | situation                                                       | begin                  | end
    BOUNDED   | 'start is valid format and before valid format end'             | '2010-01-01'           | '2011-01-01'
    BACKWARDS | 'start is valid format and after valid format end'              | '2011-01-01T00:00:00Z' | '2001-01-01T00:00:00Z'
    BOUNDED   | 'start is invalid format but paleo and before valid format end' | '-1000000000'          | '2015'
    BOUNDED   | 'start and end both invalid but paleo and start before end'     | '-2000000000'          | '-1000000000'
    BACKWARDS | 'start and end both invalid but paleo and start after end'      | '-1000000000'          | '-2000000000'
    BOUNDED   | 'start and end both same instant'                               | '2000-01-01T00:00:00Z' | '2000-01-01T00:00:00Z'
    ONGOING   | 'start exists but not end'                                      | '2000-01-01T00:00:00Z' | ''
    INVALID   | 'start does not exist but end does'                             | ''                     | '2000-01-01T00:00:00Z'
    UNDEFINED | 'neither start nor end exist'                                   | ''                     | ''
    INVALID   | 'start is invalid format but paleo and end is fully invalid'    | '-1000000000'          | '1999-13-12'
    INVALID   | 'start is fully invalid and end is invalid format but paleo'    | '15mya'                | '-1000000000'
    INVALID   | 'start is valid and end is fully invalid'                       | '2000-01-01T00:00:00Z' | '2000-12-31T25:00:00Z'
    INVALID   | 'start and end both fully invalid'                              | '2000-01-01T00:61:00Z' | '2000-11-31T00:00:00Z'
    INVALID   | 'start is fully invalid but end is valid'                       | '2000-01-01T00:00:61Z' | '2000-01-02T00:00:00Z'
  }

  def "analyzes when links are #testCase"() {
    given:
    def record = Discovery.newBuilder().setLinks(testLinks).build()

    when:
    def dataAccessAnalysis = Analyzers.analyzeDataAccess(record)

    then:
    dataAccessAnalysis instanceof DataAccessAnalysis
    dataAccessAnalysis.dataAccessExists == expected

    where:
    testCase  | testLinks                   | expected
    'missing' | []                          | false
    'present' | [Link.newBuilder().build()] | true
  }

  def "analyzes required identifiers"() {
    given:
    def metadata = Discovery.newBuilder().setFileIdentifier('xyz').build()

    when:
    def result = Analyzers.analyzeIdentifiers(metadata)

    then:
    result instanceof IdentificationAnalysis
    result.fileIdentifierExists
    result.fileIdentifierString == 'xyz'
    !result.doiExists
    result.doiString == null
    !result.parentIdentifierExists
    result.parentIdentifierString == null
    !result.hierarchyLevelNameExists
    result.matchesIdentifiers
  }

  def "detects mismatch between metadata type and corresponding identifiers"() {
    given:
    def builder = Discovery.newBuilder()
    builder.fileIdentifier = 'xyz'
    builder.hierarchyLevelName = 'granule'
    def metadata = builder.build()

    when:
    def result = Analyzers.analyzeIdentifiers(metadata)

    then:
    result instanceof IdentificationAnalysis
    result.fileIdentifierExists
    result.fileIdentifierString == 'xyz'
    !result.doiExists
    result.doiString == null
    !result.parentIdentifierExists
    result.parentIdentifierString == null
    result.hierarchyLevelNameExists
    !result.matchesIdentifiers
  }

  def 'analyzes #testCase strings'() {
    when:
    def result = Analyzers.stringInfo(value)

    then:
    result instanceof Map
    result.exists == exists
    result.characters == length

    where:
    testCase  | value  | exists | length
    'missing' | null   | false  | 0
    'empty'   | ''     | false  | 0
    'present' | 'test' | true   | 4
  }

  def "analyzes when titles are missing"() {
    given:
    def metadata = Discovery.newBuilder().build()

    when:
    def titlesAnalysis = Analyzers.analyzeTitles(metadata)

    then:
    titlesAnalysis instanceof TitleAnalysis
    !titlesAnalysis.titleExists
    titlesAnalysis.titleCharacters == 0
    !titlesAnalysis.alternateTitleExists
    titlesAnalysis.alternateTitleCharacters == 0
  }

  def "analyses when description is missing"() {
    given:
    def metadata = Discovery.newBuilder().build()

    when:
    def descriptionAnalysis = Analyzers.analyzeDescription(metadata)

    then:
    descriptionAnalysis instanceof DescriptionAnalysis
    !descriptionAnalysis.descriptionExists
    descriptionAnalysis.descriptionCharacters == 0
  }

  def "analyzes when thumbnail is #testCase"() {
    given:
    def metadata = Discovery.newBuilder().setThumbnail(value).build()

    when:
    def thumbnailAnalysis = Analyzers.analyzeThumbnail(metadata)

    then:
    thumbnailAnalysis instanceof ThumbnailAnalysis
    thumbnailAnalysis.thumbnailExists == expected

    where:
    testCase  | value                | expected
    'missing' | null                 | false
    'present' | 'thumbnailAnalysis!' | true
  }

  def "analyzes when spatial boundings are #testCase"() {
    given:
    def metadata = Discovery.newBuilder().setSpatialBounding(value).build()

    when:
    def result = Analyzers.analyzeSpatialBounding(metadata)

    then:
    result instanceof SpatialBoundingAnalysis
    result.spatialBoundingExists == expected

    where:
    testCase  | value        | expected
    'missing' | null         | false
    'present' | buildPoint() | true
  }

  static buildPoint() {
    Point.newBuilder()
        .setCoordinates([1 as Double, 2 as Double])
        .build()
  }
}
