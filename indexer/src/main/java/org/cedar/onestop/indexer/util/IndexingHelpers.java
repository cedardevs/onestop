package org.cedar.onestop.indexer.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.cedar.onestop.elastic.common.ElasticsearchConfig;
import org.cedar.schemas.avro.psi.*;
import org.cedar.schemas.avro.util.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cedar.schemas.avro.psi.ValidDescriptor.*;

public class IndexingHelpers {
  static final private Logger log = LoggerFactory.getLogger(IndexingHelpers.class);

  public static Map validateMessage(String id, ParsedRecord messageMap) {
    // FIXME Improve testability of failures by creating an Enum for invalid messages
    List<String> errors = new ArrayList<>();

    var discovery = messageMap != null ? messageMap.getDiscovery() : null;
    if (discovery == null || discovery == Discovery.newBuilder().build()) {
      errors.add("Discovery metadata missing. No metadata to load into OneStop.");
    }
    else {
      var analysis = messageMap.getAnalysis();
      if (analysis == null || analysis == Analysis.newBuilder().build()) {
        errors.add("Analysis metadata missing. Cannot verify metadata quality for OneStop.");
      }
      else {
        var titles = analysis.getTitles();
        var identification = analysis.getIdentification();
        var temporal = analysis.getTemporalBounding();
        var spatial = analysis.getSpatialBounding();

        if (identification != null && !identification.getFileIdentifierExists() && !identification.getDoiExists()) {
          errors.add("Missing identifier - record contains neither a fileIdentifier nor a DOI");
        }
        if (messageMap.getType() == null || (identification != null && !identification.getMatchesIdentifiers())) {
          errors.add("Metadata type error -- hierarchyLevelName is 'granule' but no parentIdentifier provided OR type unknown.");
        }
        if (!titles.getTitleExists()) {
          errors.add("Missing title");
        }
        if (temporal.getBeginDescriptor() == INVALID) {
          errors.add("Invalid beginDate");
        }
        if (temporal.getEndDescriptor() == INVALID) {
          errors.add("Invalid endDate");
        }
        if (temporal.getBeginDescriptor() != UNDEFINED && temporal.getEndDescriptor() != UNDEFINED && temporal.getInstantDescriptor() == INVALID) {
          errors.add("Invalid instant-only date");
        }
        if (spatial.getSpatialBoundingExists() && !spatial.getIsValid()) {
          errors.add("Invalid geoJSON for spatial bounding");
        }
      }
    }

    if (errors.size() > 0) {
      log.info("INVALID RECORD [ " + id + " ]. VALIDATION FAILURES: " + errors);
      var result = new HashMap<>();
      result.put("title", "Invalid record");
      result.put("detail", String.join(", ", errors));
      result.put("valid", false);
      return result;
    }
    else {
      var result = new HashMap<>();
      result.put("valid", true);
      return result;
    }
  }

  public static Map<String, Object> reformatMessageForSearch(ParsedRecord record) {
    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();
    var discoveryMap = AvroUtils.avroToMap(discovery, true);

    // Records validated before getting to this point to no null record.type possible (always granule or collection)
    discoveryMap.put("type", record.getType() == RecordType.granule ?
        ElasticsearchConfig.TYPE_GRANULE : ElasticsearchConfig.TYPE_COLLECTION);

    // prepare and apply fields that need to be reformatted for search
    discoveryMap.putAll(prepareGcmdKeyword(discovery));
    discoveryMap.put("temporalBounding", prepareDates(discovery.getTemporalBounding(), analysis.getTemporalBounding()));
    discoveryMap.put("dataFormat", prepareDataFormats(discovery));
    discoveryMap.put("linkProtocol", prepareLinkProtocols(discovery));
    discoveryMap.put("serviceLinkProtocol", prepareServiceLinkProtocols(discovery));

    // split responsibleParties into contacts, creators and publishers
    discoveryMap.putAll(prepareResponsibleParties(discovery.getResponsibleParties()));
    discoveryMap.remove("responsibleParties");

    // replace "services" with prepared "serviceLinks"
    discoveryMap.put("serviceLinks", prepareServices(discovery.getServices()));
    discoveryMap.put("services", "");

    return discoveryMap;
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  static List<Map> prepareServices(List<Service> services) {
    return Optional.ofNullable(services)
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
  static Set<String> prepareServiceLinkProtocols(Discovery discovery) {
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
  static Set<String> prepareLinkProtocols(Discovery discovery) {
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
  static Set<String> prepareDataFormats(Discovery discovery) {
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
    }
    else if (version == null) {
      return name;
    }
    else {
      return name + " > " + version;
    }
  }

  ////////////////////////////
  // Responsible Parties    //
  ////////////////////////////
  static Map<String, Set<Map>> prepareResponsibleParties(List<ResponsibleParty> responsibleParties) {
    Map<String, Set<Map>> groupedParties = Optional.ofNullable(responsibleParties)
        .orElse(Collections.emptyList())
        .stream()
        .collect(Collectors.groupingBy(
            IndexingHelpers.categorizeParty,
            Collectors.mapping(IndexingHelpers::parseParty, Collectors.toSet())));
    groupedParties.remove("other");
    return groupedParties;
  }

  private static Map<String, String> parseParty(ResponsibleParty party) {
    if (party == null) {
      return null;
    }
    var result = new HashMap<String, String>();
    result.put("individualName", party.getIndividualName());
    result.put("organizationName", party.getOrganizationName());
    result.put("positionName", party.getPositionName());
    result.put("role", party.getRole());
    result.put("email", party.getEmail());
    result.put("phone", party.getPhone());
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
  static Map prepareDates(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
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
      }
      else if (precision.equals(ChronoUnit.YEARS.toString())) {
        if (!analysis.getInstantIndexable()) {
          // Paleo date, so only return year value (null out dates)
          beginDate = null;
          endDate = null;
        }
        else {
          // Last day of year + end of day
          endDate = bounding.getInstant() + "-12-31T23:59:59Z";
        }
      }
      else {
        // Precision is NANOS so use instant value as-is
        endDate = beginDate;
      }
      var result = new HashMap<>();
      result.put("beginDate", beginDate);
      result.put("beginYear", year);
      result.put("endDate", endDate);
      result.put("endYear", year);
      return result;
    }
    else {
      // If dates exist and are validSearchFormat (only false here if paleo, since we filtered out bad data earlier),
      // use value from analysis block where dates are UTC datetime normalized
      var result = new HashMap<>();
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
    }
    else {
      // Watch out for BCE years
      return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)));
    }
  }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  // TODO - return Set<String>
  static Map<String, Set<Object>> prepareGcmdKeyword(Discovery discovery) {
    List<KeywordsElement> keywordGroups = Optional.ofNullable(discovery)
        .map(Discovery::getKeywords)
        .orElse(Collections.emptyList());
    var groupsMinusAccessions = keywordGroups
        .stream()
        .filter(group -> !group.getNamespace().equals("NCEI ACCESSION NUMBER"))
        .map(AvroUtils::avroToMap)
        .collect(Collectors.<Object>toSet());
    var groupedKeywords = keywordGroups
        .stream()
        .filter(g -> !g.getNamespace().equals("NCEI ACCESSION NUMBER"))
        .flatMap(g -> g.getValues().stream().map(value -> new SingleKeyword(g.getNamespace(), value)))
        .filter(k -> k.category != KeywordCategory.other)
        .map(IndexingHelpers::normalizeKeyword)
        .flatMap(IndexingHelpers::tokenizeKeyword)
        .collect(Collectors.groupingBy(
            keyword -> keyword.category.name(), // group by the category label
            Collectors.mapping(keyword -> keyword.value, Collectors.<Object>toSet()))); // map the SingleKeywords to their values then collect them in a Set

    groupedKeywords.put("keywords", groupsMinusAccessions); // TODO - keywords should end up being a Set<String> of all keyword values from all categories

    // FIXME this needs to be in place until we can use ES6 ignore_missing flags
    groupedKeywords.put("accessionValues", Collections.emptySet());
    for (KeywordCategory category : KeywordCategory.values()) {
      if (!groupedKeywords.containsKey(category.name())) {
        groupedKeywords.put(category.name(), Collections.emptySet());
      }
    }

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
        }
        else if (value.startsWith("earth science")) {
          return KeywordCategory.gcmdScience;
        }
      }
      else if (namespace.contains("location") || namespace.contains("place")) {
        return KeywordCategory.gcmdLocations;
      }
      else if (namespace.contains("platform")) {
        return KeywordCategory.gcmdPlatforms;
      }
      else if (namespace.contains("instrument")) {
        return KeywordCategory.gcmdInstruments;
      }
      else if (namespace.contains("data center")) {
        return KeywordCategory.gcmdDataCenters;
      }
      else if (namespace.contains("horizontal data resolution")) {
        return KeywordCategory.gcmdHorizontalResolution;
      }
      else if (namespace.contains("vertical data resolution")) {
        return KeywordCategory.gcmdVerticalResolution;
      }
      else if (namespace.contains("temporal data resolution")) {
        return KeywordCategory.gcmdTemporalResolution;
      }
      else if (namespace.contains("project")) {
        return KeywordCategory.gcmdProjects;
      }
      return KeywordCategory.other;
    }
  }

  static private SingleKeyword normalizeKeyword(SingleKeyword keyword) {
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
