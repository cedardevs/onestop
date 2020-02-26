package org.cedar.onestop.indexer.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.cedar.schemas.avro.psi.*;
import org.cedar.schemas.avro.util.AvroUtils;
import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cedar.schemas.avro.psi.ValidDescriptor.*;
import static org.elasticsearch.action.DocWriteRequest.OpType.*;

public class IndexingHelpers {
  static final private Logger log = LoggerFactory.getLogger(IndexingHelpers.class);
  static final private String VALIDATION_ERROR_TITLE = "Invalid for search indexing";

  ////////////////////////////
  // Validation             //
  ////////////////////////////
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

  ////////////////////////////
  // Transformation         //
  ////////////////////////////
  public static List<DocWriteRequest<?>> mapRecordToRequests(IndexingInput input) {
    if (input == null) { return null; }
    try {
      var record = ValueAndTimestamp.getValueOrNull(input.getValue());
      var operation = (isTombstone(record) || isPrivate(record)) ? DELETE : INDEX;
      var indices = input.getIndexingConfig().getTargetIndices(input.getTopic(), operation);
      return indices.stream()
          .map(indexName -> buildWriteRequest(indexName, operation, input))
          .collect(Collectors.toList());
    } catch (ElasticsearchGenerationException e) {
      log.error("failed to serialize record with key [" + input.getKey() + "] to json", e);
      return new ArrayList<>();
    }
  }

  public static DocWriteRequest<?> buildWriteRequest(String indexName, DocWriteRequest.OpType opType, IndexingInput input) {
    if (opType == DELETE) {
      return new DeleteRequest(indexName).id(input.getKey());
    }

    var targetFields = input.getEsConfig().indexedProperties(indexName).keySet();
    var formattedRecord = reformatMessageForSearch(input.getValue().value(), targetFields);
    formattedRecord.put("stagedDate", input.getValue().timestamp());

    if (opType == INDEX || opType == CREATE) {
      return new IndexRequest(indexName).opType(opType).id(input.getKey()).source(formattedRecord);
    }
    if (opType == UPDATE) {
      return new UpdateRequest(indexName, input.getKey()).doc(formattedRecord);
    }
    throw new UnsupportedOperationException("unsupported elasticsearch OpType: " + opType);
  }

  public static boolean isTombstone(ParsedRecord value) {
    return value == null;
  }

  public static boolean isPrivate(ParsedRecord value) {
    var optionalPublishing = Optional.of(value).map(ParsedRecord::getPublishing);
    var isPrivate = optionalPublishing.map(Publishing::getIsPrivate).orElse(false);
    var until = optionalPublishing.map(Publishing::getUntil).orElse(null);
    return (until == null || until > System.currentTimeMillis()) ? isPrivate : !isPrivate;
  }

  public static Map<String, Object> reformatMessageForSearch(ParsedRecord record, Set<String> targetFields) {
    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();
    var discoveryMap = AvroUtils.avroToMap(discovery, true);

    // prepare and apply fields that need to be reformatted for search
    discoveryMap.putAll(prepareGcmdKeyword(discovery));
    discoveryMap.putAll(prepareDates(discovery.getTemporalBounding(), analysis.getTemporalBounding()));
    discoveryMap.put("dataFormat", prepareDataFormats(discovery));
    discoveryMap.put("linkProtocol", prepareLinkProtocols(discovery));
    discoveryMap.put("serviceLinks", prepareServiceLinks(discovery));
    discoveryMap.put("serviceLinkProtocol", prepareServiceLinkProtocols(discovery));
    discoveryMap.putAll(prepareResponsibleParties(record));
    discoveryMap.put("internalParentIdentifier", prepareInternalParentIdentifier(record));

    // drop fields not present in target index
    var result = new LinkedHashMap<String, Object>(targetFields.size());
    targetFields.forEach(f -> result.put(f, discoveryMap.get(f)));
    return result;
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

  static Map <String, String> prepareGranuleNameFilterFields(ParsedRecord record) {
    var fileInfo = record.getFileInformation();

    var result = new HashMap<String, String>();
    result.put("titleForFilter", record.getDiscovery().getTitle());
    result.put("fileIdentifierForFilter", record.getDiscovery().getFileIdentifier());
    result.put("filename", fileInfo != null ? fileInfo.getName() : null);
    return result;
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
  private static Map<String, Object> prepareDates(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    String beginDate, endDate;
    Long year;

    // If bounding is actually an instant, set search fields accordingly
    if (analysis.getRangeDescriptor() == TimeRangeDescriptor.INSTANT) {
      beginDate = analysis.getInstantUtcDateTimeString();
      year = parseYear(beginDate);

      // Add time and/or date to endDate based on precision
      var precision = analysis.getInstantPrecision();
      if (precision.equals(ChronoUnit.DAYS.toString())) {
        // End of day
        endDate = bounding.getInstant() + "T23:59:59Z";
      } else if (precision.equals(ChronoUnit.YEARS.toString())) {
        if (!analysis.getInstantIndexable()) {
          // Paleo date, so only return year value (null out dates)
          beginDate = null;
          endDate = null;
        } else {
          // Last day of year + end of day
          endDate = bounding.getInstant() + "-12-31T23:59:59Z";
        }
      } else {
        // Precision is NANOS so use instant value as-is
        endDate = beginDate;
      }
      var result = new HashMap<String, Object>();
      result.put("beginDate", beginDate);
      result.put("beginYear", year);
      result.put("endDate", endDate);
      result.put("endYear", year);
      return result;
    } else {
      // If dates exist and are validSearchFormat (only false here if paleo, since we filtered out bad data earlier),
      // use value from analysis block where dates are UTC datetime normalized
      var result = new HashMap<String, Object>();
      result.put("beginDate", analysis.getBeginDescriptor() == VALID && analysis.getBeginIndexable() ? analysis.getBeginUtcDateTimeString() : null);
      result.put("endDate", analysis.getEndDescriptor() == VALID && analysis.getEndIndexable() ? analysis.getEndUtcDateTimeString() : null);
      result.put("beginYear", parseYear(analysis.getBeginUtcDateTimeString()));
      result.put("endYear", parseYear(analysis.getEndUtcDateTimeString()));
      return result;
    }
  }

  private static Long parseYear(String utcDateTime) {
    if (StringUtils.isBlank(utcDateTime)) {
      return null;
    } else {
      // Watch out for BCE years
      return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)));
    }
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
        .map(IndexingHelpers::normalizeKeyword)
        .flatMap(IndexingHelpers::tokenizeKeyword)
        .filter(k -> k.category != KeywordCategory.other)
        .collect(Collectors.groupingBy(
            keyword -> keyword.category.name(), // group by the category label
            Collectors.mapping(keyword -> keyword.value, Collectors.toSet()))); // map the SingleKeywords to their values then collect them in a Set
    groupedKeywords.put("keywords", allKeywords);
    return groupedKeywords;
  }

  private enum KeywordCategory {
    gcmdScience(IndexingHelpers::normalizeHierarchyKeyword, IndexingHelpers::tokenizeHierarchyKeyword),
    gcmdScienceServices(IndexingHelpers::normalizeHierarchyKeyword, IndexingHelpers::tokenizeHierarchyKeyword),
    gcmdLocations(IndexingHelpers::normalizeHierarchyKeyword, IndexingHelpers::tokenizeHierarchyKeyword),
    gcmdPlatforms(IndexingHelpers::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdInstruments(IndexingHelpers::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdProjects(IndexingHelpers::normalizeNonHierarchicalKeyword, Stream::of),
    gcmdHorizontalResolution(IndexingHelpers::normalizePlainKeyword, Stream::of),
    gcmdVerticalResolution(IndexingHelpers::normalizePlainKeyword, Stream::of),
    gcmdTemporalResolution(IndexingHelpers::normalizePlainKeyword, Stream::of),
    gcmdDataCenters(IndexingHelpers::normalizeNonHierarchicalKeyword, Stream::of),
    other(IndexingHelpers::normalizePlainKeyword, Stream::of);

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
