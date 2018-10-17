package org.cedar.onestop.api.metadata.service

import groovy.util.logging.Slf4j
import org.apache.commons.text.WordUtils
import org.springframework.stereotype.Service

import java.time.temporal.ChronoUnit

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
    def temporalBounding = readyDatesForSearch(discovery.temporalBounding, analysis.temporalBounding)
    discovery.temporalBounding.putAll(temporalBounding)
    
    // drop fields
    discovery.remove("responsibleParties")
    discovery.services = [] // this needs to be in place for as long as the manual upload is around
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
        accessionValues         : [],  // this needs to be in place for as long as the manual upload is around
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
  
  static Map readyDatesForSearch(Map temporalBounding, Map temporalAnalysis) {

    def newTemporalBounding = [:]
    newTemporalBounding.putAll(temporalBounding)

    def beginDate, beginYear, endDate, endYear

    // If bounding is actually an instant, set search fields accordingly
    if(temporalAnalysis.range.descriptor == 'INSTANT') {
      def instant = temporalAnalysis.instant
      def year = parseYear(instant.utcDateTimeString)

      // The following will all be the same regardless of precision:
      beginDate = instant.utcDateTimeString // instants normalized to beginning of day
      beginYear = year
      endYear = year

      // Add time and/or date to endDate based on precision
      switch(instant.precision) {
        case ChronoUnit.DAYS.toString():
          // End of day
          endDate = "${temporalBounding.instant}T23:59:59Z" as String
          break
        case ChronoUnit.YEARS.toString():
          if(!instant.validSearchFormat) {
            // Paleo date, so only return year value (null out dates)
            beginDate = null
            endDate = null
          }
          else {
            // Last day of year + end of day
            endDate = "${temporalBounding.instant}-12-31T23:59:59Z" as String
          }
          break
        default:
          // Precision is NANOS so use instant value as-is
          endDate = instant.utcDateTimeString
          break
      }
    }
    else {
      def begin = temporalAnalysis.begin
      def end = temporalAnalysis.end

      // If dates exist and are validSearchFormat (only false here if paleo, since we filtered out bad data earlier),
      // use value from analysis block where dates are UTC datetime normalized
      beginDate = begin.exists && begin.validSearchFormat == true ? begin.utcDateTimeString : null
      endDate = end.exists && end.validSearchFormat == true ? end.utcDateTimeString : null
      beginYear = parseYear(begin.utcDateTimeString)
      endYear = parseYear(end.utcDateTimeString)
    }

    // Replace & add fields before returning
    newTemporalBounding.put('beginDate', beginDate)
    newTemporalBounding.put('beginYear', beginYear)
    newTemporalBounding.put('endDate', endDate)
    newTemporalBounding.put('endYear', endYear)
    return newTemporalBounding
  }

  static Long parseYear(String utcDateTime) {
    if(utcDateTime == 'UNDEFINED') {
      return null
    }
    else {
      // Watch out for BCE years
      return Long.parseLong(utcDateTime.substring(0, utcDateTime.indexOf('-', 1)))
    }

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
