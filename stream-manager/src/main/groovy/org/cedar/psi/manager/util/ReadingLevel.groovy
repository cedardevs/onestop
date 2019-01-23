package org.cedar.psi.manager.util

import groovy.util.logging.Slf4j

@Slf4j
class ReadingLevel {
  static Map findWordSyllables(String originalWord) {
    def document = ClassLoader.systemClassLoader.getResourceAsStream("25K-syllabified-sorted-alphabetically.txt").text // TODO NUKE
    // return [
    //   match: document.split('\n')[0],
    //   syllables: document.split('\n')[0].split(';').size()
    // ]
    def word = originalWord.toLowerCase()
    def wordsByFirstLetter = document.split('\n').findAll({it.substring(0,1) == word.substring(0,1)}) // TODO is this actually more efficient than just doing a find on the whole doc?
    def match = wordsByFirstLetter.find({ it ->
      return it.replaceAll(';','') == word
    })
    return [
      match: match,
      syllables: match? match.split(';').size(): null,

      //source: https://codegolf.stackexchange.com/questions/47322/how-to-count-the-syllables-in-a-word
      s2: word.findAll(/[aiouy]+e*|e(?!d$|ly).|[td]ed|le$/).size(),
    ]
  }

  static List splitIntoSentences(String text) {
    return text.split(/(\.|\?|\!)\s/)
  }

  static List words(String text) { // TODO rename splitIntoWords?
    return text.toLowerCase().replaceAll(/[^\w\a\s)]/, '').split(/\s/)
  }

  static Number totalSentences(String text) {
    return splitIntoSentences(text).size()
  }
  static Number totalWords(String text) {
    return words(text).size()
  }
  static Number totalSyllables(String text) {
    return words(text).collect({it ->
        return ReadingLevel.findWordSyllables(it).s2
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
