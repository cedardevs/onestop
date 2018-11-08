package org.cedar.psi.manager.stream

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.avro.AvroTypeException
import org.apache.avro.Schema
import org.apache.avro.io.DatumReader
import org.apache.avro.io.Decoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.cedar.psi.common.avro.Analysis
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

@Unroll
class AnalysisAndValidationServiceSpec extends Specification {

  final String analysisAvro = ClassLoader.systemClassLoader.getResourceAsStream('avro/analysis.avsc').text

  def "All valid fields return expected response from service"() {
    given:
    def inputMsg = ClassLoader.systemClassLoader.getResourceAsStream('parsed-iso.json').text

    def inputMap = [:]
    inputMap.put('discovery', new JsonSlurper().parseText(inputMsg))
    def expectedAnalysisMap = [
        identification  : [
            fileIdentifierExists    : true,
            fileIdentifierString    : 'gov.super.important:FILE-ID',
            doiExists               : true,
            doiString               : 'doi:10.5072/FK2TEST',
            parentIdentifierExists  : true,
            parentIdentifierString  : 'gov.super.important:PARENT-ID',
            hierarchyLevelNameExists: true,
            matchesIdentifiers      : true
        ],
        temporalBounding: [
            beginExists             : true,
            // For why below value is not seconds, see:
            // https://docs.oracle.com/javase/8/docs/api/java/time/temporal/TemporalQueries.html#precision--
            beginPrecision          : ChronoUnit.NANOS.toString(),
            beginValidSearchFormat  : true,
            beginZoneSpecified      : '+01:00',
            beginUtcDateTimeString  : '2005-05-09T00:00:00Z',
            endExists               : true,
            endPrecision            : ChronoUnit.DAYS.toString(),
            endValidSearchFormat    : true,
            endZoneSpecified        : 'UNDEFINED',
            endUtcDateTimeString    : '2010-10-01T23:59:59Z',
            instantExists           : false,
            instantPrecision        : 'UNDEFINED',
            instantValidSearchFormat: 'UNDEFINED',
            instantZoneSpecified    : 'UNDEFINED',
            instantUtcDateTimeString: 'UNDEFINED',
            rangeDescriptor         : 'BOUNDED',
            rangeBeginLTEEnd        : true
        ],
        spatialBounding : [
            spatialBoundingExists: true
        ],
        titles          : [
            titleExists             : true,
            titleCharacters         : 63,
            alternateTitleExists    : true,
            alternateTitleCharacters: 51
        ],
        description     : [
            descriptionExists    : true,
            descriptionCharacters: 65
        ],
        thumbnail       : [
            thumbnailExists: true,
        ],
        dataAccess      : [
            dataAccessExists: true
        ]
    ]
    inputMap.put('analysis', expectedAnalysisMap)
    def expectedResponse = JsonOutput.toJson(inputMap)

    when:
    def response = AnalysisAndValidationService.analyzeParsedMetadata(inputMap)
    def responseJson = JsonOutput.toJson(AnalysisAndValidationService.analyzeParsedMetadata(inputMap))
    def analysisJson = JsonOutput.toJson(response.analysis)
    Schema schema = new Schema.Parser().parse(analysisAvro)

    then:
    validateJson(analysisJson, schema)
    responseJson == expectedResponse
  }

  def "#descriptor date range correctly identified when #situation"() {
    given:
    def timeMetadata = metadataMap

    when:
    def timeAnalysis = AnalysisAndValidationService.analyzeTemporalBounding(timeMetadata)

    then:
    timeAnalysis.rangedescriptor == descriptor

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
    timeAnalysis.rangebeginLTEEnd == value

    where:
    value       | situation                                                       | metadataMap
    true        | 'start is valid format and before valid format end'             | [temporalBounding: [beginDate: '2010-01-01', endDate: '2011-01-01']]
    false       | 'start is valid format and after valid format end'              | [temporalBounding: [beginDate: '2011-01-01T00:00:00Z', endDate: '2001-01-01T00:00:00Z']]
    true        | 'start is invalid format but paleo and before valid format end' | [temporalBounding: [beginDate: '-1000000000', endDate: '2015']]
    true        | 'start and end both invalid but paleo and start before end'     | [temporalBounding: [beginDate: '-2000000000', endDate: '-1000000000']]
    false       | 'start and end both invalid but paleo and start after end'      | [temporalBounding: [beginDate: '-1000000000', endDate: '-2000000000']]
    true        | 'start and end both same instant'                               | [temporalBounding: [beginDate: '2000-01-01T00:00:00Z', endDate: '2000-01-01T00:00:00Z']]
    true        | 'start exists but not end'                                      | [temporalBounding: [beginDate: '2000-01-01T00:00:00Z', endDate: '']]
    'UNDEFINED' | 'start does not exist but end does'                             | [temporalBounding: [beginDate: '', endDate: '2000-01-01T00:00:00Z']]
    'UNDEFINED' | 'neither start nor end exist'                                   | [temporalBounding: [beginDate: '', endDate: '']]
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
        dataAccessExists: false
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
        fileIdentifierExists    : true,
        fileIdentifierString    : 'xyz',
        doiExists               : false,
        doiString               : null,
        parentIdentifierExists  : false,
        parentIdentifierString  : null,
        hierarchyLevelNameExists: false,
        matchesIdentifiers      : true
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
        fileIdentifierExists    : true,
        fileIdentifierString    : 'xyz',
        doiExists               : false,
        doiString               : null,
        parentIdentifierExists  : false,
        parentIdentifierString  : null,
        hierarchyLevelNameExists: true,
        matchesIdentifiers      : false
    ]
  }

  def "Missing titles detected"() {
    given:
    def metadata = [title: '']

    when:
    def titlesAnalysis = AnalysisAndValidationService.analyzeTitles(metadata)

    then:
    then:
    titlesAnalysis == [
        titleExists             : false,
        titleCharacters         : 0,
        alternateTitleExists    : false,
        alternateTitleCharacters: 0
    ]
  }

  def "Missing description detected"() {
    given:
    def metadata = [description: '']

    when:
    def descriptionAnalysis = AnalysisAndValidationService.analyzeDescription(metadata)

    then:
    descriptionAnalysis == [
        descriptionExists    : false,
        descriptionCharacters: 0
    ]
  }

  def "Missing thumbnail URL detected"() {
    given:
    def metadata = [:]

    when:
    def thumbnailAnalysis = AnalysisAndValidationService.analyzeThumbnail(metadata)

    then:
    thumbnailAnalysis == [
        thumbnailExists: false
    ]
  }

  static boolean validateJson(String json, Schema schema) throws Exception {
    InputStream input = new ByteArrayInputStream(json.getBytes())
    DataInputStream din = new DataInputStream(input)

    try {
      DatumReader reader = new SpecificDatumReader(Analysis)
      Decoder decoder = DecoderFactory.get().jsonDecoder(schema, din)
      reader.read(null, decoder)
      return true
    } catch (AvroTypeException e) {
      System.out.println(e.getMessage())
      return false
    }
  }
}
