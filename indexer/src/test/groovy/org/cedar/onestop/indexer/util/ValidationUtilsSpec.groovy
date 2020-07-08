package org.cedar.onestop.indexer.util

import org.apache.kafka.streams.processor.MockProcessorContext
import org.cedar.onestop.kafka.common.util.TopicIdentifier
import org.cedar.onestop.kafka.common.util.ValueWithTopic
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.IdentificationAnalysis
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.RecordType
import org.cedar.schemas.avro.psi.SpatialBoundingAnalysis
import org.cedar.schemas.avro.psi.TemporalBounding
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.TimeRangeDescriptor
import org.cedar.schemas.avro.psi.TitleAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.psi.ValidDescriptor.*;
import static org.cedar.schemas.avro.psi.TimeRangeDescriptor.*;

import static org.cedar.onestop.indexer.util.ValidationUtils.ValidationError.*;

@Unroll
class ValidationUtilsSpec extends Specification {

  def "valid message passes validation check"() {
    given:
    MockProcessorContext mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTopic(TestUtils.collectionTopic)
    TopicIdentifier<ParsedRecord> ti = new TopicIdentifier<>()
    ti.init(mockProcessorContext)

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(TestUtils.inputAvroRecord)

    then:
    ValidationUtils.addValidationErrors(testInput).errors.isEmpty()
  }

  def "validation passes tombstones through"() {
    given:
    MockProcessorContext mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTopic(TestUtils.collectionTopic)
    TopicIdentifier<ParsedRecord> ti = new TopicIdentifier<>()
    ti.init(mockProcessorContext)

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(null)

    then:
    ValidationUtils.addValidationErrors(testInput) == null
  }

  def "null Discovery fails root validation"() {
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateRootRecord(record)

    then:
    record.discovery == null
    errors.size() == 1
    errors[0].title.equals(ROOT.title)
  }

  def "empty Discovery fails root validation"() {
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).build()
    def record = ParsedRecord.newBuilder().setDiscovery(Discovery.newBuilder().build()).setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateRootRecord(record)

    then:
    record.discovery == Discovery.newBuilder().build()
    errors.size() == 1
    errors[0].title.equals(ROOT.title)
  }

  def "null Analysis fails root validation"() {
    def discovery = Discovery.newBuilder(TestUtils.inputAvroRecord.discovery).build()
    def record = ParsedRecord.newBuilder().setDiscovery(discovery).build()

    when:
    def errors = ValidationUtils.validateRootRecord(record)

    then:
    record.analysis == null
    errors.size() == 1
    errors[0].title.equals(ROOT.title)
  }

  def "empty Analysis fails root validation"() {
    def discovery = Discovery.newBuilder(TestUtils.inputAvroRecord.discovery).build()
    def record = ParsedRecord.newBuilder().setDiscovery(discovery).setAnalysis(Analysis.newBuilder().build()).build()

    when:
    def errors = ValidationUtils.validateRootRecord(record)

    then:
    record.analysis == Analysis.newBuilder().build()
    errors.size() == 1
    errors[0].title.equals(ROOT.title)
  }

  def "validates titles when #testCase"() {
    def titleAnalysis = TitleAnalysis.newBuilder().setTitleExists(titleExists).build()
    def analysis = Analysis.newBuilder().setTitles(titleAnalysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateTitles(record)

    then:
    errors.isEmpty() == isValid

    and:
    if(!isValid) {
      errors.each({ e -> e.title.equals(TITLE.title) })
    }

    where:
    testCase                | isValid | titleExists
    "title is missing"      | false   | false
    "title is not missing"  | true    | true
  }

  def "validates identification when #testCase"() {
    def identificationAnalysis = IdentificationAnalysis.newBuilder()
        .setFileIdentifierExists(hasFileId)
        .setDoiExists(hasDoi)
        .build()
    def analysis = Analysis.newBuilder().setIdentification(identificationAnalysis).build()
    def record = ParsedRecord.newBuilder().setType(type).setAnalysis(analysis)build()

    when:
    def errors = ValidationUtils.validateIdentification(record)

    then:
    errors.size() == errorCount

    and:
    if(errorCount > 0) {
      errors.each({ e -> e.title.equals(IDENTIFICATION.title) })
    }

    where:
    testCase                      | errorCount  | hasFileId | hasDoi  | type
    "has only fileId"             | 0           | true      | false   | RecordType.collection
    "has only doi"                | 0           | false     | true    | RecordType.granule
    "has fileId and doi"          | 0           | true      | true    | RecordType.collection
    "has no ids"                  | 1           | false     | false   | RecordType.granule
    "has unknown type"            | 1           | true      | true    | null
    "has no ids and unknown type" | 2           | false     | false   | null
  }

  def "validates temporal bounds by field when #testCase"() {
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder()
        .setBeginDescriptor(begin)
        .setEndDescriptor(end)
        .setInstantDescriptor(instant)
        .setRangeDescriptor(NOT_APPLICABLE) // Forces traversal through field checks for all test cases
        .build()
    def analysis = Analysis.newBuilder().setTemporalBounding(temporalAnalysis).build()
    // Need to supply content for Discovery here to avoid NPEs
    def temporalBounding = TemporalBounding.newBuilder().setBeginDate("begin").setEndDate("end").setInstant("instant").build()
    def discovery = Discovery.newBuilder().setTemporalBounding(temporalBounding).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).setDiscovery(discovery).build()

    when:
    def errors = ValidationUtils.validateTemporalBounds(record)

    then:
    errors.size() == errorCount

    and:
    if(errorCount > 0) {
      errors.each({ e -> e.title.equals(TEMPORAL_FIELD.title) })
    }

    where:
    testCase                    | errorCount  | begin                     | end                       | instant
    "all dates undefined"       | 0           | ValidDescriptor.UNDEFINED | ValidDescriptor.UNDEFINED | ValidDescriptor.UNDEFINED
    "all dates valid"           | 0           | VALID                     | VALID                     | VALID
    "has invalid begin"         | 1           | INVALID                   | VALID                     | VALID
    "has invalid end"           | 1           | VALID                     | INVALID                   | VALID
    "has invalid instant"       | 1           | VALID                     | VALID                     | INVALID
  }

  def "validates temporal bounds by range when #testCase"() {
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder()
        .setRangeDescriptor(range)
        .build()
    def analysis = Analysis.newBuilder().setTemporalBounding(temporalAnalysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateTemporalBounds(record)

    then:
    errors.size() == errorCount

    and:
    if(errorCount > 0) {
      errors.each({ e -> e.title.equals(TEMPORAL_RANGE.title) })
    }

    where:
    testCase              | errorCount | range
    "has BOUNDED range"   | 0          | BOUNDED
    "has INSTANT range"   | 0          | INSTANT
    "has ONGOING range"   | 0          | ONGOING
    "has UNDEFINED range" | 0          | TimeRangeDescriptor.UNDEFINED
    "has AMBIGUOUS range" | 1          | AMBIGUOUS
    "has BACKWARDS range" | 1          | BACKWARDS
    "has INVALID range"   | 1          | TimeRangeDescriptor.INVALID
    // NOT_APPLICABLE range does not generate TEMPORAL_RANGE error and is tested in validation by fields test
  }

  def "validates spatial bounds when #testCase"() {
    def spatialAnalysis = SpatialBoundingAnalysis.newBuilder()
        .setSpatialBoundingExists(exists)
        .setIsValid(valid)
        .build()
    def analysis = Analysis.newBuilder().setSpatialBounding(spatialAnalysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateSpatialBounds(record)

    then:
    errors.size() == errorCount

    and:
    if(errorCount > 0) {
      errors.each({ e -> e.title.equals(SPATIAL.title) })
    }

    where:
    testCase                       | errorCount  | exists  | valid
    "bounds exist and are valid"   | 0           | true    | true
    "bounds exist and are invalid" | 1           | true    | false
    "bounds do not exist"          | 0           | false   | true
  }

  def "validates topic placement when #testCase"() {
    given:
    def identification = IdentificationAnalysis.newBuilder()
        .setParentIdentifierExists(hasParentId)
        .setHierarchyLevelNameExists(hlm != null)
        .setIsGranule(hasParentId && hlm != null && hlm.equals("granule"))
        .build()
    def analysis = Analysis.newBuilder().setIdentification(identification).build()
    def discovery = Discovery.newBuilder().setHierarchyLevelName(hlm).build()
    def record = ParsedRecord.newBuilder().setType(type).setAnalysis(analysis).setDiscovery(discovery).build()

    when:
    def errors = ValidationUtils.validateTopicPlacement(record, topic)

    then:
    errors.size() == errorCount

    and:
    if(errorCount > 0) {
      errors.each({ e -> e.title.equals(TYPE.title) })
    }

    where:
    testCase                                        | errorCount | hasParentId | hlm          | type                  | topic
    "it's valid"                                    | 0          | false       | null         | RecordType.collection | TestUtils.collectionTopic
    "RecordType only doesn't match"                 | 1          | false       | "collection" | RecordType.granule    | TestUtils.collectionTopic
    "granule on collection topic (metadata check)"  | 1          | true        | "granule"    | RecordType.collection | TestUtils.collectionTopic
    "non-granule on granule topic (no pid)"         | 2          | false       | "granule"    | RecordType.granule    | TestUtils.granuleTopic
    "non-granule on granule topic (no hlm)"         | 2          | true        | null         | RecordType.granule    | TestUtils.granuleTopic
    "non-granule on granule topic (no pid or hlm)"  | 3          | false       | null         | RecordType.granule    | TestUtils.granuleTopic
    "metadata check and RecordType check fail"      | 4          | false       | "collection" | RecordType.collection | TestUtils.granuleTopic
  }
}
