package org.cedar.onestop.indexer.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.cedar.schemas.avro.psi.*;
import org.cedar.schemas.avro.util.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cedar.schemas.avro.psi.ValidDescriptor.UNDEFINED;
import static org.cedar.schemas.avro.psi.ValidDescriptor.VALID;
// import org.cedar.schemas.analyze.Temporal;
//
// import java.time.temporal.ChronoField;

import org.cedar.onestop.kafka.common.util.DataUtils;

// TODO import org.apache.kafka.streams.StreamsBuilder;

/**
 * This class contains utilities for transforming the contents of the Avro (schemas) records into the appropriate
 * corresponding Elasticsearch mapping format.
 */
public class TransformationUtils {
  static final private Logger log = LoggerFactory.getLogger(TransformationUtils.class);

  ///////////////////////////////////////////////////////////////////////////////
  //                     Convert to Indexing Message                           //
  ///////////////////////////////////////////////////////////////////////////////
  public static Map<String, Object> reformatMessageForAnalysis(ParsedRecord record, Set<String> fields, RecordType recordType) {

    var analysis = record.getAnalysis();
    var errors = record.getErrors();
    var analysisMap = AvroUtils.avroToMap(analysis, true);
    var message = new HashMap<String, Object>();

    fields.forEach(field -> {
      message.put(field, analysisMap.get(field));
    });
    if (fields.contains("internalParentIdentifier")) {
      analysisMap.put("internalParentIdentifier", prepareInternalParentIdentifier(record));
    }
    var errorsList = errors.stream()
        .map(e -> AvroUtils.avroToMap(e))
        .collect(Collectors.toList());

    if (fields.contains("errors")) {
      message.put("errors", errorsList);
    }

    if (fields.contains("temporalBounding")) {
      message.put("temporalBounding", prepareTemporalBounding(analysis.getTemporalBounding()));
    }
    if (fields.contains("identification")) {
      message.put("identification", prepareIdentification(analysis.getIdentification(), recordType));
    }

    return message;
  }

  public static Map<String, Object> prepareIdentification(IdentificationAnalysis identification, RecordType recordType) {
    var result = new HashMap<String, Object>();
    var analysis = AvroUtils.avroToMap(identification); // TODO using map because I need javadocs on the IdentificationAnalysis object...

    if (analysis == null) {
      return result;
    }
    result.put("doiExists", analysis.get("doiExists"));
    result.put("doiString", analysis.get("doiString"));
    result.put("fileIdentifierExists", analysis.get("fileIdentifierExists"));
    result.put("fileIdentifierString", analysis.get("fileIdentifierString"));
    result.put("hierarchyLevelNameExists", analysis.get("hierarchyLevelNameExists"));
    result.put("isGranule", analysis.get("isGranule"));
    result.put("parentIdentifierExists", analysis.get("parentIdentifierExists"));
    if (recordType == RecordType.granule) {
      result.put("parentIdentifierString", analysis.get("parentIdentifierString"));
    }
    return result;
  }

  public static Map<String, Object> reformatMessageForSearch(ParsedRecord record, Set<String> fields) {

    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();
    var errors = record.getErrors();
    var discoveryMap = AvroUtils.avroToMap(discovery, true);
    var analysisMap = AvroUtils.avroToMap(analysis, true);
    var message = new HashMap<String, Object>();

    fields.forEach(field -> {
      message.put(field, discoveryMap.get(field));
    });
    var errorsList = errors.stream()
        .map(e -> AvroUtils.avroToMap(e))
        .collect(Collectors.toList());

    if (fields.contains("errors")) {
      message.put("errors", errorsList);
    }

    // prepare and apply fields that need to be reformatted for search
    Map<String, Set<String>> gcmdKeywords = prepareGcmdKeyword(discovery);
    gcmdKeywords.forEach((key, value) -> {
      if (fields.contains(key)) {
        message.put(key, value);
      }
    });
    Map<String, Object> dates = prepareDates(discovery.getTemporalBounding(), analysis.getTemporalBounding());
    dates.forEach((key, value) -> {
      if (fields.contains(key)) {
        message.put(key, value);
      }
    });
    if (fields.contains("temporalBounding")) {
      message.put("temporalBounding", prepareTemporalBounding(analysis.getTemporalBounding()));
    }
    if (fields.contains("dataFormat")) {
      message.put("dataFormat", prepareDataFormats(discovery));
    }
    if (fields.contains("linkProtocol")) {
      message.put("linkProtocol", prepareLinkProtocols(discovery));
    }
    if (fields.contains("serviceLinks")) {
      message.put("serviceLinks", prepareServiceLinks(discovery));
    }
    if (fields.contains("serviceLinkProtocol")) {
      message.put("serviceLinkProtocol", prepareServiceLinkProtocols(discovery));
    }
    Map<String, Set<String>> responsibleParties = prepareResponsibleParties(record);
    responsibleParties.forEach((key, value) -> {
      if (fields.contains(key)) {
        message.put(key, value);
      }
    });

    if (fields.contains("internalParentIdentifier")) {
      message.put("internalParentIdentifier", prepareInternalParentIdentifier(record));
    }
    if (fields.contains("filename")) {
      message.put("filename", prepareFilename(record));
    }
    if (fields.contains("checksums")) {
      message.put("checksums", prepareChecksums(record));
    }
    return message;
  }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////
  private static String prepareInternalParentIdentifier(ParsedRecord record) {
    return Optional.ofNullable(record)
        .filter(r -> r.getType() == RecordType.granule)
        .map(ParsedRecord::getRelationships)
        .orElse(Collections.emptyList())
        .stream()
        .filter(rel -> rel.getType() == RelationshipType.COLLECTION)
        .findFirst()
        .map(Relationship::getId)
        .orElse(null);
  }

  static String prepareFilename(ParsedRecord record) {
    return Optional.ofNullable(record)
        .filter(r -> r.getType() == RecordType.granule)
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getName)
        .orElse(null);
  }

  static List prepareChecksums(ParsedRecord record) {
    return Optional.ofNullable(record)
        .filter(r -> r.getType() == RecordType.granule)
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getChecksums)
        .orElse(Collections.emptyList())
        .stream()
        .map(checksumObject -> {
          var result = new HashMap<>();
          result.put("algorithm", checksumObject.getAlgorithm());
          result.put("value", checksumObject.getValue());
          return result;
        })
        .collect(Collectors.toList());
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  private static List<Map> prepareServiceLinks(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getServices)
        .orElse(Collections.emptyList())
        .stream()
        .map(service -> {
          var result = new HashMap<>();
          result.put("title", service.getTitle());
          result.put("alternateTitle", service.getAlternateTitle());
          result.put("description", service.getDescription());
          result.put("links", AvroUtils.avroCollectionToList(new ArrayList<>(getLinksForService(service)), true));
          return result;
        })
        .collect(Collectors.toList());
  }

  // see: https://github.com/OSGeo/Cat-Interop/blob/master/LinkPropertyLookupTable.csv
  private static Set<String> prepareServiceLinkProtocols(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getServices)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(service -> getLinksForService(service).stream())
        .flatMap(serviceLink -> {
          var serviceLinkProtocolKeyword = normalizeLinkProtocol(serviceLink);
          return serviceLinkProtocolKeyword != null && !serviceLinkProtocolKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(serviceLinkProtocolKeyword) :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  // see: https://github.com/OSGeo/Cat-Interop/blob/master/LinkPropertyLookupTable.csv
  private static Set<String> prepareLinkProtocols(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getLinks)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(link -> {
          var linkProtocolKeyword = normalizeLinkProtocol(link);
          return linkProtocolKeyword != null && !linkProtocolKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(linkProtocolKeyword) :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  private static List<Link> getLinksForService(Service service) {
    return Optional.ofNullable(service)
        .map(Service::getOperations)
        .map(it -> {
          it.sort(Link::compareTo);
          return it;
        })
        .orElse(List.of());
  }

  private static String normalizeLinkProtocol(Link link) {
    return Optional.ofNullable(link)
        .map(Link::getLinkProtocol)
        .map(it -> cleanInternalKeywordWhitespace(it).trim().toUpperCase())
        .orElse(null);
  }

  ////////////////////////////
  // Data Formats           //
  ////////////////////////////
  private static Set<String> prepareDataFormats(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getDataFormats)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(dataFormat -> {
          // normalize (clean whitespace/trim/uppercase/nullify empty string) contents of DataFormat to check for uniqueness
          String dataFormatKeyword = normalizeHierarchicalDataFormat(dataFormat);
          return dataFormatKeyword != null && !dataFormatKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(dataFormatKeyword) :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  private static String normalizeHierarchicalDataFormat(DataFormat dataFormat) {
    String name = Optional.ofNullable(dataFormat)
        .map(DataFormat::getName)
        .filter(Predicate.not(StringUtils::isBlank))
        .map(it -> cleanInternalKeywordWhitespace(it).trim().toUpperCase())
        .orElse(null);
    String version = Optional.ofNullable(dataFormat)
        .map(DataFormat::getVersion)
        .filter(Predicate.not(StringUtils::isBlank))
        .map(it -> cleanInternalKeywordWhitespace(it).trim().toUpperCase())
        .orElse(null);

    if (name == null) {
      return null;
    } else if (version == null) {
      return name;
    } else {
      return name + " > " + version;
    }
  }

  ////////////////////////////
  // Responsible Parties    //
  ////////////////////////////
  private static Map<String, Set<String>> prepareResponsibleParties(ParsedRecord record) {
    Set<String> individualNames = new HashSet<>();
    Set<String> organizationNames = new HashSet<>();
    Optional.ofNullable(record)
        .filter(r -> r.getType() == RecordType.collection)
        .map(ParsedRecord::getDiscovery)
        .map(Discovery::getResponsibleParties)
        .orElse(Collections.emptyList())
        .stream()
        .filter(p -> p.getRole() != null && !p.getRole().isBlank())
        .filter(p -> !categorizeParty.apply(p).equals("other"))
        .forEach(party -> {
          var individualName = party.getIndividualName();
          if (individualName != null && !individualName.isBlank()) {
            individualNames.add(individualName);
          }
          var organizationName = party.getOrganizationName();
          if (organizationName != null && !organizationName.isBlank()) {
            organizationNames.add(organizationName);
          }
        });
    var result = new HashMap<String, Set<String>>();
    result.put("individualNames", individualNames);
    result.put("organizationNames", organizationNames);
    return result;
  }

  private static final Set<String> contactRoles = Set.of("pointOfContact", "distributor");
  private static final Set<String> creatorRoles = Set.of("resourceProvider", "originator", "principalInvestigator", "author", "collaborator", "coAuthor");
  private static final Set<String> publisherRoles = Set.of("publisher");
  private static final Function<ResponsibleParty, String> categorizeParty = p ->
      contactRoles.contains(p.getRole()) ? "contacts" :
          creatorRoles.contains(p.getRole()) ? "creators" :
              publisherRoles.contains(p.getRole()) ? "publishers" :
                  "other";

  ////////////////////////////
  // Dates                  //
  ////////////////////////////

  private static Map<String, Object> prepareTemporalBounding(TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();
    result.put("beginDescriptor", analysis.getBeginDescriptor());
    result.put("beginIndexable", analysis.getBeginIndexable());
    result.put("beginPrecision", analysis.getBeginPrecision());
    result.put("beginUtcDateTimeString", analysis.getBeginUtcDateTimeString());
    result.put("beginZoneSpecified", analysis.getBeginZoneSpecified());
    result.put("endDescriptor", analysis.getEndDescriptor());
    result.put("endIndexable", analysis.getEndIndexable());
    result.put("endPrecision", analysis.getEndPrecision());
    result.put("endUtcDateTimeString", analysis.getEndUtcDateTimeString());
    result.put("endZoneSpecified", analysis.getEndZoneSpecified());
    result.put("instantDescriptor", analysis.getInstantDescriptor());
    result.put("instantIndexable", analysis.getInstantIndexable());
    result.put("instantPrecision", analysis.getInstantPrecision());
    result.put("instantUtcDateTimeString", analysis.getInstantUtcDateTimeString());
    result.put("instantZoneSpecified", analysis.getInstantZoneSpecified());
    result.put("rangeDescriptor", analysis.getRangeDescriptor());
    return result;
  }

  private static Map<String, Object> prepareDatesForInstant(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    String beginDate, endDate;
    Long beginYear, endYear;
    Integer beginDayOfYear, beginDayOfMonth, beginMonth;
    Integer endDayOfYear, endDayOfMonth, endMonth;
    var result = new HashMap<String, Object>();

    if (!analysis.getInstantIndexable()) {
      // paleo dates are not indexable, so don't add beginDate or endDate to the index
      beginDate = null;
      endDate = null;
      beginDayOfYear = null;
      beginDayOfMonth = null;
      beginMonth = null;
      endDayOfYear = null;
      endDayOfMonth = null;
      endMonth = null;
    } else {
      beginDate = analysis.getInstantUtcDateTimeString();
      var precision = analysis.getInstantPrecision();
      if (precision.equals(ChronoUnit.DAYS.toString())) {
        // End of day
        endDate = bounding.getInstant() + "T23:59:59Z";
      } else if (precision.equals(ChronoUnit.YEARS.toString())) {
        // Last day of year + end of day
        endDate = bounding.getInstant() + "-12-31T23:59:59Z";
      } else {
        // Precision is NANOS so use instant value as-is
        // TODO hopefully it's not a weird rare precision like "month"
        endDate = beginDate;
      }

      if (analysis.getInstantDayOfYear() != null) {
        beginDayOfYear = analysis.getInstantDayOfYear();
        endDayOfYear = analysis.getInstantDayOfYear();
      } else {
        beginDayOfYear = 1;
        endDayOfYear = 365; // TODO leap year
      }

      if (analysis.getInstantDayOfMonth() != null) {
        beginDayOfMonth = analysis.getInstantDayOfMonth();
        endDayOfMonth = analysis.getInstantDayOfMonth();
      } else {
        beginDayOfMonth = 1;
        endDayOfMonth = 31; // TODO depends on if there is a month but no day, but for the moment I'm assuming it's Year or Day precision, but not month...
      }

      if (analysis.getInstantMonth() != null) {
        beginMonth = analysis.getInstantMonth();
        endMonth = analysis.getInstantMonth();
      } else {
        beginMonth = 1;
        endMonth = 12;
      }
    }

    beginYear = analysis.getInstantYear();
    endYear = analysis.getInstantYear();

    result.put("beginDate", beginDate);
    result.put("beginYear", beginYear);
    result.put("beginDayOfYear", beginDayOfYear);
    result.put("beginDayOfMonth", beginDayOfMonth);
    result.put("beginMonth", beginMonth);

    result.put("endDate", endDate);
    result.put("endYear", endYear);
    result.put("endDayOfYear", endDayOfYear);
    result.put("endDayOfMonth", endDayOfMonth);
    result.put("endMonth", endMonth);

    return result;
  }

  private static Map<String, Object> prepareBeginDate(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();
    Integer beginDayOfYear, beginDayOfMonth, beginMonth;

    if (analysis.getBeginDescriptor() == VALID) {
      if (analysis.getBeginIndexable()) {
        result.put("beginDate", analysis.getBeginUtcDateTimeString());

        var precision = analysis.getBeginPrecision();

        // if (Temporal.extractField(parsedDate, ChronoField.DAY_OF_YEAR) != null) {
        if (precision.equals(ChronoUnit.DAYS.toString()) || precision.equals(ChronoUnit.NANOS.toString())) {
          beginDayOfYear = analysis.getBeginDayOfYear();
          beginDayOfMonth = analysis.getBeginDayOfMonth();
          beginMonth = analysis.getBeginMonth();
        }
        else {
          beginDayOfYear = 1;
          beginDayOfMonth = 1;
          beginMonth = 1; // TODO base off month precision, if applicable
        }

      } else {
        beginDayOfYear = null;
        beginDayOfMonth = null;
        beginMonth = null;
      }
      result.put("beginYear", analysis.getBeginYear());

      // if (precision.equals(ChronoUnit.DAYS.toString())) {
      //   beginDayOfYear = analysis.getBeginDayOfYear();
      //   beginDayOfMonth = analysis.getBeginDayOfMonth();
      //   beginMonth = analysis.getBeginMonth();
      // }
      // // else {
      // //   beginDayOfYear = 1;
      // //   beginDayOfMonth = 1;
      // //   beginMonth = 1; // TODO base off month precision, if applicable
      // // }
      // else {
      //   beginDayOfYear = null;
      //   beginDayOfMonth = null;
      //   beginMonth = null;
      // }

      result.put("beginDayOfYear", beginDayOfYear);
      result.put("beginDayOfMonth", beginDayOfMonth);
      result.put("beginMonth", beginMonth);
    }
    return result;
  }

  private static Map<String, Object> prepareEndDate(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();
    Integer endDayOfYear, endDayOfMonth, endMonth;

    if (analysis.getEndDescriptor() == VALID) {
      if (analysis.getEndIndexable()) {
        result.put("endDate", analysis.getEndUtcDateTimeString());

        var precision = analysis.getEndPrecision();
        if (precision.equals(ChronoUnit.DAYS.toString())) {
          endDayOfYear = analysis.getEndDayOfYear();
          endDayOfMonth = analysis.getEndDayOfMonth();
          endMonth = analysis.getEndMonth();
        }
        else { // TODO this implies other precision checks (begin date) are also needed
          endDayOfYear = 365; // TODO leap years
          endDayOfMonth = 31; // TODO base off month precision, if applicable
          endMonth = 12; // TODO base off month precision, if applicable
        }
      } else {
        endDayOfYear = null;
        endDayOfMonth = null;
        endMonth = null;
      }
      result.put("endYear", analysis.getEndYear());


      // if (precision.equals(ChronoUnit.DAYS.toString())) {
      //   endDayOfYear = analysis.getEndDayOfYear();
      //   endDayOfMonth = analysis.getEndDayOfMonth();
      //   endMonth = analysis.getEndMonth();
      // }
      // // else { // TODO this implies other precision checks (begin date) are also needed
      //   // endDayOfYear = 365; // TODO leap years
      //   // endDayOfMonth = 31; // TODO base off month precision, if applicable
      //   // endMonth = 12; // TODO base off month precision, if applicable
      // // }
      // else {
      //   endDayOfYear = null;
      //   endDayOfMonth = null;
      //   endMonth = null;
      // }

      result.put("endDayOfYear", endDayOfYear);
      result.put("endDayOfMonth", endDayOfMonth);
      result.put("endMonth", endMonth);
    }
    return result;
  }

  private static Map<String, Object> prepareDates(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    String beginDate, endDate;
    // Long year;
    Long beginYear, endYear;
    Integer beginDayOfYear, beginDayOfMonth, beginMonth;
    Integer endDayOfYear, endDayOfMonth, endMonth;
    var result = new HashMap<String, Object>();

    // If bounding is actually an instant, set search fields accordingly
    if (analysis.getRangeDescriptor() == TimeRangeDescriptor.INSTANT && analysis.getBeginDescriptor() == UNDEFINED) {
      return prepareDatesForInstant(bounding, analysis);
    } else {
      // If dates exist (thus VALID) and are indexable use value from analysis block where dates are UTC datetime normalized,
      // else only set the year values as this is indicative of a paleo date TODO does this all behave the same now?

      result.putAll(prepareBeginDate(bounding, analysis));
      result.putAll(prepareEndDate(bounding, analysis));
      // if (analysis.getBeginDescriptor() == VALID && analysis.getBeginIndexable()) {
      //   beginDate = analysis.getBeginUtcDateTimeString();
      //   beginYear = analysis.getBeginYear();
      //   var precision = analysis.getBeginPrecision();
      //   if (precision.equals(ChronoUnit.DAYS.toString())) {
      //     beginDayOfYear = analysis.getBeginDayOfYear();
      //     beginDayOfMonth = analysis.getBeginDayOfMonth();
      //     beginMonth = analysis.getBeginMonth();
      //   } else {
      //     beginDayOfYear = 1;
      //     beginDayOfMonth = 1;
      //     beginMonth = 1; // TODO base off month precision, if applicable
      //   }
      // } else {
      //   beginDate = null;
      //   beginYear = null;
      //   beginDayOfYear = null;
      //   beginDayOfMonth = null;
      //   beginMonth = null;
      // }
      // if (analysis.getEndDescriptor() == VALID && analysis.getEndIndexable()) {
      //   endDate = analysis.getEndUtcDateTimeString();
      //   endYear = analysis.getEndYear();
      //   var precision = analysis.getEndPrecision();
      //   if (precision.equals(ChronoUnit.DAYS.toString())) {
      //     endDayOfYear = analysis.getEndDayOfYear();
      //     endDayOfMonth = analysis.getEndDayOfMonth();
      //     endMonth = analysis.getEndMonth();
      //   } else { // TODO this implies other precision checks (begin date) are also needed
      //     endDayOfYear = 365; // TODO leap years
      //     endDayOfMonth = 31; // TODO base off month precision, if applicable
      //     endMonth = 12; // TODO base off month precision, if applicable
      //   }
      // } else {
      //   endDate = null;
      //   endYear = null;
      //   endDayOfYear = null;
      //   endDayOfMonth = null;
      //   endMonth = null;
      // }
    }

    // result.put("beginDate", beginDate);
    // result.put("beginYear", beginYear);
    // result.put("beginDayOfYear", beginDayOfYear);
    // result.put("beginDayOfMonth", beginDayOfMonth);
    // result.put("beginMonth", beginMonth);
    // result.putAll(parseAdditionalTimeFields("begin", beginDate));

    // result.put("endDate", endDate);
    // result.put("endYear", endYear);
    // result.put("endDayOfYear", endDayOfYear);
    // result.put("endDayOfMonth", endDayOfMonth);
    // result.put("endMonth", endMonth);
    // result.putAll(parseAdditionalTimeFields("end", endDate));

    return result;
  }

  // private static HashMap<String, Object> parseAdditionalTimeFields(String prefix, String time){
  //   var result = new HashMap<String, Object>();
  //   try {
  //
  //     Integer dayOfYear, dayOfMonth, month;
  //     if (time != null) {
  //       ZonedDateTime dateTime = ZonedDateTime.parse(time);
  //
  //       dayOfYear = dateTime.getDayOfYear();
  //       dayOfMonth = dateTime.getDayOfMonth();
  //       month = dateTime.getMonthValue();
  //     }
  //     else {
  //       dayOfYear = null;
  //       dayOfMonth = null;
  //       month = null;
  //     }
  //
  //     result.put(prefix + "DayOfYear", dayOfYear);
  //     result.put(prefix + "DayOfMonth", dayOfMonth);
  //     result.put(prefix + "Month", month);
  //   } catch (Exception e) {} // TODO temporary
  //   return result;
  // }

  // private static Long parseYear(String utcDateTime) {
  //   if (StringUtils.isBlank(utcDateTime)) {
  //     return null;
  //   } else {
  //     // Watch out for BCE years
  //     return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)));
  //   }
  // }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  private static Map<String, Set<String>> prepareGcmdKeyword(Discovery discovery) {
    var allKeywords = new HashSet<String>();
    var groupedKeywords = Optional.ofNullable(discovery)
        .map(Discovery::getKeywords)
        .orElse(Collections.emptyList())
        .stream()
        .filter(g -> !g.getNamespace().equals("NCEI ACCESSION NUMBER"))
        .flatMap(g -> g.getValues().stream().map(value -> new SingleKeyword(g.getNamespace(), value)))
        .peek(k -> allKeywords.add(k.value))
        .map(TransformationUtils::normalizeKeyword)
        .flatMap(TransformationUtils::tokenizeKeyword)
        .filter(k -> k.category != KeywordCategory.other)
        .collect(Collectors.groupingBy(
            keyword -> keyword.category.name(), // group by the category label
            Collectors.mapping(keyword -> keyword.value, Collectors.toSet()))); // map the SingleKeywords to their values then collect them in a Set
    groupedKeywords.put("keywords", allKeywords);
    return groupedKeywords;
  }

  private enum KeywordCategory {
    gcmdScience(TransformationUtils::normalizeHierarchyKeyword, TransformationUtils::tokenizeHierarchyKeyword),
    gcmdScienceServices(TransformationUtils::normalizeHierarchyKeyword, TransformationUtils::tokenizeHierarchyKeyword),
    gcmdLocations(TransformationUtils::normalizeHierarchyKeyword, TransformationUtils::tokenizeHierarchyKeyword),
    gcmdPlatforms(TransformationUtils::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdInstruments(TransformationUtils::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdProjects(TransformationUtils::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdHorizontalResolution(TransformationUtils::normalizePlainKeyword, Stream::of),
    gcmdVerticalResolution(TransformationUtils::normalizePlainKeyword, Stream::of),
    gcmdTemporalResolution(TransformationUtils::normalizePlainKeyword, Stream::of),
    gcmdDataCenters(TransformationUtils::normalizeNonHierarchicalKeyword, Stream::of),
    other(TransformationUtils::normalizePlainKeyword, Stream::of);

    final Function<String, String> normalizer;
    final Function<String, Stream<String>> tokenizer;

    KeywordCategory(Function<String, String> normalizer, Function<String, Stream<String>> tokenizer) {
      this.normalizer = normalizer;
      this.tokenizer = tokenizer;
    }
  }

  private static class SingleKeyword {
    final String namespace;
    final String value;
    final KeywordCategory category;

    SingleKeyword(String namespace, String value) {
      this.namespace = namespace;
      this.value = value;
      this.category = categorizeGcmdKeyword(namespace, value);
    }

    SingleKeyword(String namespace, String value, KeywordCategory category) {
      this.namespace = namespace;
      this.value = value;
      this.category = category;
    }

    private static KeywordCategory categorizeGcmdKeyword(String inNamespace, String inValue) {
      var namespace = Optional.ofNullable(inNamespace).map(String::toLowerCase).orElse("");
      var value = Optional.ofNullable(inValue).map(String::toLowerCase).orElse("");
      if (namespace.contains("science")) {
        if (value.startsWith("earth science services")) {
          return KeywordCategory.gcmdScienceServices;
        } else if (value.startsWith("earth science")) {
          return KeywordCategory.gcmdScience;
        }
      } else if (namespace.contains("location") || namespace.contains("place")) {
        return KeywordCategory.gcmdLocations;
      } else if (namespace.contains("platform")) {
        return KeywordCategory.gcmdPlatforms;
      } else if (namespace.contains("instrument")) {
        return KeywordCategory.gcmdInstruments;
      } else if (namespace.contains("data center")) {
        return KeywordCategory.gcmdDataCenters;
      } else if (namespace.contains("horizontal data resolution")) {
        return KeywordCategory.gcmdHorizontalResolution;
      } else if (namespace.contains("vertical data resolution")) {
        return KeywordCategory.gcmdVerticalResolution;
      } else if (namespace.contains("temporal data resolution")) {
        return KeywordCategory.gcmdTemporalResolution;
      } else if (namespace.contains("project")) {
        return KeywordCategory.gcmdProjects;
      }
      return KeywordCategory.other;
    }
  }

  private static SingleKeyword normalizeKeyword(SingleKeyword keyword) {
    Function<String, String> normalizer = keyword.category.normalizer;
    return new SingleKeyword(keyword.namespace, normalizer.apply(keyword.value), keyword.category); // preserve input category
  }

  static private Stream<SingleKeyword> tokenizeKeyword(SingleKeyword keyword) {
    Function<String, Stream<String>> tokenizer = keyword.category.tokenizer;
    return tokenizer.apply(keyword.value)
        .map(s -> new SingleKeyword(keyword.namespace, s, keyword.category)); // preserve input category
  }

  private static String normalizeHierarchyKeyword(String text) {
    var cleanText = cleanInternalKeywordWhitespace(text);
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace("Earth Science > ", "").replace("Earth Science Services > ", "");
  }

  private static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    var cleanText = cleanInternalKeywordWhitespace(text);
    var elements = Arrays.asList(cleanText.split(" > "));
    String longName = elements.get(elements.size() - 1);
    if (longName.equals(longName.toUpperCase()) || longName.equals(longName.toLowerCase())) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters);
      elements.set(elements.size() - 1, longName);
    }
    return String.join(" > ", elements);
  }

  private static String normalizePlainKeyword(String text) {
    return WordUtils.capitalizeFully(text, capitalizingDelimiters);
  }

  private static final char[] capitalizingDelimiters = new char[]{' ', '/', '.', '(', '-', '_'};

  private static String cleanInternalKeywordWhitespace(String text) {
    return text.replaceAll("\\s+", " ");
  }

  private static Stream<String> tokenizeHierarchyKeyword(String text) {
    var result = new ArrayList<String>();
    var i = text.length();
    while (i > 0) {
      text = text.substring(0, i).trim();
      result.add(text);
      i = text.lastIndexOf('>', i);
    }
    return result.stream();
  }

}
