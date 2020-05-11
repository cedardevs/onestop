package org.cedar.onestop.indexer.util;

import org.cedar.onestop.kafka.common.constants.StreamsApps;
import org.cedar.onestop.kafka.common.util.ValueWithTopic;
import org.cedar.schemas.avro.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.cedar.schemas.avro.psi.ValidDescriptor.INVALID;
import static org.cedar.schemas.avro.psi.ValidDescriptor.UNDEFINED;

/**
 * This class contains utilities for validating the contents of the Avro (schemas) records prior to indexing
 * into Elasticsearch,
 */
public class ValidationUtils {
  static final private Logger log = LoggerFactory.getLogger(ValidationUtils.class);

  static final private String VALIDATION_ERROR_TITLE = "Invalid for search indexing";

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
    if (record.getDiscovery() == null || record.getDiscovery() == Discovery.newBuilder().build()) {
      result.add(buildValidationError("Discovery metadata missing. No metadata to load into OneStop."));
    }
    if (record.getAnalysis() == null || record.getAnalysis() == Analysis.newBuilder().build()) {
      result.add(buildValidationError("Analysis metadata missing. Cannot verify metadata quality for OneStop."));
    }
    return result;
  }

  public static List<ErrorEvent> validateIdentification(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var identification = record.getAnalysis().getIdentification();
    if (identification != null && !identification.getFileIdentifierExists() && !identification.getDoiExists()) {
      result.add(buildValidationError("Missing identifier - record contains neither a fileIdentifier nor a DOI"));
    }
    if (record.getType() == null ) {
      result.add(buildValidationError("Metadata type error -- type unknown."));
    }
    return result;
  }

  public static List<ErrorEvent> validateTopicPlacement(ParsedRecord record, String topic) {
    var result = new ArrayList<ErrorEvent>();
    var declaredRecordType = record.getType();
    var recordTypeForTopic = IndexingUtils.determineTypeFromTopic(topic);

    if(declaredRecordType != recordTypeForTopic) {
      result.add(buildValidationError("Declared record type [ " + declaredRecordType.toString() +
          " ] does not match expected type [ " + recordTypeForTopic.toString() +
          " ]. Metadata was ingested downstream into wrong topic."));
    }

    var identification = record.getAnalysis().getIdentification();
    var hlm = record.getDiscovery().getHierarchyLevelName();
    // Granule on collection topic
    if(identification != null && identification.getIsGranule() && recordTypeForTopic != RecordType.granule) {
      result.add(buildValidationError("Metadata indicates granule type but record is not on granule topic."));
    }
    // Non-granule on granule topic
    if(identification != null && !identification.getIsGranule() && recordTypeForTopic == RecordType.granule) {
      result.add(buildValidationError("Metadata indicates non-granule type but record is on granule topic."));
      if(!identification.getParentIdentifierExists()) {
        result.add(buildValidationError("Expected granule record but missing parentIdentifier."));
      }
      if(!identification.getHierarchyLevelNameExists()) {
        result.add(buildValidationError("Expected granule record but missing hierarchyLevelName. This must be present and equal to case-insensitive 'granule'."));
      }
      if(identification.getHierarchyLevelNameExists() && !hlm.toLowerCase().equals("granule")) {
        result.add(buildValidationError("Expected granule record but hierarchyLevelName is [ " + hlm + " ] and should be case-insensitive 'granule'."));
      }
    }
    return result;
  }

  public static List<ErrorEvent> validateTitles(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var titles = record.getAnalysis().getTitles();
    if (!titles.getTitleExists()) {
      result.add(buildValidationError("Missing title"));
    }
    return result;
  }

  public static List<ErrorEvent> validateTemporalBounds(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var temporal = record.getAnalysis().getTemporalBounding();
    if (temporal.getBeginDescriptor() == INVALID) {
      result.add(buildValidationError("Invalid beginDate"));
    }
    if (temporal.getEndDescriptor() == INVALID) {
      result.add(buildValidationError("Invalid endDate"));
    }
    if (temporal.getBeginDescriptor() != UNDEFINED && temporal.getEndDescriptor() != UNDEFINED && temporal.getInstantDescriptor() == INVALID) {
      result.add(buildValidationError("Invalid instant-only date"));
    }
    return result;
  }

  public static List<ErrorEvent> validateSpatialBounds(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var spatial = record.getAnalysis().getSpatialBounding();
    if (spatial.getSpatialBoundingExists() && !spatial.getIsValid()) {
      result.add(buildValidationError("Invalid GeoJSON for spatial bounding"));
    }
    return result;
  }

  private static ErrorEvent buildValidationError(String details) {
    return ErrorEvent.newBuilder()
        .setTitle(VALIDATION_ERROR_TITLE)
        .setDetail(details)
        .setSource(StreamsApps.INDEXER_ID)
        .build();
  }
}
