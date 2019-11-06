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
      log.info("INVALID RECORD [ $id ]. VALIDATION FAILURES:  $details ");
      return nullableMap("title", "Invalid record",
          "detail", String.join(", ", errors),
          "valid", false);
    }
    else {
      return nullableMap("valid", true);
    }
  }

  public static Map reformatMessageForSearch(ParsedRecord record) {
    Discovery discovery = record.getDiscovery();
    Analysis analysis = record.getAnalysis();

    Map discoveryMap = AvroUtils.avroToMap(discovery, true);

    // Records validated before getting to this point to no null record.type possible (always granule or collection)
    discoveryMap.put("type", record.getType() == RecordType.granule ?
        ElasticsearchConfig.TYPE_GRANULE : ElasticsearchConfig.TYPE_COLLECTION);

    // create GCMD keywords
    Map gcmdKeywords = createGcmdKeyword(discovery);
    discoveryMap.putAll(gcmdKeywords);

    // create contacts ,creators and publishers
    Map<String, Set> partyData = parseResponsibleParties(discovery.getResponsibleParties());
    discoveryMap.putAll(partyData);

    // update temporal Bounding
    var temporalData = readyDatesForSearch(discovery.getTemporalBounding(), analysis.getTemporalBounding());
    discoveryMap.put("temporalBounding", temporalData);

    // drop fields
    discoveryMap.remove("responsibleParties");

    List<Service> services = discovery.getServices();
    discoveryMap.remove("services");
    discoveryMap.put("serviceLinks", createServices(services));

    // create data format name list for this record
    discoveryMap.put("dataFormat", createDataFormat(discovery));

    // create protocol list (from links and service links of this record)
    // https://github.com/OSGeo/Cat-Interop/blob/master/LinkPropertyLookupTable.csv
    discoveryMap.put("linkProtocol", createLinkProtocol(discovery));
    discoveryMap.put("serviceLinkProtocol", createServiceLinkProtocol(discovery));

    discoveryMap.put("services", "");
    return discoveryMap;
  }

  static List<Link> getLinksForService(Service service) {
    return Optional.ofNullable(service)
        .map(Service::getOperations)
        .map(it -> {
          it.sort(Link::compareTo);
          return it;
        })
        .orElse(List.of());
  }

  static List<Map> createServices(List<Service> services) {
    return Optional.ofNullable(services)
        .orElse(Collections.emptyList())
        .stream()
        .map(service -> nullableMap(
            "title", service.getTitle(),
            "alternateTitle", service.getAlternateTitle(),
            "description", service.getDescription(),
            "links", AvroUtils.avroCollectionToList(new ArrayList<>(getLinksForService(service)), true)))
        .collect(Collectors.toList());
  }

  // create data format names
  static Set<String> createDataFormat(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getDataFormats)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(dataFormat -> {
          // normalize (clean whitespace/trim/uppercase/nullify empty string) contents of DataFormat to check for uniqueness
          String dataFormatKeyword = normalizeHierarchicalDataFormat(dataFormat);
          return dataFormatKeyword != null && !dataFormatKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(dataFormatKeyword).stream() :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  // create link protocols
  static Set<String> createLinkProtocol(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getLinks)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(link -> {
          var linkProtocolKeyword = normalizeLinkProtocol(link);
          return linkProtocolKeyword != null && !linkProtocolKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(linkProtocolKeyword).stream() :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  static Set<String> createServiceLinkProtocol(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getServices)
        .orElse(Collections.emptyList())
        .stream()
        .flatMap(service -> getLinksForService(service).stream())
        .flatMap(serviceLink -> {
          var serviceLinkProtocolKeyword = normalizeLinkProtocol(serviceLink);
          return serviceLinkProtocolKeyword != null && !serviceLinkProtocolKeyword.isEmpty() ?
              tokenizeHierarchyKeyword(serviceLinkProtocolKeyword).stream() :
              Stream.empty();
        })
        .collect(Collectors.toSet());
  }

  private enum KeywordCategory {
    gcmdScience("gcmdScience", true, false, true),
    gcmdScienceServices("gcmdScienceServices", true, false, true),
    gcmdLocations("gcmdLocations", true, false, true),
    gcmdPlatforms("gcmdPlatforms", false, true, false),
    gcmdInstruments("gcmdInstruments", false, true, false),
    gcmdProjects("gcmdProjects", false, true, false),
    gcmdHorizontalResolution("gcmdHorizontalResolution", false, false, false),
    gcmdVerticalResolution("gcmdVerticalResolution", false, false, false),
    gcmdTemporalResolution("gcmdTemporalResolution", false, false, false),
    gcmdDataCenters("gcmdDataCenters", false, true, false),
    other("other", false, false, false);

    final String label;
    final boolean hierarchical;
    final boolean shortName;
    final boolean tokenize;

    KeywordCategory(String label, boolean hierarchical, boolean shortName, boolean tokenize) {
      this.label = label;
      this.hierarchical = hierarchical;
      this.shortName = shortName;
      this.tokenize = tokenize;
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
    Function<String, String> normalizer =
        keyword.category.hierarchical ? IndexingHelpers::normalizeHierarchyKeyword :
            keyword.category.shortName ? IndexingHelpers::normalizeNonHierarchicalKeyword :
                IndexingHelpers::normalizePlainKeyword;
    return new SingleKeyword(keyword.namespace, normalizer.apply(keyword.value), keyword.category);
  }

  static private Stream<SingleKeyword> tokenizeKeyword(SingleKeyword keyword) {
    if (keyword.category.tokenize) {
      return tokenizeHierarchyKeyword(keyword.value)
          .stream()
          .map(s -> new SingleKeyword(keyword.namespace, s, keyword.category)); // preserve input category
    }
    return Stream.of(keyword);
  }

  // TODO - return Set<String>
  static Map<String, Set<Object>> createGcmdKeyword(Discovery discovery) {
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
        .filter(group -> !group.getNamespace().equals("NCEI ACCESSION NUMBER"))
        .flatMap(group -> group.getValues().stream().map(value -> new SingleKeyword(group.getNamespace(), value)))
        .filter(keyword -> keyword.category != KeywordCategory.other)
        .map(IndexingHelpers::normalizeKeyword)
        .flatMap(IndexingHelpers::tokenizeKeyword)
        .collect(Collectors.groupingBy(
            keyword -> keyword.category.label, // group by the category label
            Collectors.mapping(keyword -> keyword.value, Collectors.<Object>toSet()))); // map the SingleKeywords to their values then collect them in a Set

    groupedKeywords.put("keywords", groupsMinusAccessions); // TODO - keywords should end up being a Set<String> of all keyword values from all categories
    groupedKeywords.put("accessionValues", Collections.emptySet()); // FIXME this needs to be in place until we can use ES6 ignore_missing flags
    for (KeywordCategory category : KeywordCategory.values()) {
      if (!groupedKeywords.containsKey(category.label)) {
        groupedKeywords.put(category.label, Collections.emptySet());
      }
    }
    return groupedKeywords;
  }

  /*
  Create contacts, creators and publishers from responsibleParties
  */
  static Map<String, String> parseParty(ResponsibleParty party) {
    if (party == null) {
      return null;
    }
    return nullableMap(
        "individualName", party.getIndividualName(),
        "organizationName", party.getOrganizationName(),
        "positionName", party.getPositionName(),
        "role", party.getRole(),
        "email", party.getEmail(),
        "phone", party.getPhone());
  }

  static Map<String, Set> parseResponsibleParties(List<ResponsibleParty> responsibleParties) {
    Set contacts = new HashSet<String>();
    Set contactRoles = Set.of("pointOfContact", "distributor");
    Set creators = new HashSet<String>();
    Set creatorRoles = Set.of("resourceProvider", "originator", "principalInvestigator", "author", "collaborator", "coAuthor");
    Set publishers = new HashSet<String>();
    Set publisherRoles = Set.of("publisher");

    Optional.ofNullable(responsibleParties).orElse(Collections.emptyList()).forEach(party -> {
      var parsedParty = parseParty(party);
      log.debug("parsedParty: " + parsedParty);
      String role = parsedParty.getOrDefault("role", null);
      if (contactRoles.contains(role)) {
        contacts.add(parsedParty);
      }
      else if (creatorRoles.contains(role)) {
        creators.add(parsedParty);
      }
      else if (publisherRoles.contains(role)) {
        publishers.add(parsedParty);
      }
    });
    return nullableMap("contacts", contacts, "creators", creators, "publishers", publishers);
  }

  static Map readyDatesForSearch(TemporalBounding bounding, TemporalBoundingAnalysis analysis) {
    String beginDate, endDate;
    Long year;

    // If bounding is actually an instant, set search fields accordingly
    if (analysis.getRangeDescriptor() == TimeRangeDescriptor.INSTANT) {
      beginDate = analysis.getInstantUtcDateTimeString();
      year = parseYear(beginDate);

      // Add time and/or date to endDate based on precision
      var precision = analysis.getInstantPrecision();
      if (precision == ChronoUnit.DAYS.toString()) {
        // End of day
        endDate = bounding.getInstant() + "T23:59:59Z";
      }
      else if (precision == ChronoUnit.YEARS.toString()) {
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
      return nullableMap("beginDate", beginDate, "beginYear", year, "endDate", endDate, "endYear", year);
    }
    else {
      // If dates exist and are validSearchFormat (only false here if paleo, since we filtered out bad data earlier),
      // use value from analysis block where dates are UTC datetime normalized
      return nullableMap(
          "beginDate", analysis.getBeginDescriptor() == VALID && analysis.getBeginIndexable() ? analysis.getBeginUtcDateTimeString() : null,
          "endDate", analysis.getEndDescriptor() == VALID && analysis.getEndIndexable() ? analysis.getEndUtcDateTimeString() : null,
          "beginYear", parseYear(analysis.getBeginUtcDateTimeString()),
          "endYear", parseYear(analysis.getEndUtcDateTimeString())
      );
    }

  }

  static Long parseYear(String utcDateTime) {
    if (StringUtils.isBlank(utcDateTime)) {
      return null;
    }
    else {
      // Watch out for BCE years
      return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)));
    }

  }

  // helper functions
  static String normalizeHierarchicalDataFormat(DataFormat dataFormat) {
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

  static String normalizeLinkProtocol(Link link) {
    return Optional.ofNullable(link)
        .map(Link::getLinkProtocol)
        .map(it -> cleanInternalKeywordWhitespace(it).trim().toUpperCase())
        .orElse(null);
  }

  static String normalizeHierarchyKeyword(String text) {
    var cleanText = cleanInternalKeywordWhitespace(text);
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace("Earth Science > ", "").replace("Earth Science Services > ", "");
  }

  static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    var cleanText = cleanInternalKeywordWhitespace(text);
    var elements = Arrays.asList(cleanText.split(" > "));
    String longName = elements.get(elements.size() - 1);
    if (longName == longName.toUpperCase() || longName == longName.toLowerCase()) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters);
      elements.set(elements.size() - 1, longName);
    }
    return String.join(" > ", elements);
  }

  static String normalizePlainKeyword(String text) {
    return WordUtils.capitalizeFully(text, capitalizingDelimiters);
  }

  private static final char[] capitalizingDelimiters = new char[]{' ', '/', '.', '(', '-', '_'};

  static String cleanInternalKeywordWhitespace(String text) {
    return text.replaceAll("\\s+", " ");
  }

  static List<String> tokenizeHierarchyKeyword(String text) {
    var result = new ArrayList<String>();
    var i = text.length();
    while (i > 0) {
      text = text.substring(0, i).trim();
      result.add(text);
      i = text.lastIndexOf('>', i);
    }
    return result;
  }

  private static Map nullableMap(Object... args) {
    var result = new HashMap<>();
    var i = 0;
    while (args.length - i >= 2) {
      result.put(args[i], args[i+1]);
      i += 2;
    }
    return result;
  }
}
