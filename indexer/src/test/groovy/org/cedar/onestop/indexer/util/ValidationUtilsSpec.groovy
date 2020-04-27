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
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.TitleAnalysis
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.psi.ValidDescriptor.INVALID
import static org.cedar.schemas.avro.psi.ValidDescriptor.VALID

@Unroll
class ValidationUtilsSpec extends Specification {

  MockProcessorContext mockProcessorContext
  TopicIdentifier<ParsedRecord> ti

  def setup() {
    mockProcessorContext = new MockProcessorContext()
    mockProcessorContext.setTopic(TestUtils.collectionTopic)
    ti = new TopicIdentifier<>()
    ti.init(mockProcessorContext)
  }

  def "valid message passes validation check"() {
    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(TestUtils.inputAvroRecord)

    then:
    ValidationUtils.addValidationErrors(testInput).errors.isEmpty()
  }

  def "validation passes tombstones through"() {
    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(null)

    then:
    ValidationUtils.addValidationErrors(testInput) == null
  }

  def "validates titles when #testCase"() {
    def titleAnalysis = TitleAnalysis.newBuilder(TestUtils.inputAvroRecord.analysis.titles).setTitleExists(titleExists).build()
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).setTitles(titleAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord).setAnalysis(analysis).build()

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(record)
    def validated = ValidationUtils.addValidationErrors(testInput)

    then:
    validated.errors.isEmpty() == isValid

    where:
    testCase                | isValid | titleExists
    "title is missing"      | false   | false
    "title is not missing"  | true    | true
  }

  def "validates identification when #testCase"() {
    def identificationAnalysis = IdentificationAnalysis.newBuilder(TestUtils.inputAvroRecord.analysis.identification)
        .setFileIdentifierExists(hasFileId)
        .setDoiExists(hasDoi)
        .setMatchesIdentifiers(matches)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).setIdentification(identificationAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord).setAnalysis(analysis).build()

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(record)
    def validated = ValidationUtils.addValidationErrors(testInput)

    then:
    validated.errors.size() == errors

    where:
    testCase                | errors  | hasFileId | hasDoi  | matches
    "has only fileId"       | 0       | true      | false   | true
    "has only doi"          | 0       | false     | true    | true
    "has no fileId nor doi" | 1       | false     | false   | true
    "has mismatched type"   | 1       | true      | true    | false
    "no id and mismatched"  | 2       | false     | false   | false
  }

  def "validates temporal bounds when #testCase"() {
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder(TestUtils.inputAvroRecord.analysis.temporalBounding)
        .setBeginDescriptor(beginValid ? VALID : INVALID)
        .setEndDescriptor(endValid ? VALID : INVALID)
        .setInstantDescriptor(instantValid ? VALID : INVALID)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).setTemporalBounding(temporalAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord).setAnalysis(analysis).build()

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(record)
    def validated = ValidationUtils.addValidationErrors(testInput)

    then:
    validated.errors.size() == errors

    where:
    testCase                    | errors  | beginValid| endValid| instantValid
    "has valid bounds"          | 0       | true      | true    | true
    "has invalid start"         | 1       | false     | true    | true
    "has invalid end"           | 1       | true      | false   | true
    "has invalid start and end" | 2       | false     | false   | true
    "is invalid instant"        | 1       | true      | true    | false
    "is completely invalid"     | 3       | false     | false   | false
  }

  def "validates spatial bounds when #testCase"() {
    def spatialAnalysis = SpatialBoundingAnalysis.newBuilder(TestUtils.inputAvroRecord.analysis.spatialBounding)
        .setSpatialBoundingExists(exists)
        .setIsValid(valid)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).setSpatialBounding(spatialAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord).setAnalysis(analysis).build()

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(record)
    def validated = ValidationUtils.addValidationErrors(testInput)

    then:
    validated.errors.size() == errors

    where:
    testCase                | errors  | exists  | valid
    "bounds are valid"      | 0       | true    | true
    "bounds are invalid"    | 1       | true    | false
    "bounds not not exist"  | 0       | false   | false
  }

  def "validates topic placement when #testCase"() {
    def identification = IdentificationAnalysis.newBuilder(TestUtils.inputAvroRecord.analysis.identification)
        .setParentIdentifierExists(hasParentId)
        .build()
    def discovery = Discovery.newBuilder(TestUtils.inputAvroRecord.getDiscovery()).setHierarchyLevelName(hlm).build()
    def analysis = Analysis.newBuilder(TestUtils.inputAvroRecord.analysis).setIdentification(identification).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputAvroRecord).setType(type).setAnalysis(analysis).setDiscovery(discovery).build()

    // Setup places record on the collection topic, so we overwrite setup here
    mockProcessorContext.setTopic(topic)
    ti = new TopicIdentifier<>()
    ti.init(mockProcessorContext)

    when:
    ValueWithTopic<ParsedRecord> testInput = ti.transform(record)
    def validated = ValidationUtils.addValidationErrors(testInput)

    then:
    validated.errors.size() == errors

    where:
    testCase                                        | errors | hasParentId | hlm          | type                  | topic
    "it's valid"                                    | 0      | false       | null         | RecordType.collection | TestUtils.collectionTopic
    "RecordType only doesn't match"                 | 1      | false       | "collection" | RecordType.granule    | TestUtils.collectionTopic
    "granule on collection topic (metadata check)"  | 1      | true        | "granule"    | RecordType.collection | TestUtils.collectionTopic
    "non-granule on granule topic (metadata check)" | 1      | false       | null         | RecordType.granule    | TestUtils.granuleTopic
    "metadata check and RecordType check fail"      | 2      | false       | "collection" | RecordType.collection | TestUtils.granuleTopic

  }
}
