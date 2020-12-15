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
import org.cedar.onestop.mapping.analysis.CollectionIdentification;
import org.cedar.onestop.mapping.analysis.GranuleIdentification;
import org.cedar.onestop.mapping.analysis.DataAccess;
import org.cedar.onestop.mapping.analysis.Description;
import org.cedar.onestop.mapping.analysis.SpatialBounding;
import org.cedar.onestop.mapping.analysis.Thumbnail;
import org.cedar.onestop.mapping.analysis.Titles;
import org.cedar.onestop.mapping.analysis.Error;
import org.cedar.onestop.mapping.search.SearchObjectWithDates;
import org.cedar.onestop.mapping.search.SearchObjectWithKeywords;
import org.cedar.onestop.mapping.search.SearchObjectWithResponsibleParties;
import org.cedar.onestop.mapping.search.SearchCollection;
import org.cedar.onestop.mapping.search.SearchGranule;
import org.cedar.onestop.mapping.search.SearchFlattenedGranule;
import org.cedar.onestop.mapping.search.ServiceLink;
import org.cedar.onestop.mapping.search.Checksum;

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

  public static CollectionIdentification convertCollectionIdentificationAnalysis(IdentificationAnalysis identificationAnalysis) {
    CollectionIdentification converted = new CollectionIdentification();
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

  public static GranuleIdentification convertGranuleIdentificationAnalysis(IdentificationAnalysis identificationAnalysis) {
    GranuleIdentification converted = new GranuleIdentification();
    if (identificationAnalysis == null) {
      return converted;
    }
    return converted
    .withDoiExists(identificationAnalysis.getDoiExists())
    .withDoiString(identificationAnalysis.getDoiString())
    .withFileIdentifierExists(identificationAnalysis.getFileIdentifierExists())
    .withFileIdentifierString(identificationAnalysis.getFileIdentifierString())
    .withHierarchyLevelNameExists(identificationAnalysis.getHierarchyLevelNameExists())
    .withParentIdentifierExists(identificationAnalysis.getParentIdentifierExists())
    .withParentIdentifierString(identificationAnalysis.getParentIdentifierString());
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

  public static AnalysisErrorGranule reformatGranuleForAnalysis(long timestamp, ParsedRecord record) {

    Analysis analysis = record.getAnalysis();

    AnalysisErrorGranule message = new AnalysisErrorGranule()
      .withInternalParentIdentifier(prepareInternalParentIdentifier(record))
      .withStagedDate(timestamp)
      .withDataAccess(convertDataAccess(analysis.getDataAccess()))
      .withDescription(convertDescription(analysis.getDescription()))
      .withIdentification(convertGranuleIdentificationAnalysis(analysis.getIdentification()))
      .withSpatialBounding(convertSpatialAnalysis(analysis.getSpatialBounding()))
      .withTemporalBounding(
        convertTemporalAnalysis(analysis.getTemporalBounding()))
      .withThumbnail(convertThumbnail(analysis.getThumbnail()))
      .withTitles(convertTitles(analysis.getTitles()));

    var errorsList = record.getErrors().stream()
        .map(e -> new Error()
          .withTitle(e.getTitle())
          .withDetail(e.getDetail())
          .withSource(e.getSource()))
        .collect(Collectors.toList());
    message.setErrors(errorsList);

    return message;
  }

  public static AnalysisErrorCollection reformatCollectionForAnalysis(long timestamp, ParsedRecord record) {

    Analysis analysis = record.getAnalysis();

    AnalysisErrorCollection message = new AnalysisErrorCollection()
      .withStagedDate(timestamp)
      .withDataAccess(convertDataAccess(analysis.getDataAccess()))
      .withDescription(convertDescription(analysis.getDescription()))
      .withIdentification(convertCollectionIdentificationAnalysis(analysis.getIdentification()))
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
  }

  public static SearchGranule reformatGranuleForSearch(long timestamp, ParsedRecord record) {

    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();

    SearchGranule message = new SearchGranule()
    .withStagedDate(timestamp)
    .withParentIdentifier(discovery.getParentIdentifier())
    .withFileIdentifier(discovery.getFileIdentifier())
    .withDoi(discovery.getDoi())
    .withTitle(discovery.getTitle())
    .withDescription(discovery.getDescription())
    .withIsGlobal(discovery.getIsGlobal())
    .withThumbnail(discovery.getThumbnail())
    .withSpatialBounding(prepareSpatialBounding(discovery))
    .withDataFormats(convertDataFormats(discovery))
    .withDataFormat(prepareDataFormats(discovery))
    .withLinkProtocol(prepareLinkProtocols(discovery))
    .withServiceLinkProtocol(prepareServiceLinkProtocols(discovery))
    .withLinks(convertLinks(discovery.getLinks()))
    .withServiceLinks(prepareServiceLinks(discovery))
    .withCiteAsStatements(discovery.getCiteAsStatements())
    .withChecksums(prepareChecksums(record))
    .withInternalParentIdentifier(prepareInternalParentIdentifier(record))
    .withFilename(prepareFilename(record))
    .withFilesize(prepareFilesize(record))
    .withFileFormat(prepareFileFormat(record));
    prepareDates(message, analysis != null ? analysis.getTemporalBounding():null);
    prepareGcmdKeyword(message, discovery);

    return message;
  }

  public static SearchFlattenedGranule reformatFlattenedGranuleForSearch(long timestamp, ParsedRecord record) {
    // TODO add unit tests
    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();

    SearchFlattenedGranule message = new SearchFlattenedGranule()
    .withStagedDate(timestamp)
    .withParentIdentifier(discovery.getParentIdentifier())
    .withFileIdentifier(discovery.getFileIdentifier())
    .withDoi(discovery.getDoi())
    .withTitle(discovery.getTitle())
    .withDescription(discovery.getDescription())
    .withIsGlobal(discovery.getIsGlobal())
    .withThumbnail(discovery.getThumbnail())
    .withSpatialBounding(prepareSpatialBounding(discovery))
    .withDsmmAverage(discovery.getDsmmAverage())
    .withEdition(discovery.getEdition())
    .withOrderingInstructions(discovery.getOrderingInstructions())
    .withAccessFeeStatement(discovery.getAccessFeeStatement())
    .withLegalConstraints(discovery.getLegalConstraints())
    .withUseLimitation(discovery.getUseLimitation())
    .withDataFormats(convertDataFormats(discovery))
    .withDataFormat(prepareDataFormats(discovery))
    .withLinkProtocol(prepareLinkProtocols(discovery))
    .withServiceLinkProtocol(prepareServiceLinkProtocols(discovery))
    .withLinks(convertLinks(discovery.getLinks()))
    .withServiceLinks(prepareServiceLinks(discovery))
    .withCiteAsStatements(discovery.getCiteAsStatements())
    .withChecksums(prepareChecksums(record))
    .withInternalParentIdentifier(prepareInternalParentIdentifier(record))
    .withFilename(prepareFilename(record))
    .withLargerWorks(convertReferences(discovery.getLargerWorks()))
    .withCrossReferences(convertReferences(discovery.getCrossReferences()))
    .withFilesize(prepareFilesize(record))
    .withFileFormat(prepareFileFormat(record));
    prepareDates(message, analysis != null ? analysis.getTemporalBounding():null);
    prepareResponsibleParties(message, record);
    prepareGcmdKeyword(message, discovery);

    return message;
  }

  public static SearchCollection reformatCollectionForSearch(long timestamp, ParsedRecord record) {

    var discovery = record.getDiscovery();
    var analysis = record.getAnalysis();

    SearchCollection message = new SearchCollection()
    .withStagedDate(timestamp)
    .withParentIdentifier(discovery.getParentIdentifier())
    .withFileIdentifier(discovery.getFileIdentifier())
    .withDoi(discovery.getDoi())
    .withTitle(discovery.getTitle())
    .withDescription(discovery.getDescription())
    .withIsGlobal(discovery.getIsGlobal())
    .withThumbnail(discovery.getThumbnail())
    .withSpatialBounding(prepareSpatialBounding(discovery))
    .withDsmmAverage(discovery.getDsmmAverage())
    .withEdition(discovery.getEdition())
    .withOrderingInstructions(discovery.getOrderingInstructions())
    .withAccessFeeStatement(discovery.getAccessFeeStatement())
    .withLegalConstraints(discovery.getLegalConstraints())
    .withUseLimitation(discovery.getUseLimitation())
    .withCiteAsStatements(discovery.getCiteAsStatements())
    .withDataFormats(convertDataFormats(discovery))
    .withDataFormat(prepareDataFormats(discovery))
    .withLinkProtocol(prepareLinkProtocols(discovery))
    .withServiceLinkProtocol(prepareServiceLinkProtocols(discovery))
    .withLinks(convertLinks(discovery.getLinks()))
    .withServiceLinks(prepareServiceLinks(discovery))
    .withLargerWorks(convertReferences(discovery.getLargerWorks()))
    .withCrossReferences(convertReferences(discovery.getCrossReferences()));
    prepareDates(message, analysis != null ? analysis.getTemporalBounding():null);
    prepareResponsibleParties(message, record);
    prepareGcmdKeyword(message, discovery);

    return message;
  }

  public static Map<String, Object> prepareSpatialBounding(Discovery discovery) {
    var discoveryMap = AvroUtils.avroToMap(discovery, true);
    return (Map<String, Object>)discoveryMap.get("spatialBounding");
  }

  public static List<org.cedar.onestop.mapping.search.Reference> convertReferences(List<Reference> references) {
    return references.stream().map(work -> {
          return new org.cedar.onestop.mapping.search.Reference().withTitle(work.getTitle()).withDate(work.getDate()).withLinks(convertLinks(work.getLinks()));
        })
        .collect(Collectors.toList());
  }

  ////////////////////////////////
  // Identifiers, "Names"       //
  ////////////////////////////////
  private static String prepareInternalParentIdentifier(ParsedRecord record) {
    return Optional.ofNullable(record)
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
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getName)
        .orElse(null);
  }

  static String prepareFileFormat(ParsedRecord record) {
    return Optional.ofNullable(record)
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getFormat)
        .orElse(null);
  }

  static List<Checksum> prepareChecksums(ParsedRecord record) {
    return Optional.ofNullable(record)
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getChecksums)
        .orElse(Collections.emptyList())
        .stream()
        .map(checksumObject -> {
          return new Checksum()
          .withAlgorithm(checksumObject.getAlgorithm().toString()) // TODO will probably fail if it's null?
          .withValue(checksumObject.getValue());
        })
        .collect(Collectors.toList());
  }

  static Long prepareFilesize(ParsedRecord record) {
    return Optional.ofNullable(record)
        .map(ParsedRecord::getFileInformation)
        .map(FileInformation::getSize)
        .orElse(null);
  }

  ////////////////////////////////
  // Services, Links, Protocols //
  ////////////////////////////////
  private static List<ServiceLink> prepareServiceLinks(Discovery discovery) {
    return Optional.ofNullable(discovery)
        .map(Discovery::getServices)
        .orElse(Collections.emptyList())
        .stream()
        .map(service -> {
          return new ServiceLink() // TODO note the class name should probably be ServiceLinks
            .withTitle(service.getTitle())
            .withAlternateTitle(service.getAlternateTitle())
            .withDescription(service.getDescription())
            .withLinks(convertLinks(getLinksForService(service)));
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

  public static List<org.cedar.onestop.mapping.Link> convertLinks(List<Link> links) {
    List<org.cedar.onestop.mapping.Link> list = new ArrayList<org.cedar.onestop.mapping.Link>();
    links.forEach(it -> {
          list.add( new org.cedar.onestop.mapping.Link()
            .withLinkName(it.getLinkName())
            .withLinkProtocol(it.getLinkProtocol())
            .withLinkUrl(it.getLinkUrl())
            .withLinkDescription(it.getLinkDescription())
            .withLinkFunction(it.getLinkFunction()));
        });
    return list;
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
  public static List<org.cedar.onestop.mapping.search.DataFormat> convertDataFormats(Discovery discovery) {
    List<org.cedar.onestop.mapping.search.DataFormat> converted = new ArrayList<org.cedar.onestop.mapping.search.DataFormat>();
    if (discovery == null || discovery.getDataFormats() == null) {
      return converted;
    }
    discovery.getDataFormats().forEach((it) -> {
      converted.add(new org.cedar.onestop.mapping.search.DataFormat().withName(it.getName()).withVersion(it.getVersion()));
    });
    return converted;
  }

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
  private static void prepareResponsibleParties(SearchObjectWithResponsibleParties search, ParsedRecord record) {
    Set<String> individualNames = new HashSet<>();
    Set<String> organizationNames = new HashSet<>();
    Optional.ofNullable(record)
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
    search.setIndividualNames(individualNames);
    search.setOrganizationNames(organizationNames);
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

  private static void prepareDatesForInstant(SearchObjectWithDates search, TemporalBoundingAnalysis analysis) {

    if (analysis.getInstantIndexable()) {
      // paleo dates are not indexable, so only add beginDate or endDate to the index if instantIndexable
      search.setBeginDate(analysis.getInstantUtcDateTimeString());
      search.setEndDate(analysis.getInstantEndUtcDateTimeString());
    }

    search.setBeginYear(analysis.getInstantYear());
    search.setBeginDayOfYear(analysis.getInstantDayOfYear() != null ? analysis.getInstantDayOfYear().shortValue():null);
    search.setBeginDayOfMonth(analysis.getInstantDayOfMonth() != null ? analysis.getInstantDayOfMonth().byteValue():null);
    search.setBeginMonth(analysis.getInstantMonth() != null ? analysis.getInstantMonth().byteValue():null);

    search.setEndYear(analysis.getInstantYear());
    search.setEndDayOfYear(analysis.getInstantEndDayOfYear() != null ? analysis.getInstantEndDayOfYear().shortValue():null);
    search.setEndDayOfMonth(analysis.getInstantEndDayOfMonth() != null ? analysis.getInstantEndDayOfMonth().byteValue():null);
    search.setEndMonth(analysis.getInstantEndMonth() != null ? analysis.getInstantEndMonth().byteValue():null);

  }

  private static void prepareBeginDate(SearchObjectWithDates search, TemporalBoundingAnalysis analysis) {

    if (analysis.getBeginDescriptor() == VALID) {
      if (analysis.getBeginIndexable()) {
        search.setBeginDate(analysis.getBeginUtcDateTimeString());
      }

      search.setBeginYear(analysis.getBeginYear());
      search.setBeginDayOfYear(analysis.getBeginDayOfYear() != null? analysis.getBeginDayOfYear().shortValue():null); // TODO every single call to shortValue() or byteValue should get a null check!
      search.setBeginDayOfMonth(analysis.getBeginDayOfMonth() != null ?analysis.getBeginDayOfMonth().byteValue():null);
      search.setBeginMonth(analysis.getBeginMonth() != null ? analysis.getBeginMonth() .byteValue():null);
    }
  }

  private static void prepareEndDate(SearchObjectWithDates search, TemporalBoundingAnalysis analysis) {

    if (analysis.getEndDescriptor() == VALID) {
      if (analysis.getEndIndexable()) {
        search.setEndDate(analysis.getEndUtcDateTimeString());
      }

      search.setEndYear(analysis.getEndYear());
      search.setEndDayOfYear(analysis.getEndDayOfYear() != null ? analysis.getEndDayOfYear().shortValue() : null);
      search.setEndDayOfMonth(analysis.getEndDayOfMonth() != null? analysis.getEndDayOfMonth().byteValue():null);
      search.setEndMonth(analysis.getEndMonth() != null ? analysis.getEndMonth().byteValue():null); // TODO log potential issues with type conversion?
    }
  }

  private static void prepareDates(SearchObjectWithDates search, TemporalBoundingAnalysis analysis) {
    if (analysis == null) {
      return;
    }
    // If bounding is actually an instant, set search fields accordingly
    if (analysis.getRangeDescriptor() == TimeRangeDescriptor.INSTANT && analysis.getBeginDescriptor() == UNDEFINED) { // distinguished getting begin and end date that were exactly the same (also described as instant), but in that case need to use prepareBeginDate and prepareEndDate to get data off the correct analysis fields
      prepareDatesForInstant(search, analysis);
    } else {
      prepareBeginDate(search, analysis);
      prepareEndDate(search, analysis);
    }

  }

  ////////////////////////////
  // Keywords               //
  ////////////////////////////
  private static void prepareGcmdKeyword(SearchObjectWithKeywords search, Discovery discovery) {
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

    search.setKeywords(allKeywords);
    search.setGcmdScience(groupedKeywords.get("gcmdScience"));
    search.setGcmdScienceServices(groupedKeywords.get("gcmdScienceServices"));
    search.setGcmdLocations(groupedKeywords.get("gcmdLocations"));
    search.setGcmdInstruments(groupedKeywords.get("gcmdInstruments"));
    search.setGcmdPlatforms(groupedKeywords.get("gcmdPlatforms"));
    search.setGcmdProjects(groupedKeywords.get("gcmdProjects"));
    search.setGcmdDataCenters(groupedKeywords.get("gcmdDataCenters"));
    search.setGcmdHorizontalResolution(groupedKeywords.get("gcmdHorizontalResolution"));
    search.setGcmdVerticalResolution(groupedKeywords.get("gcmdVerticalResolution"));
    search.setGcmdTemporalResolution(groupedKeywords.get("gcmdTemporalResolution"));
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
