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

  def "validates titles when #testCase"() {
    def titleAnalysis = TitleAnalysis.newBuilder().setTitleExists(titleExists).build()
    def analysis = Analysis.newBuilder().setTitles(titleAnalysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateTitles(record)

    then:
    errors.isEmpty() == isValid

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

    where:
    testCase                      | errorCount  | hasFileId | hasDoi  | type
    "has only fileId"             | 0           | true      | false   | RecordType.collection
    "has only doi"                | 0           | false     | true    | RecordType.granule
    "has fileId and doi"          | 0           | true      | true    | RecordType.collection
    "has no ids"                  | 1           | false     | false   | RecordType.granule
    "has unknown type"            | 1           | true      | true    | null
    "has no ids and unknown type" | 2           | false     | false   | null
  }

  def "validates temporal bounds when #testCase"() {
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder()
        .setBeginDescriptor(beginValid ? VALID : INVALID)
        .setEndDescriptor(endValid ? VALID : INVALID)
        .setInstantDescriptor(instantValid ? VALID : INVALID)
        .build()
    def analysis = Analysis.newBuilder().setTemporalBounding(temporalAnalysis).build()
    def record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    when:
    def errors = ValidationUtils.validateTemporalBounds(record)

    then:
    errors.size() == errorCount

    where:
    testCase                    | errorCount  | beginValid| endValid| instantValid
    "has valid bounds"          | 0           | true      | true    | true
    "has invalid start"         | 1           | false     | true    | true
    "has invalid end"           | 1           | true      | false   | true
    "has invalid start and end" | 2           | false     | false   | true
    "is invalid instant"        | 1           | true      | true    | false
    "is completely invalid"     | 3           | false     | false   | false
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
