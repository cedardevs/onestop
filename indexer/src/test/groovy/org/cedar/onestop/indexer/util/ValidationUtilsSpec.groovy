package org.cedar.onestop.indexer.util

import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.IdentificationAnalysis
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.SpatialBoundingAnalysis
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.TitleAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import static org.cedar.schemas.avro.psi.ValidDescriptor.INVALID
import static org.cedar.schemas.avro.psi.ValidDescriptor.VALID

@Unroll
class ValidationUtilsSpec extends Specification {

  def "valid message passes validation check"() {
    expect:
    ValidationUtils.addValidationErrors(TestUtils.inputRecord).errors.isEmpty()
  }

  def "validation passes tombstones through"() {
    expect:
    ValidationUtils.addValidationErrors(null) == null
  }

  def "invalid records have errors added"() {
    given:
    def titleAnalysis = TitleAnalysis.newBuilder(TestUtils.inputRecord.analysis.titles)
        .setTitleExists(false)
        .build()
    def idAnalysis = IdentificationAnalysis.newBuilder(TestUtils.inputRecord.analysis.identification)
        .setFileIdentifierExists(false)
        .setParentIdentifierExists(false)
        .build()
    def timeAnalysis = TemporalBoundingAnalysis.newBuilder(TestUtils.inputRecord.analysis.temporalBounding)
        .setBeginDescriptor(INVALID)
        .setBeginUtcDateTimeString(null)
        .setEndDescriptor(INVALID)
        .setEndUtcDateTimeString(null)
        .setInstantDescriptor(ValidDescriptor.UNDEFINED)
        .setInstantUtcDateTimeString(null)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputRecord.analysis)
        .setTitles(titleAnalysis)
        .setIdentification(idAnalysis)
        .setTemporalBounding(timeAnalysis)
        .build()
    def record = ParsedRecord.newBuilder(TestUtils.inputRecord)
        .setAnalysis(analysis)
        .build()

    when:
    def validated = ValidationUtils.addValidationErrors(record)

    then:
    !validated.errors.isEmpty()
  }

  def "validates titles when #testCase"() {
    def titleAnalysis = TitleAnalysis.newBuilder(TestUtils.inputRecord.analysis.titles).setTitleExists(titleExists).build()
    def analysis = Analysis.newBuilder(TestUtils.inputRecord.analysis).setTitles(titleAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputRecord).setAnalysis(analysis).build()

    when:
    def validated = ValidationUtils.addValidationErrors(record)

    then:
    validated.errors.isEmpty() == isValid

    where:
    testCase                | isValid | titleExists
    "title is missing"      | false   | false
    "title is not missing"  | true    | true
  }

  def "validates identification when #testCase"() {
    def identificationAnalysis = IdentificationAnalysis.newBuilder(TestUtils.inputRecord.analysis.identification)
        .setFileIdentifierExists(hasFileId)
        .setDoiExists(hasDoi)
        .setMatchesIdentifiers(matches)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputRecord.analysis).setIdentification(identificationAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputRecord).setAnalysis(analysis).build()

    when:
    def validated = ValidationUtils.addValidationErrors(record)

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
    def temporalAnalysis = TemporalBoundingAnalysis.newBuilder(TestUtils.inputRecord.analysis.temporalBounding)
        .setBeginDescriptor(beginValid ? VALID : INVALID)
        .setEndDescriptor(endValid ? VALID : INVALID)
        .setInstantDescriptor(instantValid ? VALID : INVALID)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputRecord.analysis).setTemporalBounding(temporalAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputRecord).setAnalysis(analysis).build()

    when:
    def validated = ValidationUtils.addValidationErrors(record)

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
    def spatialAnalysis = SpatialBoundingAnalysis.newBuilder(TestUtils.inputRecord.analysis.spatialBounding)
        .setSpatialBoundingExists(exists)
        .setIsValid(valid)
        .build()
    def analysis = Analysis.newBuilder(TestUtils.inputRecord.analysis).setSpatialBounding(spatialAnalysis).build()
    def record = ParsedRecord.newBuilder(TestUtils.inputRecord).setAnalysis(analysis).build()

    when:
    def validated = ValidationUtils.addValidationErrors(record)

    then:
    validated.errors.size() == errors

    where:
    testCase                | errors  | exists  | valid
    "bounds are valid"      | 0       | true    | true
    "bounds are invalid"    | 1       | true    | false
    "bounds not not exist"  | 0       | false   | false
  }
}
