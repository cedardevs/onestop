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

import org.cedar.onestop.mapping.analysis.AnalysisErrorGranule;
import org.cedar.onestop.mapping.analysis.AnalysisErrorCollection;
import org.cedar.onestop.mapping.analysis.Identification;
import org.cedar.onestop.mapping.analysis.DataAccess;
import org.cedar.onestop.mapping.analysis.Description;
import org.cedar.onestop.mapping.analysis.SpatialBounding;
import org.cedar.onestop.mapping.analysis.Thumbnail;
import org.cedar.onestop.mapping.analysis.Titles;
import org.cedar.onestop.mapping.analysis.Error;
// import org.cedar.onestop.mapping.TemporalBounding;

import static org.cedar.schemas.avro.psi.ValidDescriptor.UNDEFINED;
import static org.cedar.schemas.avro.psi.ValidDescriptor.VALID;

/**
 * This class contains utilities for transforming the contents of the Avro (schemas) records into the appropriate
 * corresponding Elasticsearch mapping format.
 */
public class TransformationUtils {
  static final private Logger log = LoggerFactory.getLogger(TransformationUtils.class);

  ///////////////////////////////////////////////////////////////////////////////
  //                     Convert to Indexing Message                           //
  ///////////////////////////////////////////////////////////////////////////////
  public static org.cedar.onestop.mapping.analysis.TemporalBounding convertTemporalAnalysis(TemporalBoundingAnalysis temporalAnalysis) {
    // TODO fully resolved import path due to psi & es mapping naming conflict - could resolve with smaller util files
    org.cedar.onestop.mapping.analysis.TemporalBounding converted = new org.cedar.onestop.mapping.analysis.TemporalBounding();
    if (temporalAnalysis == null) {
      return converted;
    }
    if (temporalAnalysis.getRangeDescriptor() != null) {
      converted.setRangeDescriptor(temporalAnalysis.getRangeDescriptor().toString());
    }
    if (temporalAnalysis.getBeginDescriptor() != null) {
      converted.setBeginDescriptor(temporalAnalysis.getBeginDescriptor().toString());
    }
    if (temporalAnalysis.getEndDescriptor() != null) {
      converted.setEndDescriptor(temporalAnalysis.getEndDescriptor().toString());
    }
    if (temporalAnalysis.getInstantDescriptor() != null) {
      converted.setInstantDescriptor(temporalAnalysis.getInstantDescriptor().toString());
    }
    return converted
    .withBeginIndexable(temporalAnalysis.getBeginIndexable())
    .withBeginPrecision(temporalAnalysis.getBeginPrecision())
    .withBeginUtcDateTimeString(temporalAnalysis.getBeginUtcDateTimeString())
    .withBeginZoneSpecified(temporalAnalysis.getBeginZoneSpecified())
    .withEndIndexable(temporalAnalysis.getEndIndexable())
    .withEndPrecision(temporalAnalysis.getEndPrecision())
    .withEndUtcDateTimeString(temporalAnalysis.getEndUtcDateTimeString())
    .withEndZoneSpecified(temporalAnalysis.getEndZoneSpecified())
    .withInstantIndexable(temporalAnalysis.getInstantIndexable())
    .withInstantPrecision(temporalAnalysis.getInstantPrecision())
    .withInstantUtcDateTimeString(temporalAnalysis.getInstantUtcDateTimeString())
    .withInstantZoneSpecified(temporalAnalysis.getInstantZoneSpecified());
  }

  public static Identification convertIdentificationAnalysis(IdentificationAnalysis identificationAnalysis) {
    Identification converted = new Identification();
    if (identificationAnalysis == null) {
      return converted;
    }
    return converted
    .withDoiExists(identificationAnalysis.getDoiExists())
    .withDoiString(identificationAnalysis.getDoiString())
    .withFileIdentifierExists(identificationAnalysis.getFileIdentifierExists())
    .withFileIdentifierString(identificationAnalysis.getFileIdentifierString())
    .withHierarchyLevelNameExists(identificationAnalysis.getHierarchyLevelNameExists())
    .withParentIdentifierExists(identificationAnalysis.getParentIdentifierExists());
     // TODO .withIsGranule() my api docs for analysis might be out of date, not sure where this data is exactly
  }

  public static DataAccess convertDataAccess(DataAccessAnalysis dataAccess) {
    DataAccess converted = new DataAccess();
    if (dataAccess == null) {
      return converted;
    }
    return converted.withDataAccessExists(dataAccess.getDataAccessExists());
  }

  public static Description convertDescription(DescriptionAnalysis description) {
    Description converted = new Description();
    if (description == null) {
      return converted;
    }
    return converted
    .withDescriptionCharacters(description.getDescriptionCharacters().shortValue()) // TODO extra logging around this conversion?
    .withDescriptionExists(description.getDescriptionExists());
  }

  public static Thumbnail convertThumbnail(ThumbnailAnalysis thumbnail) {
    Thumbnail converted = new Thumbnail();
    if (thumbnail == null) {
      return converted;
    }
    return converted.withThumbnailExists(thumbnail.getThumbnailExists());
  }

  public static Titles convertTitles(TitleAnalysis titles) {
    Titles converted = new Titles();
    if (titles == null) {
      return converted;
    }
    return converted.withTitleExists(titles.getTitleExists())
      .withTitleCharacters(titles.getTitleCharacters().shortValue())// TODO extra logging around this conversion?
      .withAlternateTitleExists(titles.getAlternateTitleExists())
      .withAlternateTitleCharacters(titles.getAlternateTitleCharacters().shortValue());// TODO extra logging around this conversion?
  }

  public static SpatialBounding convertSpatialAnalysis(SpatialBoundingAnalysis spatialAnalysis) {
    SpatialBounding converted = new SpatialBounding();
    if (spatialAnalysis == null) {
      return converted;
    }
    return converted
      .withIsValid(spatialAnalysis.getIsValid())
      .withSpatialBoundingExists(spatialAnalysis.getSpatialBoundingExists())
      .withValidationError(spatialAnalysis.getValidationError());
  }

  public static AnalysisErrorCollection reformatCollectionForAnalysis(long timestamp, ParsedRecord record) {

    Analysis analysis = record.getAnalysis();


    // TODO switch based on type and/or split into separate methods
      // .withStagedDate(ZonedDateTime.now()) // TODO FIXME DO NOT LEAVE THIS WITHOUT PASSING ALONG TIMESTAMP CORRECTLY:!>!>!>!!!!1111!!

    AnalysisErrorCollection message = new AnalysisErrorCollection().withDataAccess(convertDataAccess(analysis.getDataAccess()))
    .withDescription(convertDescription(analysis.getDescription()))
    .withIdentification(convertIdentificationAnalysis(analysis.getIdentification()))
    .withSpatialBounding(convertSpatialAnalysis(analysis.getSpatialBounding()))
    .withTemporalBounding(
      convertTemporalAnalysis(analysis.getTemporalBounding()))
    .withThumbnail(convertThumbnail(analysis.getThumbnail()))
    .withTitles(convertTitles(analysis.getTitles()));
    var errorsList = record.getErrors().stream()
        .map(e -> new Error().withTitle(e.getTitle()).withDetail(e.getDetail())) // TODO withSource(??)
        .collect(Collectors.toList());
    message.setErrors(errorsList);



    return message;

    // ObjectMapper mapper = new ObjectMapper();
    // try {
    //   return mapper.writeValueAsString(message);
    // } catch (JsonProcessingException e) {
    //   // TODO DECIDE HOW TO HANDLE THIS ERROR FIXME DO NOT IGNORE THIS SERIOUSLY DO NOT DO IT
    //   System.out.println("UNABLE TO MAP OBJECT");
    //   System.out.println(e);
    //   System.out.println("returning null!!");
    // }
    // return null;



    // var analysis = record.getAnalysis();
    // var errors = record.getErrors();
    // var analysisMap = AvroUtils.avroToMap(analysis, true);
    // var message = new HashMap<String, Object>();
    //
    // fields.forEach(field -> {
    //   message.put(field, analysisMap.get(field));
    // });
    // if (fields.contains("internalParentIdentifier")) {
    //   analysisMap.put("internalParentIdentifier", prepareInternalParentIdentifier(record));
    // }
    // var errorsList = errors.stream()
    //     .map(e -> AvroUtils.avroToMap(e))
    //     .collect(Collectors.toList());
    //
    // if (fields.contains("errors")) {
    //   message.put("errors", errorsList);
    // }
    //
    // if (fields.contains("temporalBounding")) {
    //   message.put("temporalBounding", prepareTemporalBounding(analysis.getTemporalBounding()));
    // }
    // if (fields.contains("identification")) {
    //   message.put("identification", prepareIdentification(analysis.getIdentification(), recordType));
    // }
    //
    // return message;
  }

  public static Map<String, Object> prepareIdentification(IdentificationAnalysis identification, RecordType recordType) {
    var result = new HashMap<String, Object>();
    var analysis = AvroUtils.avroToMap(identification); // currently using map because couldn't get it working with IdentificationAnalysis object. Worth revisiting at some point.

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

    if (analysis == null) {
      return result;
    }

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
    var result = new HashMap<String, Object>();

    if (analysis.getInstantIndexable()) {
      // paleo dates are not indexable, so only add beginDate or endDate to the index if instantIndexable
      result.put("beginDate", analysis.getInstantUtcDateTimeString());
      result.put("endDate", analysis.getInstantEndUtcDateTimeString());
    }

    result.put("beginYear", analysis.getInstantYear());
    result.put("beginDayOfYear", analysis.getInstantDayOfYear());
    result.put("beginDayOfMonth", analysis.getInstantDayOfMonth());
    result.put("beginMonth",  analysis.getInstantMonth());

    result.put("endYear", analysis.getInstantYear());
    result.put("endDayOfYear", analysis.getInstantEndDayOfYear());
    result.put("endDayOfMonth", analysis.getInstantEndDayOfMonth());
    result.put("endMonth", analysis.getInstantEndMonth());

    return result;
  }

  private static Map<String, Object> prepareBeginDate(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();

    if (analysis.getBeginDescriptor() == VALID) {
      if (analysis.getBeginIndexable()) {
        result.put("beginDate", analysis.getBeginUtcDateTimeString());
      }

      result.put("beginYear", analysis.getBeginYear());
      result.put("beginDayOfYear", analysis.getBeginDayOfYear());
      result.put("beginDayOfMonth", analysis.getBeginDayOfMonth());
      result.put("beginMonth", analysis.getBeginMonth());
    }
    return result;
  }

  private static Map<String, Object> prepareEndDate(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();

    if (analysis.getEndDescriptor() == VALID) {
      if (analysis.getEndIndexable()) {
        result.put("endDate", analysis.getEndUtcDateTimeString());
      }

      result.put("endYear", analysis.getEndYear());
      result.put("endDayOfYear", analysis.getEndDayOfYear());
      result.put("endDayOfMonth", analysis.getEndDayOfMonth());
      result.put("endMonth", analysis.getEndMonth());
    }
    return result;
  }

  private static Map<String, Object> prepareDates(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    var result = new HashMap<String, Object>();

    // If bounding is actually an instant, set search fields accordingly
    if (analysis.getRangeDescriptor() == TimeRangeDescriptor.INSTANT && analysis.getBeginDescriptor() == UNDEFINED) { // distinguished getting begin and end date that were exactly the same (also described as instant), but in that case need to use prepareBeginDate and prepareEndDate to get data off the correct analysis fields
      return prepareDatesForInstant(bounding, analysis);
    } else {
      result.putAll(prepareBeginDate(bounding, analysis));
      result.putAll(prepareEndDate(bounding, analysis));
    }

    return result;
  }

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
