package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.apache.commons.text.WordUtils
import org.springframework.stereotype.Service

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery

@Slf4j
@Service
class ManipulateMetadataService {
  static Map oneStopReady(Map discovery, Map analysis) {
    // create gcmdkeywords
    Map gcmdKeywords = createGcmdKeyword(discovery)
    discovery.putAll(gcmdKeywords)
    
    // create contacts ,creators and publishers
    Map<String, Set> partyData = parseDataResponsibleParties(discovery.responsibleParties as Map)
    discovery.putAll(partyData)
    
    // update temporal Bounding
    def temporalBounding = elasticDateInfo(discovery.temporalBounding, analysis.temporalBounding)
    discovery.temporalBounding.putAll(temporalBounding)
    
    // drop fields
    discovery.remove("responsibleParties")
    discovery.remove("services")
    return discovery
  }
  
  //Create GCMD keyword lists
  static Map createGcmdKeyword(Map record) {
    def gcmdScience = [] as Set
    def gcmdScienceServices = [] as Set
    def gcmdLocations = [] as Set
    def gcmdPlatforms = [] as Set
    def gcmdInstruments = [] as Set
    def gcmdProjects = [] as Set
    def gcmdHorizontalResolution = [] as Set
    def gcmdVerticalResolution = [] as Set
    def gcmdTemporalResolution = [] as Set
    def gcmdDataCenters = [] as Set
    
    //remove and create new keywords with out accession values
    def keywords = record.keywords.findAll { keys ->
      keys.namespace != 'NCEI ACCESSION NUMBER'
    }
    
    keywords.each { group ->
      def it = group.namespace.toLowerCase() ?: null
      def keywordsInGroup = group.values ?: null
      if (it.contains('gcmd') || it.contains('global change master directory')) {
        switch (it) {
          case { it.contains('earth science services') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdScienceServices.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('earth science') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdScience.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('location') || it.contains('place') }:
            keywordsInGroup.each { k ->
              def text = normalizeHierarchyKeyword(k)
              gcmdLocations.addAll(tokenizeHierarchyKeyword(text))
            }
            break
          case { it.contains('platform') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdPlatforms.add(text)
            }
            break
          case { it.contains('instrument') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdInstruments.add(text)
            }
            break
          case { it.contains('data center') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdDataCenters.add(text)
            }
            break
          case { it.contains('horizontal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdHorizontalResolution.add(text)
            }
            break
          case { it.contains('vertical data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdVerticalResolution.add(text)
            }
            break
          case { it.contains('temporal data resolution') }:
            keywordsInGroup.each { k ->
              def text = WordUtils.capitalizeFully(k, capitalizingDelimiters)
              gcmdTemporalResolution.add(text)
            }
            break
          case { it.contains('project') }:
            keywordsInGroup.each { k ->
              def text = normalizeNonHierarchicalKeyword(k)
              gcmdProjects.add(text)
            }
            break
          default:
            // Namespace didn't meet our checks
            log.debug("Namespace didn't meet our checks")
        }
      }
    }
    
    return [
        keywords                : keywords,
        gcmdScienceServices     : gcmdScienceServices,
        gcmdScience             : gcmdScience,
        gcmdLocations           : gcmdLocations,
        gcmdInstruments         : gcmdInstruments,
        gcmdPlatforms           : gcmdPlatforms,
        gcmdProjects            : gcmdProjects,
        gcmdDataCenters         : gcmdDataCenters,
        gcmdHorizontalResolution: gcmdHorizontalResolution,
        gcmdVerticalResolution  : gcmdVerticalResolution,
        gcmdTemporalResolution  : gcmdTemporalResolution
    ]
  }
  
  /*
  Create contacts, creators and publishers from responsibleParties
  */
  
  static Map<String, String> parseParty(Map party) {
    String individualName = party.individualName ?: null
    String organizationName = party.organizationName ?: null
    String positionName = party.positionName ?: null
    String role = party.role ?: null
    String email = party.email ?: null
    String phone = party.phone ?: null
    return [
        individualName  : individualName,
        organizationName: organizationName,
        positionName    : positionName,
        role            : role,
        email           : email,
        phone           : phone
    ]
  }
  
  static Map<String, Set> parseDataResponsibleParties(Map responsibleParties) {
    Set contacts = []
    Set contactRoles = ['pointOfContact', 'distributor']
    Set creators = []
    Set creatorRoles = ['resourceProvider', 'originator', 'principalInvestigator', 'author', 'collaborator', 'coAuthor']
    Set publishers = []
    Set publisherRoles = ['publisher']
    
    responsibleParties.each { party ->
      def parsedParty = parseParty(party as Map)
      if (contactRoles.contains(parsedParty.role)) {
        contacts.add(parsedParty)
      } else if (creatorRoles.contains(parsedParty.role)) {
        creators.add(parsedParty)
      } else if (publisherRoles.contains(parsedParty.role)) {
        publishers.add(parsedParty)
      }
    }
    return [contacts: contacts, creators: creators, publishers: publishers]
  }
  
  // handle 3 optional date formats in priority of full-parse option to minimal-parse options
  static final DateTimeFormatter PARSE_DATE_FORMATTER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_ZONED_DATE_TIME)  // e.g. - 2010-12-30T00:00:00Z
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)  // e.g. - 2010-12-30T00:00:00
      .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)       // e.g. - 2010-12-30
      .toFormatter()
      .withResolverStyle(ResolverStyle.STRICT)
  
  static Map elasticDateInfo(Map temporalBounding, Map temporalAnlysis) {
    /*
    * validSearchFormat == false AND associated precision == ChronoUnit.YEARS.toString(), null
    * */
    def beginDateAnlysis = temporalAnlysis.begin as Map
    def endDateAnlysis = temporalAnlysis.end as Map
    TemporalAccessor parsedDate = null
    Long beginYear
    Long endYear
    // begin date and year
    if (!temporalBounding.beginDate) {
      beginYear = null
    } else {
      String beginDate = temporalBounding.beginDate as String
      // paleo dates can be longs
      beginYear = parsedYear(beginDate)
      // invalid Format
      if (beginDateAnlysis.validSearchFormat == false && beginDateAnlysis.precision == ChronoUnit.YEARS.toString()) {
        temporalBounding.beginDate = null
      }
    }
    
    // end date and year
    if (!temporalBounding.endDate) {
      endYear = null
    } else {
      String endDate = temporalBounding.endDate as String
      // paleo dates can be longs
      endYear = parsedYear(endDate)
      // invalid Format
      if (endDateAnlysis.validSearchFormat == false && endDateAnlysis.precision == ChronoUnit.YEARS.toString()) {
        temporalBounding.endDate = null
      }
    }
    
    //Update begin and end dates & years based on instant value if date range is 'INSTANT'
    // ToDo this definitely need to be reviewed (can't see much in too it till i see such data)
    // ToDo what if the precision is days
    def range = temporalAnlysis.range as Map
    def instant = temporalAnlysis.instant as Map
    if (range.descriptor == 'INSTANT') {
      if (instant.validSearchFormat == false && instant.precision == ChronoUnit.YEARS.toString()) {
        beginYear = parsedYear(temporalBounding.instant as String)
      } else {
        temporalBounding.beginDate = temporalBounding.instant
        beginYear = parsedYear(temporalBounding.instant as String)
      }
    }
    
    // update year
    def begin = ['beginYear': beginYear]
    def end = ['endYear': endYear]
    temporalBounding.putAll(begin)
    temporalBounding.putAll(end)
    
    return temporalBounding
  }
  
  // parse year
  static Long parsedYear(String date) {
    TemporalAccessor parsedDate = null
    Long year
    if (date.isLong()) {
      year = Long.parseLong(date)
    } else {
      // the "::" operator in Java8 is ".&" in groovy until groovy fully adopts "::"
      parsedDate = PARSE_DATE_FORMATTER.parseBest(date, ZonedDateTime.&from as TemporalQuery, LocalDateTime.&from as TemporalQuery, LocalDate.&from as TemporalQuery)
      year = parsedDate.get(ChronoField.YEAR)
      
      if (parsedDate instanceof LocalDateTime) {
        // assume UTC
        ZonedDateTime parsedDateUTC = parsedDate.atZone(ZoneId.of("UTC"))
        // re-evaluate year in off-chance year was affected by zone id
        year = parsedDateUTC.get(ChronoField.YEAR)
      }
    }
    
    return year
  }
  
  // helper functions
  static String normalizeHierarchyKeyword(String text) {
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    return WordUtils.capitalizeFully(cleanText, capitalizingDelimiters)
        .replace('Earth Science > ', '').replace('Earth Science Services > ', '')
  }
  
  static String normalizeNonHierarchicalKeyword(String text) {
    // These are in the format 'Short Name > Long Name', where 'Short Name' is likely an acronym. This normalizing allows
    // for title casing the 'Long Name' if and only if it's given in all caps or all lowercase (so we don't title case an
    // acronym here)
    def cleanText = cleanInternalGCMDKeywordWhitespace(text)
    def elements = Arrays.asList(cleanText.split(' > '))
    String longName = elements.last()
    if (longName == longName.toUpperCase() || longName == longName.toLowerCase()) {
      longName = WordUtils.capitalizeFully(longName, capitalizingDelimiters)
      elements.set(elements.size() - 1, longName)
    }
    return String.join(' > ', elements)
  }
  
  static final char[] capitalizingDelimiters = [' ', '/', '.', '(', '-', '_'].collect({ it as char })
  
  static String cleanInternalGCMDKeywordWhitespace(String text) {
    String cleanString = text.replaceAll("\\s+", " ")
    return cleanString
  }
  
  static List<String> tokenizeHierarchyKeyword(String text) {
    def result = []
    def i = text.length()
    while (i > 0) {
      text = text.substring(0, i).trim()
      result.add(text)
      i = text.lastIndexOf('>', i)
    }
    return result
  }
}
