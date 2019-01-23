package org.cedar.psi.manager.util

import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestMatchers.closeTo

@Unroll
class ReadingLevelSpec extends Specification {

  def 'words in a few sentences'() {
    when:
    def words = ReadingLevel.splitIntoWords('A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.')

    then:
    words.size() == 13
    words[0] == 'a'
    words[2] == 'separate'
    words[12] == 'anyway'

    when:
    def syllables = words.collect({it ->
      println(it)
      return ReadingLevel.findSyllablesInWord(it)
    })//.sum()

    then:
    syllables == [1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 3, 4, 3] // 24 // it says 4 for hopefully, because estimating syllables with regex is nontrivial! (it's that internal silent 'e')
  }

  def 'sentences are not too bad...'() {
    when:
    def sentences = ReadingLevel.splitIntoSentences('A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.')

    then:
    sentences.size() == 6
  }

  def 'components of readability algorithm'() {
    def text = 'A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.'

    when:
    def sentences = ReadingLevel.totalSentences(text)
    def words = ReadingLevel.totalWords(text)
    def syllables = ReadingLevel.totalSyllables(text)
    def fraction = words/sentences
    def fraction2 = syllables/words

    then:
    sentences == 6
    words == 13
    syllables == 24
    closeTo(2.16, 0.01).matches(fraction)
    closeTo(1.85, 0.01).matches(fraction2)
  }

  def 'readability example #desc' () {
    when:
    boolean wcagSuccess = ReadingLevel.wcagReadingLevelCriteria(text)
    Number easeScore = ReadingLevel.FleschReadingEaseScore(text)
    Number gradeLevel = ReadingLevel.FleschKincaidReadingGradeLevel(text)

    then:
    closeTo(expectedEase, 0.01).matches(easeScore)
    closeTo(expectedGrade, 0.01).matches(gradeLevel)
    wcagSuccess == expectedWcagCriteriaSuccess

    where:
    desc | expectedEase | expectedGrade | expectedWcagCriteriaSuccess | text
    'A short example paragraph' | 48.45 | 7.04 | true | 'A few separate. Sentences! And such?! That are not too confusing? Hopefully... Anyway.'
    'A clear readable example' | 92.17 | 3.26 | true | 'The goal is to have simple, clear text that anyone can read. This is not easy to do, or even truely to measure.'
    'A GHRSST description' | 20.6 | 16.51 | false | 'A Group for High Resolution Sea Surface Temperature (GHRSST) Level 2P dataset based on multi-channel sea surface temperature (SST) retrievals generated in real-time from the Infrared Atmospheric Sounding Interferometer (IASI) on the European Meteorological Operational-B (MetOp-B)satellite (launched 17 Sep 2012). The European Organization for the Exploitation of Meteorological Satellites (EUMETSAT),Ocean and Sea Ice Satellite Application Facility (OSI SAF) is producing SST products in near realtime from METOP/IASI. The Infrared Atmospheric Sounding Interferometer (IASI) measures inthe infrared part of the electromagnetic spectrum at a horizontal resolution of 12 km at nadir up to40km over a swath width of about 2,200 km. With 14 orbits in a sun-synchronous mid-morningorbit (9:30 Local Solar Time equator crossing, descending node) global observations can beprovided twice a day. The SST retrieval is performed and provided by the IASI L2 processor atEUMETSAT headquarters. The product format is compliant with the GHRSST Data Specification(GDS) version 2.'
  }
}
