package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j

@Slf4j
class ReadingLevel {
  static Number findSyllablesInWord(String originalWord) {
    def word = originalWord.toLowerCase()
    //source for syllables: https://codegolf.stackexchange.com/questions/47322/how-to-count-the-syllables-in-a-word
    // note this is an estimate, and occasionally gets the answer wrong, but is generally close enough
    return word.findAll(/[aiouy]+e*|e(?!d$|ly).|[td]ed|le$/).size()
  }

  static List splitIntoSentences(String text) {
    return text.split(/(\.|\?|\!)\s/)
  }

  static List splitIntoWords(String text) { // TODO rename splitIntoWords?
    return text.toLowerCase().replaceAll(/[^\w\a\s)]/, '').split(/\s/)
  }

  static Number totalSentences(String text) {
    return splitIntoSentences(text).size()
  }

  static Number totalWords(String text) {
    return splitIntoWords(text).size()
  }

  static Number totalSyllables(String text) {
    return splitIntoWords(text).collect({it ->
        return ReadingLevel.findSyllablesInWord(it)
      }).sum()
  }

  /**
  Score value should be 0-100.
  100 -> very easy to read
  0 -> readable only by college graduate (very difficult)
  */
  static Number readabilityFleschKincaid(String text) {
    def words = totalWords(text)
    def sentences = totalSentences(text)
    def syllables = totalSyllables(text)
    return 206.835 - 1.015 * (words/sentences) - 84.6 * (syllables/words)
  }

  static boolean passesReadabilityTest(String text) {
    Number score =  readabilityFleschKincaid(text)
    return score >= 60 // 9th grade reading level or easier, I *think* this correlates to 'lower secondary education level'
  }
}
