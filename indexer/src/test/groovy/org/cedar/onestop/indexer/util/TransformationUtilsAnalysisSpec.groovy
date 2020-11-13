package org.cedar.onestop.indexer.util

import org.cedar.schemas.analyze.Analyzers
import org.cedar.schemas.avro.psi.Analysis
import org.cedar.schemas.avro.psi.TemporalBoundingAnalysis
import org.cedar.schemas.avro.psi.ValidDescriptor
import org.cedar.schemas.avro.psi.TimeRangeDescriptor
import org.cedar.schemas.avro.psi.DataAccessAnalysis
import org.cedar.schemas.avro.psi.Discovery
import org.cedar.schemas.avro.psi.ErrorEvent
import org.cedar.schemas.avro.psi.ParsedRecord
import org.cedar.schemas.avro.psi.Relationship
import org.cedar.schemas.avro.psi.RelationshipType
import org.cedar.schemas.avro.psi.SpatialBoundingAnalysis
import org.cedar.schemas.avro.psi.ThumbnailAnalysis
import spock.lang.Specification
import spock.lang.Unroll


@Unroll
class TransformationUtilsAnalysisSpec extends Specification {

  def "reformat handles timestamp #label"() {
    when:
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(Analysis.newBuilder().build()).build()
    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(time, record)
    def indexedCollection = TransformationUtils.reformatCollectionForAnalysis(time, record)
    then:
    indexedGranule.getStagedDate() == time
    indexedCollection.getStagedDate() == time

    where:
    label | time
    'Thursday, July 29, 2010 5:32:16 PM' | 1280424736L
    'Saturday, January 1, 2000 12:00:00 PM' | 946728000L
  }

  def "reformat handles internalParentIdentifier for granule"() {
    when:
    def testId = "ABC"
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(Analysis.newBuilder().build()).setRelationships([
        Relationship.newBuilder().setType(RelationshipType.COLLECTION).setId(testId).build()
    ]).build()
    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getInternalParentIdentifier() == testId
  }

  def "reformat handles identification file: #fileId doi: #doi (#hierarchy)"() {
    when:
    def discovery = Discovery.newBuilder().setDoi(doi).setFileIdentifier(fileId).setHierarchyLevelName(hierarchy).setParentIdentifier(parent).build()
    def analysis = Analysis.newBuilder().setIdentification(Analyzers.analyzeIdentifiers(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatCollectionForAnalysis(12341234L, record)

    then:
    indexedGranule.getIdentification().getFileIdentifierExists() == fileIdExists
    indexedGranule.getIdentification().getFileIdentifierString() == fileId
    indexedGranule.getIdentification().getDoiExists() == doiExists
    indexedGranule.getIdentification().getDoiString() == doi
    indexedGranule.getIdentification().getHierarchyLevelNameExists() == hierarchyExists
    // indexedGranule.getIdentification().getIsGranule() == isGranule TODO ADD THIS!
    indexedGranule.getIdentification().getParentIdentifierExists() == parentIdExists
    indexedGranule.getIdentification().getParentIdentifierString() == parent

    and:
    indexedCollection.getIdentification().getFileIdentifierExists() == fileIdExists
    indexedCollection.getIdentification().getFileIdentifierString() == fileId
    indexedCollection.getIdentification().getDoiExists() == doiExists
    indexedCollection.getIdentification().getDoiString() == doi
    indexedCollection.getIdentification().getHierarchyLevelNameExists() == hierarchyExists
    // indexedCollection.getIdentification().getIsGranule() == isGranule TODO ADD THIS!
    indexedCollection.getIdentification().getParentIdentifierExists() == parentIdExists

    where:
    fileId | doi | parent | hierarchy  | fileIdExists | doiExists | hierarchyExists | parentIdExists
    'abc' | null | 'doi of parent'     | 'granule' | true | false | true | true
    null | '123' | 'file id of parent' | 'granule' | false | true | true | true
    'abc' | '123' | null               | null      | true | true | false | false
    'abc' | null | 'doi of parent'     | 'collection' | true | false | true | true
    null | '123' | 'file id of parent' | 'collection' | false | true | true | true
  }

  def "reformat handles data access: #label"() {
    when:
    def analysis = Analysis.newBuilder().setDataAccess(
      DataAccessAnalysis.newBuilder().setDataAccessExists(dataAccessExists).build()
    ).build() // populate analysis directly instead of running analysis on populated parsed record, for simplicity
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getDataAccess().getDataAccessExists() == dataAccessExists
    and:
    indexedCollection.getDataAccess().getDataAccessExists() == dataAccessExists

    where:
    label | dataAccessExists
    'has data access' | true
    'no data access' | false
  }

  def "reformat handles thumbnail: #label"() {
    when:
    def analysis = Analysis.newBuilder().setThumbnail(
      ThumbnailAnalysis.newBuilder().setThumbnailExists(thumbnailExists).build()
    ).build() // populate analysis directly instead of running analysis on populated parsed record, for simplicity
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getThumbnail().getThumbnailExists() == thumbnailExists
    and:
    indexedCollection.getThumbnail().getThumbnailExists() == thumbnailExists

    where:
    label  | thumbnailExists
    'has thumbnail' | true
    'no thumbnail' | false
  }

  def "reformat handles description #desc"() {
    when:

    def discovery = Discovery.newBuilder().setDescription(desc).build()
    def analysis = Analysis.newBuilder().setDescription(Analyzers.analyzeDescription(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getDescription().getDescriptionExists() == exists
    indexedGranule.getDescription().getDescriptionCharacters() == length

    and:
    indexedCollection.getDescription().getDescriptionExists() == exists
    indexedCollection.getDescription().getDescriptionCharacters() == length

    where:
    desc | exists | length
    'test description' | true | 16
    null | false | 0
  }

  def "reformat handles title #title"() {
    when:

    def discovery = Discovery.newBuilder().setTitle(title).setAlternateTitle(altTitle).build()
    def analysis = Analysis.newBuilder().setTitles(Analyzers.analyzeTitles(discovery)).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getTitles().getTitleExists() == exists
    indexedGranule.getTitles().getTitleCharacters() == length
    indexedGranule.getTitles().getAlternateTitleExists() == altExists
    indexedGranule.getTitles().getAlternateTitleCharacters() == altLength

    and:
    indexedCollection.getTitles().getTitleExists() == exists
    indexedCollection.getTitles().getTitleCharacters() == length
    indexedCollection.getTitles().getAlternateTitleExists() == altExists
    indexedCollection.getTitles().getAlternateTitleCharacters() == altLength

    where:
    title | altTitle | exists | length | altExists | altLength
    null | null | false | 0 | false | 0
    "title only" | null | true | 10 | false | 0
    "title and alt" | "alt" | true | 13 | true | 3
  }

  def "reformat handles temporal #label"() {
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

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getTemporalBounding().getBeginDescriptor() == expectedBeginDesc
    indexedGranule.getTemporalBounding().getBeginIndexable() == beginIndexable
    indexedGranule.getTemporalBounding().getBeginPrecision() == beginPrecision
    indexedGranule.getTemporalBounding().getBeginUtcDateTimeString() == beginString
    indexedGranule.getTemporalBounding().getBeginZoneSpecified() == beginZone
    indexedGranule.getTemporalBounding().getEndDescriptor() == expectedEndDesc
    indexedGranule.getTemporalBounding().getEndIndexable() == endIndexable
    indexedGranule.getTemporalBounding().getEndPrecision() == endPrecision
    indexedGranule.getTemporalBounding().getEndUtcDateTimeString() == endString
    indexedGranule.getTemporalBounding().getEndZoneSpecified() == endZone
    indexedGranule.getTemporalBounding().getInstantDescriptor() == expectedInstantDesc
    indexedGranule.getTemporalBounding().getInstantIndexable() == instantIndexable
    indexedGranule.getTemporalBounding().getInstantPrecision() == instantPrecision
    indexedGranule.getTemporalBounding().getInstantUtcDateTimeString() == instantString
    indexedGranule.getTemporalBounding().getInstantZoneSpecified() == instantZone
    indexedGranule.getTemporalBounding().getRangeDescriptor() == expectedRangeDesc

    and:
    indexedCollection.getTemporalBounding().getBeginDescriptor() == expectedBeginDesc
    indexedCollection.getTemporalBounding().getBeginIndexable() == beginIndexable
    indexedCollection.getTemporalBounding().getBeginPrecision() == beginPrecision
    indexedCollection.getTemporalBounding().getBeginUtcDateTimeString() == beginString
    indexedCollection.getTemporalBounding().getBeginZoneSpecified() == beginZone
    indexedCollection.getTemporalBounding().getEndDescriptor() == expectedEndDesc
    indexedCollection.getTemporalBounding().getEndIndexable() == endIndexable
    indexedCollection.getTemporalBounding().getEndPrecision() == endPrecision
    indexedCollection.getTemporalBounding().getEndUtcDateTimeString() == endString
    indexedCollection.getTemporalBounding().getEndZoneSpecified() == endZone
    indexedCollection.getTemporalBounding().getInstantDescriptor() == expectedInstantDesc
    indexedCollection.getTemporalBounding().getInstantIndexable() == instantIndexable
    indexedCollection.getTemporalBounding().getInstantPrecision() == instantPrecision
    indexedCollection.getTemporalBounding().getInstantUtcDateTimeString() == instantString
    indexedCollection.getTemporalBounding().getInstantZoneSpecified() == instantZone
    indexedCollection.getTemporalBounding().getRangeDescriptor() == expectedRangeDesc

    where:
    label | beginDesc | expectedBeginDesc | beginIndexable | beginPrecision | beginString | beginZone | endDesc | expectedEndDesc | endIndexable | endPrecision | endString | endZone | instantDesc | expectedInstantDesc | instantIndexable | instantPrecision | instantString | instantZone | rangeDesc | expectedRangeDesc
    'bounded' | ValidDescriptor.VALID | 'VALID' | true | 'Days' | '2001-01-01T00:00.00Z' | 'Z' | ValidDescriptor.VALID | 'VALID' | true | 'Nanos' | '2001-05-05T12:12:12.000Z' | 'Z' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | TimeRangeDescriptor.BOUNDED | 'BOUNDED'
    'instant' | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.UNDEFINED | 'UNDEFINED' | false | null | null | null | ValidDescriptor.INVALID | 'INVALID' | true | 'Month' | '2001-02' | 'Z' | TimeRangeDescriptor.INSTANT | 'INSTANT' // Note the timezone values are a total random guess. Everything else is approximately accurate to the best of my knowledge

  }

  def "reformat handles spatial #label"() {
    when:

    def analysis = Analysis.newBuilder().setSpatialBounding(
      SpatialBoundingAnalysis.newBuilder()
      .setSpatialBoundingExists(boundsExist)
      .setIsValid(isValid)
      .setValidationError(error)
      .build()).build()
    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(analysis).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getSpatialBounding().getSpatialBoundingExists() == boundsExist
    indexedGranule.getSpatialBounding().getIsValid() == isValid
    indexedGranule.getSpatialBounding().getValidationError() == error

    and:
    indexedCollection.getSpatialBounding().getSpatialBoundingExists() == boundsExist
    indexedCollection.getSpatialBounding().getIsValid() == isValid
    indexedCollection.getSpatialBounding().getValidationError() == error

    where:
    label | boundsExist | isValid | error
    'happy space' | true | true | null
    'sad space' | true | false | 'unable to parse'
    'empty space' | false | true | null
  }

  def "reformat handles errors #numErrors"() {
    when:

    ParsedRecord record = ParsedRecord.newBuilder().setAnalysis(Analysis.newBuilder().build()).setErrors(errors).build()

    def indexedGranule = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)
    def indexedCollection = TransformationUtils.reformatGranuleForAnalysis(12341234L, record)

    then:
    indexedGranule.getErrors().size() == numErrors
    indexedGranule.getErrors()[0].getTitle() == 'one'
    indexedGranule.getErrors()[0].getDetail() == 'first error'

    and:
    indexedCollection.getErrors().size() == numErrors
    indexedCollection.getErrors()[0].getTitle() == 'one'
    indexedCollection.getErrors()[0].getDetail() == 'first error'

    where:
    errors | numErrors
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build(), ErrorEvent.newBuilder().setTitle('two').setDetail('second error').build()] | 2
    [ErrorEvent.newBuilder().setTitle('one').setDetail('first error').build()] | 1
  }
}
