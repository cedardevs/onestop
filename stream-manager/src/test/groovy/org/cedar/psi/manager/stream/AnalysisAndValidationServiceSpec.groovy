package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class AnalysisAndValidationServiceSpec extends Specification {

  def "All valid fields return expected response from service"() {
    given:
    def inputMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-iso.json').text
    def inputMap = [:]
    inputMap.put('discovery', new JsonSlurper().parseText(inputMsg))
    def expectedAnalysisMap = [
        identification  : [
            fileIdentifier    : [
                exists: true
            ],
            doi               : [
                exists: true
            ],
            parentIdentifier  : [
                exists: true
            ],
            hierarchyLevelName: [
                exists            : true,
                matchesIdentifiers: true
            ]
        ],
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
        spatialBounding : [
            exists: true
        ],
        titles          : [
            title: [
                exists: true,
                characters: 63
            ],
            alternateTitle: [
                exists: true,
                characters: 51
            ]
        ],
        description     : [
            exists    : true,
            characters: 65
        ],
        thumbnail       : [
            exists: true,
        ],
        dataAccess      : [
            exists: true
        ]
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
    def metadata = [
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
    def timeAnalysis = AnalysisAndValidationService.analyzeTemporalBounding(metadata)

    then:
    timeAnalysis == [
        beginDate: [
            exists: true,
            valid : false
        ],
        endDate  : [
            exists: true,
            valid : true
        ],
        instant  : [
            exists: false,
            valid : true
        ]
    ]
  }

  def "Missing links detected"() {
    given:
    def metadata = [
        links: []
    ]

    when:
    def dataAccessAnalysis = AnalysisAndValidationService.analyzeDataAccess(metadata)

    then:
    dataAccessAnalysis == [
        exists    : false
    ]
  }

  def "Missing required identifiers detected"() {
    given:
    def metadata = [
        fileIdentifier: 'xyz',
    ]

    when:
    def identifiersAnalysis = AnalysisAndValidationService.analyzeIdentifiers(metadata)

    then:
    identifiersAnalysis == [
        fileIdentifier    : [
            exists: true
        ],
        doi               : [
            exists: false
        ],
        parentIdentifier  : [
            exists: false
        ],
        hierarchyLevelName: [
            exists            : false,
            matchesIdentifiers: true
        ]
    ]
  }

  def "Mismatch between metadata type and corresponding identifiers detected"() {
    given:
    def metadata = [
        fileIdentifier    : 'xyz',
        hierarchyLevelName: 'granule'
    ]

    when:
    def identifiersAnalysis = AnalysisAndValidationService.analyzeIdentifiers(metadata)

    then:
    identifiersAnalysis == [
        fileIdentifier    : [
            exists: true
        ],
        doi               : [
            exists: false
        ],
        parentIdentifier  : [
            exists: false
        ],
        hierarchyLevelName: [
            exists            : true,
            matchesIdentifiers: false
        ]
    ]
  }

  def "Missing titles detected"() {
    given:
    def metadata = [:]

    when:
    def titlesAnalysis = AnalysisAndValidationService.analyzeTitles(metadata)

    then:
    titlesAnalysis == [
        title         : [
            exists    : false,
            characters: 0
        ],
        alternateTitle: [
            exists    : false,
            characters: 0
        ]
    ]
  }

  def "Missing description detected"() {
    given:
    def metadata = [:]

    when:
    def descriptionAnalysis = AnalysisAndValidationService.analyzeDescription(metadata)

    then:
    descriptionAnalysis == [
        exists    : false,
        characters: 0
    ]
  }

  def "Missing thumbnail URL detected"() {
    given:
    def metadata = [:]

    when:
    def thumbnailAnalysis = AnalysisAndValidationService.analyzeThumbnail(metadata)

    then:
    thumbnailAnalysis == [
        exists: false
    ]
  }
}
