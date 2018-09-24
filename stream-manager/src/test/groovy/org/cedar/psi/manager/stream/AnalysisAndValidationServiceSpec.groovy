package org.cedar.psi.manager.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

class AnalysisAndValidationServiceSpec extends Specification {

  final String analysisSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('analysis-schema.json').text
  final String identifierSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('identifiers-schema.json').text
  final String inputSchemaString = ClassLoader.systemClassLoader.getResourceAsStream('input-schema.json').text

  final ObjectMapper mapper = new ObjectMapper()
  final JsonSchemaFactory factory = JsonSchemaFactory.byDefault()

  final JsonNode analysisSchemaNode = mapper.readTree(analysisSchemaString)
  final JsonNode identifierSchemaNode = mapper.readTree(identifierSchemaString)
  final JsonNode inputSchemaNode = mapper.readTree(inputSchemaString)

  final JsonSchema analysisSchema = factory.getJsonSchema(analysisSchemaNode)
  final JsonSchema identifierSchema = factory.getJsonSchema(identifierSchemaNode)
  final JsonSchema inputSchema = factory.getJsonSchema(inputSchemaNode)

  def "All valid fields return expected response from service"() {
    given:
    def inputMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-iso.json').text
    inputSchema.validate(mapper.readTree(inputMsg))

    def inputMap = [:]
    inputMap.put('discovery', new JsonSlurper().parseText(inputMsg))
    def expectedAnalysisMap = [
        identification  : [
            fileIdentifier    : [
                exists: true,
                fileIdentifierString: 'gov.super.important:FILE-ID'
            ],
            doi               : [
                exists: true,
                doiString: 'doi:10.5072/FK2TEST'
            ],
            parentIdentifier  : [
                exists: true,
                parentIdentifierString: 'gov.super.important:PARENT-ID'
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
    analysisSchema.validate(mapper.readTree(response))
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
    identifierSchema.validate(mapper.readTree(JsonOutput.toJson(identifiersAnalysis)))

    identifiersAnalysis == [
        fileIdentifier    : [
            exists: true,
            fileIdentifierString: 'xyz'
        ],
        doi               : [
            exists: false,
            doiString: null
        ],
        parentIdentifier  : [
            exists: false,
            parentIdentifierString: null
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
    identifierSchema.validate(mapper.readTree(JsonOutput.toJson(identifiersAnalysis)))
    identifiersAnalysis == [
        fileIdentifier    : [
            exists: true,
            fileIdentifierString: 'xyz'
        ],
        doi               : [
            exists: false,
            doiString: null
        ],
        parentIdentifier  : [
            exists: false,
            parentIdentifierString: null
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
