package org.cedar.onestop.indexer.util;

import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.ValueWithTopic;
import org.cedar.schemas.avro.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.cedar.schemas.avro.psi.TimeRangeDescriptor.*;

/**
 * This class contains utilities for validating the contents of the Avro (schemas) records prior to indexing
 * into Elasticsearch,
 */
public class ValidationUtils {
  static final private Logger log = LoggerFactory.getLogger(ValidationUtils.class);

  public static ParsedRecord addValidationErrors(ValueWithTopic<ParsedRecord> value) {
    ParsedRecord record = value == null ? null : value.getValue();
    if (record == null) {
      return null;
    }
    List<ErrorEvent> errors = record.getErrors();
    List<ErrorEvent> rootErrors = validateRootRecord(record);
    errors.addAll(rootErrors);
    if (rootErrors.isEmpty()) {
      errors.addAll(validateIdentification(record));
      errors.addAll(validateTopicPlacement(record, value.getTopic()));
      errors.addAll(validateTitles(record));
      errors.addAll(validateTemporalBounds(record));
      errors.addAll(validateSpatialBounds(record));
    }
    return ParsedRecord.newBuilder(record).setErrors(errors).build();
  }

  public static List<ErrorEvent> validateRootRecord(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    if (record.getDiscovery() == null || record.getDiscovery().equals(Discovery.newBuilder().build())) {
      result.add(buildValidationError(ValidationError.ROOT,
          "Discovery metadata missing -- no metadata to load into OneStop."));
    }
    if (record.getAnalysis() == null || record.getAnalysis().equals(Analysis.newBuilder().build())) {
      result.add(buildValidationError(ValidationError.ROOT,
          "Analysis metadata missing -- cannot verify metadata quality for OneStop."));
    }
    return result;
  }

  public static List<ErrorEvent> validateIdentification(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var identification = record.getAnalysis().getIdentification();
    if (identification != null && !identification.getFileIdentifierExists() && !identification.getDoiExists()) {
      result.add(buildValidationError(ValidationError.IDENTIFICATION,
          "Missing identifier -- record contains neither a fileIdentifier nor a DOI"));
    }
    if (record.getType() == null ) {
      result.add(buildValidationError(ValidationError.IDENTIFICATION,
          "Metadata type error -- type unknown."));
    }
    return result;
  }

  public static List<ErrorEvent> validateTopicPlacement(ParsedRecord record, String topic) {
    var result = new ArrayList<ErrorEvent>();
    var declaredRecordType = record.getType();
    var recordTypeForTopic = IndexingUtils.determineRecordTypeFromTopic(topic);

    if(declaredRecordType != recordTypeForTopic) {
      result.add(buildValidationError(ValidationError.TYPE,
          "Declared record type [ " + declaredRecordType.toString() +
          " ] does not match expected type [ " + recordTypeForTopic.toString() +
          " ]. Metadata was ingested upstream into wrong topic."));
    }

    var identification = record.getAnalysis().getIdentification();
    var hln = record.getDiscovery().getHierarchyLevelName();
    // Granule on collection topic
    if(identification != null && identification.getIsGranule() && recordTypeForTopic != RecordType.granule) {
      result.add(buildValidationError(ValidationError.TYPE,
          "Metadata indicates granule type but record is not on granule topic."));
    }
    // Non-granule on granule topic
    if(identification != null && !identification.getIsGranule() && recordTypeForTopic == RecordType.granule) {
      result.add(buildValidationError(ValidationError.TYPE,
          "Metadata indicates non-granule type but record is on granule topic."));
      if(!identification.getParentIdentifierExists()) {
        result.add(buildValidationError(ValidationError.TYPE,
            "Expected granule record but missing parentIdentifier."));
      }
      if(!identification.getHierarchyLevelNameExists()) {
        result.add(buildValidationError(ValidationError.TYPE,
            "Expected granule record but missing hierarchyLevelName. This must be present and equal to case-insensitive 'granule'."));
      }
      if(identification.getHierarchyLevelNameExists() && !hln.toLowerCase().equals("granule")) {
        result.add(buildValidationError(ValidationError.TYPE,
            "Expected granule record but hierarchyLevelName is [ " + hln + " ] and should be case-insensitive 'granule'."));
      }
    }
    return result;
  }

  public static List<ErrorEvent> validateTitles(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var titles = record.getAnalysis().getTitles();
    if (!titles.getTitleExists()) {
      result.add(buildValidationError(ValidationError.TITLE,
          "Missing title"));
    }
    return result;
  }

  public static List<ErrorEvent> validateTemporalBounds(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var temporalAnalysis = record.getAnalysis().getTemporalBounding();

    // No temporal information is okay
    if (temporalAnalysis == null) {
      return result;
    }

    var range = temporalAnalysis.getRangeDescriptor();
    if (range == NOT_APPLICABLE) {
      // Range is always NOT_APPLICABLE when there is an error in one or more individual date fields; temporalBounding
      // access is null-safe here since an INVALID date only occurs with parsing errors
      var temporalDiscovery = record.getDiscovery().getTemporalBounding();
      var begin = temporalDiscovery.getBeginDate();
      var end = temporalDiscovery.getEndDate();
      var instant = temporalDiscovery.getInstant();
      if (temporalAnalysis.getBeginDescriptor() == ValidDescriptor.INVALID) {
        result.add(buildValidationError(ValidationError.TEMPORAL_FIELD,
            "The beginDate [ " + begin + " ] could not be parsed."));
      }
      if (temporalAnalysis.getEndDescriptor() == ValidDescriptor.INVALID) {
        result.add(buildValidationError(ValidationError.TEMPORAL_FIELD,
            "The endDate [ " + end + " ] could not be parsed."));
      }
      if (temporalAnalysis.getInstantDescriptor() == ValidDescriptor.INVALID) {
        result.add(buildValidationError(ValidationError.TEMPORAL_FIELD,
            "The instant [ " + instant + " ] could not be parsed."));
      }
    }
    else if (range == AMBIGUOUS) {
      result.add(buildValidationError(ValidationError.TEMPORAL_RANGE,
          "Ambiguous temporal bounding -- both an instant and a beginDate present, defining two valid ranges."));
    }
    else if (range == BACKWARDS) {
      result.add(buildValidationError(ValidationError.TEMPORAL_RANGE,
          "Backwards temporal bounding -- beginDate after endDate."));
    }
    else if (range == TimeRangeDescriptor.INVALID) {
      result.add(buildValidationError(ValidationError.TEMPORAL_RANGE,
          "Invalid temporal bounding -- endDate present without beginDate."));
    }

    return result;
  }

  public static List<ErrorEvent> validateSpatialBounds(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var spatial = record.getAnalysis().getSpatialBounding();
    if (spatial.getSpatialBoundingExists() && !spatial.getIsValid()) {
      result.add(buildValidationError(ValidationError.SPATIAL,
          "Invalid GeoJSON for spatial bounding"));
    }
    return result;
  }

  public enum ValidationError {
    ROOT("Record Missing Major Component"),
    IDENTIFICATION("Identification Error"),
    TYPE("Type Error"),
    TITLE("Title Error"),
    TEMPORAL_FIELD("Temporal Bounding Field Error"),
    TEMPORAL_RANGE("Temporal Bounding Range Error"),
    SPATIAL("Spatial Bounding Error");

    private final String title;
    ValidationError(String title) { this.title = title; }

    String getTitle() { return title; }
  }

  private static ErrorEvent buildValidationError(ValidationError errorCategory, String details) {
    return ErrorEvent.newBuilder()
        .setTitle(errorCategory.getTitle())
        .setDetail(details)
        .setSource(StreamsApps.INDEXER_ID)
        .build();
  }
}
