package org.cedar.psi.manager.util

import org.cedar.schemas.avro.geojson.Point
import org.cedar.schemas.avro.psi.*
import org.cedar.schemas.avro.util.AvroUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

import static org.cedar.schemas.avro.psi.NullDescriptor.INVALID
import static org.cedar.schemas.avro.psi.NullDescriptor.UNDEFINED
import static org.cedar.schemas.avro.psi.TimeRangeDescriptor.*
import static spock.util.matcher.HamcrestMatchers.closeTo

@Unroll
class AnalyzersSpec extends Specification {

  final String analysisAvro = ClassLoader.systemClassLoader.getResourceAsStream('avro/psi/analysis.avsc').text

  def 'unknown word in dictionary'() {
    when:
    def word = Analyzers.findWordSyllables('foo')

    then:
    word instanceof Map
    word.match == null
    word.syllables == null
  }

  def 'short known word in dictionary'() {
    when:
    def word = Analyzers.findWordSyllables('cat')

    then:
    word instanceof Map
    word.match == 'cat'
    word.syllables == 1
  }

  def 'syllables care not for your captialization'() {
    when:
    def word = Analyzers.findWordSyllables('CaT')

    then:
    word instanceof Map
    word.match == 'cat'
    word.syllables == 1
  }

  def 'long word in dictionary'() {
    when:
    def word = Analyzers.findWordSyllables('simultaneously')

    then:
    word instanceof Map
    word.match == 'si;mul;ta;ne;ous;ly'
    word.syllables == 6
  }

  def 'words in a few sentences'() {
    when:
    def words = Analyzers.words('A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.')

    then:
    words.size() == 13
    words[0] == 'a'
    words[2] == 'separate'
    words[12] == 'anyway'

    when:
    def syllables = words.collect({it ->
      println(it)
      return Analyzers.findWordSyllables(it).s2
    })//.sum()

    then:
    syllables == [1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 3, 4, 3] // 24 // it says 4 for hopefully, because estimating syllables with regex is nontrivial!
  }

  def 'sentences are not too bad...'() {
    when:
    def sentences = Analyzers.splitIntoSentences('A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.')

    then:
    sentences.size() == 6
  }

  def 'components of readability algorithm'() {
    def text = 'A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.'

    when:
    def sentences = Analyzers.totalSentences(text)
    def words = Analyzers.totalWords(text)
    def syllables = Analyzers.totalSyllables(text)
    def fraction = words/sentences
    def fraction2 = syllables/words

    then:
    sentences == 6
    words == 13
    syllables == 24
    closeTo(2.16, 0.01).matches(fraction)
    closeTo(1.85, 0.01).matches(fraction2)
  }

  def 'F-K readability example 3' () {
    def text = 'The goal is to have simple, clear text that anyone can read. This is not easy to do, or even truely to measure.'

    when:
    boolean FKthreshold = Analyzers.passesReadabilityTest(text)
    Number fkScore = Analyzers.readabilityFleschKincaid(text)

    then:
    closeTo(92.17, 0.01).matches(fkScore)
    FKthreshold == true
  }

  def 'F-K readability example 1' () {
    def text = 'A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.'

    when:
    boolean FKthreshold = Analyzers.passesReadabilityTest(text)
    Number fkScore = Analyzers.readabilityFleschKincaid(text)

    then:
    closeTo(48.45, 0.01).matches(fkScore)
    FKthreshold == false
  }

  def 'F-K readability example 2' () {
    def text = 'A Group for High Resolution Sea Surface Temperature (GHRSST) Level 2P dataset based on multi-channel sea surface temperature (SST) retrievals generated in real-time from the Infrared Atmospheric Sounding Interferometer (IASI) on the European Meteorological Operational-B (MetOp-B)satellite (launched 17 Sep 2012). The European Organization for the Exploitation of Meteorological Satellites (EUMETSAT),Ocean and Sea Ice Satellite Application Facility (OSI SAF) is producing SST products in near realtime from METOP/IASI. The Infrared Atmospheric Sounding Interferometer (IASI) measures inthe infrared part of the electromagnetic spectrum at a horizontal resolution of 12 km at nadir up to40km over a swath width of about 2,200 km. With 14 orbits in a sun-synchronous mid-morningorbit (9:30 Local Solar Time equator crossing, descending node) global observations can beprovided twice a day. The SST retrieval is performed and provided by the IASI L2 processor atEUMETSAT headquarters. The product format is compliant with the GHRSST Data Specification(GDS) version 2.'

    when:
    boolean FKthreshold = Analyzers.passesReadabilityTest(text)
    Number fkScore = Analyzers.readabilityFleschKincaid(text)

    then:
    closeTo(20.6, 0.01).matches(fkScore)
    FKthreshold == false

  }

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
            beginExists             : true,
            // For why below value is not seconds, see:
            // https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalQueries.html#precision--
            beginPrecision          : ChronoUnit.NANOS.toString(),
            beginIndexable          : true,
            beginZoneSpecified      : 'Z',
            beginUtcDateTimeString  : '2005-05-09T00:00:00Z',
            endExists               : true,
            endPrecision            : ChronoUnit.DAYS.toString(),
            endIndexable            : true,
            endZoneSpecified        : UNDEFINED,
            endUtcDateTimeString    : '2010-10-01T23:59:59Z',
            instantExists           : false,
            instantPrecision        : UNDEFINED,
            instantIndexable        : true,
            instantZoneSpecified    : UNDEFINED,
            instantUtcDateTimeString: UNDEFINED,
            rangeDescriptor         : BOUNDED,
            rangeBeginLTEEnd        : true
        ],
        spatialBounding : [
            spatialBoundingExists: true
        ],
        titles          : [
            titleExists             : true,
            titleCharacters         : 63,
            alternateTitleExists    : true,
            alternateTitleCharacters: 51
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
    result.exists == exists
    result.precision == precision
    result.indexable == valid
    result.zoneSpecified == zone
    result.utcDateTimeString == string

    where:
    input                  | start || exists | precision   | valid | zone        | string
    '2042-04-02T00:42:42Z' | false || true   | 'Nanos'     | true  | 'Z'         | '2042-04-02T00:42:42Z'
    '2042-04-02T00:42:42'  | false || true   | 'Nanos'     | true  | UNDEFINED   | '2042-04-02T00:42:42Z'
    '2042-04-02'           | false || true   | 'Days'      | true  | UNDEFINED   | '2042-04-02T23:59:59Z'
    '2042-04-02'           | true  || true   | 'Days'      | true  | UNDEFINED   | '2042-04-02T00:00:00Z'
    '2042'                 | true  || true   | 'Years'     | true  | UNDEFINED   | '2042-01-01T00:00:00Z'
    '-5000'                | true  || true   | 'Years'     | true  | UNDEFINED   | '-5000-01-01T00:00:00Z'
    '-100000001'           | true  || true   | 'Years'     | false | UNDEFINED   | '-100000001-01-01T00:00:00Z'
    'ABC'                  | true  || true   | INVALID     | false | INVALID     | INVALID
    ''                     | true  || false  | UNDEFINED   | true  | UNDEFINED   | UNDEFINED
    null                   | true  || false  | UNDEFINED   | true  | UNDEFINED   | UNDEFINED
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
    descriptor| situation                                                   | begin                  | end                    | instant
    ONGOING   | 'start date exists but not end date'                        | '2010-01-01'           | ''                     | null
    BOUNDED   | 'start and end date exist and are valid'                    | '2000-01-01T00:00:00Z' | '2001-01-01T00:00:00Z' | null
    UNDEFINED | 'neither start nor end date exist'                          | ''                     | ''                     | null
    INSTANT   | 'neither start nor end date exist but valid instant does'   | ''                     | ''                     | '2001-01-01'
    INVALID   | 'end date exists but not start date'                        | ''                     | '2010'                 | null
    INVALID   | 'start and end date exist but start after end'              | '2100-01-01T00:00:00Z' | '2002-01-01'           | null
    INVALID   | 'neither start nor end date exist but invalid instant does' | ''                     | ''                     | '2001-01-32'
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
    result.rangeBeginLTEEnd == value

    where:
    value       | situation                                                       | begin                  | end
    true        | 'start is valid format and before valid format end'             | '2010-01-01'           | '2011-01-01'
    false       | 'start is valid format and after valid format end'              | '2011-01-01T00:00:00Z' | '2001-01-01T00:00:00Z'
    true        | 'start is invalid format but paleo and before valid format end' | '-1000000000'          | '2015'
    true        | 'start and end both invalid but paleo and start before end'     | '-2000000000'          | '-1000000000'
    false       | 'start and end both invalid but paleo and start after end'      | '-1000000000'          | '-2000000000'
    true        | 'start and end both same instant'                               | '2000-01-01T00:00:00Z' | '2000-01-01T00:00:00Z'
    true        | 'start exists but not end'                                      | '2000-01-01T00:00:00Z' | ''
    UNDEFINED   | 'start does not exist but end does'                             | ''                     | '2000-01-01T00:00:00Z'
    UNDEFINED   | 'neither start nor end exist'                                   | ''                     | ''
    UNDEFINED   | 'start is invalid format but paleo and end is fully invalid'    | '-1000000000'          | '1999-13-12'
    UNDEFINED   | 'start is fully invalid and end is invalid format but paleo'    | '15mya'                | '-1000000000'
    UNDEFINED   | 'start is valid and end is fully invalid'                       | '2000-01-01T00:00:00Z' | '2000-12-31T25:00:00Z'
    UNDEFINED   | 'start and end both fully invalid'                              | '2000-01-01T00:61:00Z' | '2000-11-31T00:00:00Z'
    UNDEFINED   | 'start is fully invalid but end is valid'                       | '2000-01-01T00:00:61Z' | '2000-01-02T00:00:00Z'
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
