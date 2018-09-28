package org.cedar.psi.manager.stream

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

@Unroll
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
            begin: [
                exists: true,
                // For why below value is not seconds, see:
                // https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalQueries.html#precision--
                precision: ChronoUnit.NANOS.toString(),
                validSearchFormat: true,
                zoneSpecified: '+01:00',
                utcDateTimeString: '2005-05-09T00:00:00Z'
            ],
            end: [
                exists: true,
                precision: ChronoUnit.DAYS.toString(),
                validSearchFormat: true,
                zoneSpecified: 'UNDEFINED',
                utcDateTimeString: '2010-10-01T23:59:59Z'
            ],
            instant: [
                exists: false,
                precision: 'UNDEFINED',
                validSearchFormat: 'UNDEFINED',
                zoneSpecified: 'UNDEFINED',
                utcDateTimeString: 'UNDEFINED'
            ],
            range: [
                descriptor: 'BOUNDED',
                beginLTEEnd: true
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

  def "#descriptor date range correctly identified when #situation"() {
    given:
    def timeMetadata = metadataMap

    when:
    def timeAnalysis = AnalysisAndValidationService.analyzeTemporalBounding(timeMetadata)

    then:
    timeAnalysis.range.descriptor == descriptor

    where:
    descriptor  | situation                                                   | metadataMap
    'ONGOING'   | 'start date exists but not end date'                        | [temporalBounding: [beginDate: '2010-01-01', endDate: '']]
    'BOUNDED'   | 'start and end date exist and are valid'                    | [temporalBounding: [beginDate: '2000-01-01T00:00:00Z', endDate: '2001-01-01T00:00:00Z']]
    'UNDEFINED' | 'neither start nor end date exist'                          | [temporalBounding: [beginDate: '', endDate: '']]
    'INSTANT'   | 'neither start nor end date exist but valid instant does'   | [temporalBounding: [beginDate: '', endDate: '', instant: '2001-01-01']]
    'INVALID'   | 'end date exists but not start date'                        | [temporalBounding: [beginDate: '', endDate: '2010']]
    'INVALID'   | 'start and end date exist but start after end'              | [temporalBounding: [beginDate: '2100-01-01T00:00:00Z', endDate: '2002-01-01']]
    'INVALID'   | 'neither start nor end date exist but invalid instant does' | [temporalBounding: [beginDate: '', endDate: '', instant: '2001-01-32']]
  }

  def "Begin date LTE end date check is #value when #situation"() {
    given:
    def timeMetadata = metadataMap

    when:
    def timeAnalysis = AnalysisAndValidationService.analyzeTemporalBounding(timeMetadata)

    then:
    timeAnalysis.range.beginLTEEnd == value

    where:
    value       | situation                                                       | metadataMap
    true        | 'start is valid format and before valid format end'             | [temporalBounding: [beginDate: '2010-01-01', endDate: '2011-01-01']]
    false       | 'start is valid format and after valid format end'              | [temporalBounding: [beginDate: '2011-01-01T00:00:00Z', endDate: '2001-01-01T00:00:00Z']]
    true        | 'start is invalid format but paleo and before valid format end' | [temporalBounding: [beginDate: '-1000000000', endDate: '2015']]
    true        | 'start and end both invalid but paleo and start before end'     | [temporalBounding: [beginDate: '-2000000000', endDate: '-1000000000']]
    false       | 'start and end both invalid but paleo and start after end'      | [temporalBounding: [beginDate: '-1000000000', endDate: '-2000000000']]
    true        | 'start and end both same instant'                               | [temporalBounding: [beginDate: '2000-01-01T00:00:00Z', endDate: '2000-01-01T00:00:00Z']]
    'UNDEFINED' | 'start is invalid format but paleo and end is fully invalid'    | [temporalBounding: [beginDate: '-1000000000', endDate: '1999-13-12']]
    'UNDEFINED' | 'start is fully invalid and end is invalid format but paleo'    | [temporalBounding: [beginDate: '15mya', endDate: '-1000000000']]
    'UNDEFINED' | 'start is valid and end is fully invalid'                       | [temporalBounding: [beginDate: '2000-01-01T00:00:00Z', endDate: '2000-12-31T25:00:00Z']]
    'UNDEFINED' | 'start and end both fully invalid'                              | [temporalBounding: [beginDate: '2000-01-01T00:61:00Z', endDate: '2000-11-31T00:00:00Z']]
    'UNDEFINED' | 'start is fully invalid but end is valid'                       | [temporalBounding: [beginDate: '2000-01-01T00:00:61Z', endDate: '2000-01-02T00:00:00Z']]
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
