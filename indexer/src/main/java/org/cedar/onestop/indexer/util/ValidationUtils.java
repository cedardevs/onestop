package org.cedar.onestop.indexer.util;

import org.cedar.schemas.avro.psi.Analysis;
import org.cedar.schemas.avro.psi.Discovery;
import org.cedar.schemas.avro.psi.ErrorEvent;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.cedar.schemas.avro.psi.ValidDescriptor.INVALID;
import static org.cedar.schemas.avro.psi.ValidDescriptor.UNDEFINED;

public class ValidationUtils {
  static final private Logger log = LoggerFactory.getLogger(ValidationUtils.class);

  static final private String VALIDATION_ERROR_TITLE = "Invalid for search indexing";

  public static ParsedRecord addValidationErrors(ParsedRecord record) {
    if (record == null) {
      return null;
    }
    List<ErrorEvent> errors = record.getErrors();
    List<ErrorEvent> rootErrors = validateRootRecord(record);
    errors.addAll(rootErrors);
    if (rootErrors.isEmpty()) {
      errors.addAll(validateIdentification(record));
      errors.addAll(validateTitles(record));
      errors.addAll(validateTemporalBounds(record));
      errors.addAll(validateSpatialBounds(record));
    }
    return ParsedRecord.newBuilder(record).setErrors(errors).build();
  }

  private static List<ErrorEvent> validateRootRecord(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    if (record.getDiscovery() == null || record.getDiscovery() == Discovery.newBuilder().build()) {
      result.add(buildValidationError("Discovery metadata missing. No metadata to load into OneStop."));
    }
    if (record.getAnalysis() == null || record.getAnalysis() == Analysis.newBuilder().build()) {
      result.add(buildValidationError("Analysis metadata missing. Cannot verify metadata quality for OneStop."));
    }
    return result;
  }

  private static List<ErrorEvent> validateIdentification(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var identification = record.getAnalysis().getIdentification();
    if (identification != null && !identification.getFileIdentifierExists() && !identification.getDoiExists()) {
      result.add(buildValidationError("Missing identifier - record contains neither a fileIdentifier nor a DOI"));
    }
    if (record.getType() == null || (identification != null && !identification.getMatchesIdentifiers())) {
      result.add(buildValidationError("Metadata type error -- hierarchyLevelName is 'granule' but no parentIdentifier provided OR type unknown."));
    }
    return result;
  }

  private static List<ErrorEvent> validateTitles(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var titles = record.getAnalysis().getTitles();
    if (!titles.getTitleExists()) {
      result.add(buildValidationError("Missing title"));
    }
    return result;
  }

  private static List<ErrorEvent> validateTemporalBounds(ParsedRecord record) {
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

  private static List<ErrorEvent> validateSpatialBounds(ParsedRecord record) {
    var result = new ArrayList<ErrorEvent>();
    var spatial = record.getAnalysis().getSpatialBounding();
    if (spatial.getSpatialBoundingExists() && !spatial.getIsValid()) {
      result.add(buildValidationError("Invalid geoJSON for spatial bounding"));
    }
    return result;
  }

  private static ErrorEvent buildValidationError(String details) {
    return ErrorEvent.newBuilder()
        .setTitle(VALIDATION_ERROR_TITLE)
        .setDetail(details)
        .build();
  }
}
