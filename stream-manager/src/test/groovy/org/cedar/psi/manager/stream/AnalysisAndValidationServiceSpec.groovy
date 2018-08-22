package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class AnalysisAndValidationServiceSpec extends Specification {

  def "All valid fields return expected response from service"() {
    given:
    def inputMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-iso.json').text
    def inputMap = new JsonSlurper().parseText(inputMsg)
    def expectedAnalysisMap = [
        identification  : [],
        temporalBounding: [
            beginDate: [
                exists: true,
                valid : true
            ],
            endDate  : [
                exists: true,
                valid : true
            ],
            instant  : [
                exists: false,
                valid : true
            ]
        ],
        spatialBounding : [],
        titles          : [],
        description     : [
            exists: true,
            characters: 65
        ],
        thumbnail       : [],
        dataAccess      : []
    ]
    inputMap.put('analysis', expectedAnalysisMap)
    def expectedResponse = JsonOutput.toJson(inputMap)

    when:
    def response = JsonOutput.toJson(AnalysisAndValidationService.analyzeParsedMetadata(inputMap))

    then:
    response == expectedResponse
  }

  def "Invalidly formatted time fields accurately identified"() {
    given:
    def inputMsg = [
        temporalBounding: [
            beginDate           : '1984-04-31',
            beginIndeterminate  : null,
            beginYear           : null,
            endDate             : '1985-05-09T00:00:00Z',
            endIndeterminate    : null,
            endYear             : 1985,
            instant             : null,
            instantIndeterminate: null,
            description         : null,
            invalidDates        : [
                begin  : true,
                end    : false,
                instant: false
            ]
        ]
    ]

    when:
    def timeAnalysis = AnalysisAndValidationService.analyzeTemporalBounding(inputMsg)

    then:
    timeAnalysis == [
        beginDate: [
            exists: true,
            valid: false
        ],
        endDate: [
            exists: true,
            valid: true
        ],
        instant: [
            exists: false,
            valid: true
        ]
    ]
  }

  def "Invalid access protocols accurately identified"() {
    // TODO
  }

  def "Missing required identifiers detected"() {
    // TODO
  }

  def "Mismatch between metadata type and corresponding identifiers detected"() {
    // TODO
  }

  def "Missing title detected"() {
    // TODO
  }

  def "Missing description detected"() {
    given:
    def inputMap = [:]

    when:
    def descriptionAnalysis = AnalysisAndValidationService.analyzeDescription(inputMap)

    then:
    descriptionAnalysis == [
        exists: false,
        characters: 0
    ]
  }

  def "Missing thumbnail URL detected"() {
    // TODO
  }
}
