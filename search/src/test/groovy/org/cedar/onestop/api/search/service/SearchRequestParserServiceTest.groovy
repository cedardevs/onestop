package org.cedar.onestop.api.search.service

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SearchRequestParserServiceTest extends Specification {

  private slurper = new JsonSlurper()
  private requestParser = new SearchRequestParserService(null)

  def "Request with zeros in #type filter #relative (#relation) creates valid elasticsearch request"() {
    // confirms a bug fix - 0s were causing no filter to be created
    given: 'datetime or year filters before/after 0'
    def json = """{
        "filters": [{
                      "type": "${type}",
                      "relation": "${relation}",
                      "${relative}": 0
                  }],
        "summary": false
    }"""
    def params = slurper.parseText(json)

    when:
    def yearFilters = requestParser.constructYearFilter(params.filters)

    then: 'the query should not be empty'
    yearFilters.size() == 1

    where:
    type       | relation     | relative
    'year'     | 'intersects' | 'after'
    'year'     | 'intersects' | 'before'
    'year'     | 'disjoint'   | 'after'
    'year'     | 'disjoint'   | 'before'
    'year'     | 'contains'   | 'after'
    'year'     | 'contains'   | 'before'
    'year'     | 'within'     | 'after'
    'year'     | 'within'     | 'before'

    'datetime' | 'intersects' | 'after'
    'datetime' | 'intersects' | 'before'
    'datetime' | 'disjoint'   | 'after'
    'datetime' | 'disjoint'   | 'before'
    'datetime' | 'contains'   | 'after'
    'datetime' | 'contains'   | 'before'
    'datetime' | 'within'     | 'after'
    'datetime' | 'within'     | 'before'
  }

  def "Request with #label creates empty elasticsearch request"() {
    given:
    def params = slurper.parseText(json)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery

    where:
    label                       | json
    'nothing'                   | '{}'
    'empty queries and filters' | '{"queries":[],"filters":[]}'
    'only queries'              | '{"queries":[]}'
    'only filters'              | '{"filters":[]}'
  }

  def "Test only queryText specified"() {
    given:
    def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [[
                                           query_string: [
                                               query               : "winter",
                                               phrase_slop         : 0,
                                               tie_breaker         : 0,
                                               minimum_should_match: '75%',
                                               lenient             : true
                                           ]]]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def "Test only queryText with field boosts configured"() {
    given:
    def config = new SearchConfig(fields: [title: 4])
    config.initialize()
    def parser = new SearchRequestParserService(config)

    def request = '{"queries":[{"type":"queryText","value":"winter"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = parser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [[
                                           query_string: [
                                               query               : "winter",
                                               fields              : ["title^4.0"],
                                               phrase_slop         : 0,
                                               tie_breaker         : 0,
                                               minimum_should_match: '75%',
                                               lenient             : true
                                           ]]]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Multiple queryText objects build right request'() {
    given:
    def request = '{"queries":[{"type":"queryText","value":"winter"},{"type":"queryText","value":"is"},{"type":"queryText","value":"coming"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [[
                                           query_string: [
                                               query               : "(winter) AND (is) AND (coming)",
                                               phrase_slop         : 0,
                                               tie_breaker         : 0,
                                               minimum_should_match: '75%',
                                               lenient             : true
                                           ]]]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Granule name query generates expected elasticsearch query when #desc'() {
    given:
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [[
                                           multi_match: [
                                               query   : "ghrsst goes",
                                               fields  : expectedFields,
                                               operator: expectedOperator,
                                               type    : 'cross_fields'
                                           ]]]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery

    where:
    desc                                       | request                                                                                      | expectedOperator | expectedFields
    'defaults for field and allTermsMustMatch' | '{"queries":[{"type": "granuleName", "value": "ghrsst goes"}]}'                              | 'OR'             | ['title', 'fileIdentifier', 'filename']
    'declared single field value'              | '{"queries":[{"type": "granuleName", "value": "ghrsst goes", "field": "title" }]}'           | 'OR'             | ['title']
    'declared "all" field value'               | '{"queries":[{"type": "granuleName", "value": "ghrsst goes", "field": "all" }]}'             | 'OR'             | ['title', 'fileIdentifier', 'filename']
    'allTermsMustMatch is false'               | '{"queries":[{"type": "granuleName", "value": "ghrsst goes", "allTermsMustMatch": false }]}' | 'OR'             | ['title', 'fileIdentifier', 'filename']
    'allTermsMustMatch is true'                | '{"queries":[{"type": "granuleName", "value": "ghrsst goes", "allTermsMustMatch": true }]}'  | 'AND'            | ['title', 'fileIdentifier', 'filename']
  }

  def 'Multiple granuleName objects build right request'() {
    given:
    def request = '{"queries":[{"type":"granuleName","value":"goes","field":"filename"},{"type":"granuleName","value":"ghrsst","field":"title"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [
                                    [
                                        multi_match: [
                                            query   : "goes",
                                            fields  : ['filename'],
                                            operator: 'OR',
                                            type    : 'cross_fields'
                                        ]
                                    ],
                                    [
                                        multi_match: [
                                            query   : "ghrsst",
                                            fields  : ['title'],
                                            operator: 'OR',
                                            type    : 'cross_fields'
                                        ]
                                    ]
                                ]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Granule name and query text queries in request build expected request'() {
    given:
    def request = '{"queries":[{"type":"queryText","value":"winter"},{"type":"granuleName","value":"ghrsst goes"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [
                [
                    function_score: [
                        query             : [
                            bool: [
                                must: [
                                    [
                                        query_string: [
                                            query               : "winter",
                                            phrase_slop         : 0,
                                            tie_breaker         : 0,
                                            minimum_should_match: '75%',
                                            lenient             : true
                                        ]
                                    ],
                                    [
                                        multi_match: [
                                            query   : "ghrsst goes",
                                            fields  : ['title', 'fileIdentifier', 'filename'],
                                            operator: 'OR',
                                            type    : 'cross_fields'
                                        ]
                                    ]
                                ]]
                        ],
                        field_value_factor: [
                            field   : 'dsmmAverage',
                            modifier: 'log1p',
                            factor  : 1.0,
                            missing : 0
                        ],
                        boost_mode        : 'sum'
                    ]
                ]
            ],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Test field filter'() {
    given:
    def request = """{"filters": [{"type": "field", "name": "$field", "value": "$value"}]}"""
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [
                    term: [
                        (field): value
                    ]
                ]
            ]
        ]
    ]

    then:
    queryResult == expectedQuery

    where:
    field        | value
    'fileFormat' | 'NetCDF'
  }

  def 'Test field filter with wildcard'() {
    given:
    def request = """{"filters": [{"type": "field", "name": "$field", "value": "$value", "exactMatch": false}]}"""
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [
                    wildcard: [
                        (field): value
                    ]
                ]
            ]
        ]
    ]

    then:
    queryResult == expectedQuery

    where:
    field        | value
    'fileFormat' | 'Net*'
  }

  def 'Test field filter with nested field'() {
    given:
    def request = """{"filters": [{"type": "field", "name": "${field}", "value": "${value}"}]}"""
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [
                    nested: [
                        path : 'links',
                        query: [
                            term: [
                                (field): value
                            ]
                        ]
                    ]
                ]
            ]
        ]
    ]

    then:
    queryResult == expectedQuery

    where:
    field           | value
    'links.linkUrl' | 'http://foo.bar.com/csv/baz.csv'
  }

  def 'Default datetime filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type":"datetime","before":"2011-11-11", "after":"2010-10-10"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [bool: [
                    minimum_should_match: 1,
                    should              : [
                        [bool: [
                            must: [
                                [range: [beginDate: [lte: '2011-11-11']]],
                                [range: [endDate: [gte: '2010-10-10']]]
                            ]
                        ]],
                        [bool: [
                            must    : [
                                [range: [endDate: [gte: '2010-10-10']]]
                            ],
                            must_not: [
                                [exists: [field: 'beginDate']]
                            ]
                        ]],
                        [bool: [
                            must    : [
                                [range: [beginDate: [lte: '2011-11-11']]]
                            ],
                            must_not: [
                                [exists: [field: 'endDate']]
                            ]
                        ]]
                    ]
                ]]
            ]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Geopoint filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "contains", "geometry": {"type": "Point", "coordinates": [67.89, 12.345]}}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [geo_shape: [
                    spatialBounding: [
                        shape   : [
                            type       : 'Point',
                            coordinates: [67.89, 12.345]
                        ],
                        relation: 'contains'
                    ]
                ]]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Bbox filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type": "geometry", "relation": "disjoint", "geometry":' +
        '  {"type": "Polygon", "coordinates": [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]}' +
        '}]}'
    def params = slurper.parseText(request)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [geo_shape: [
                    spatialBounding: [
                        shape   : [
                            type       : 'Polygon',
                            coordinates: [[[-5.99, 45.99], [-5.99, 36.49], [36.49, 30.01], [36.49, 45.99], [-5.99, 45.99]]]
                        ],
                        relation: 'disjoint'
                    ]
                ]]
            ]]
    ]

    when:
    def queryResult = requestParser.parseSearchQuery(params)

    then:
    queryResult == expectedQuery
  }

  def 'Collection filter request generates expected elasticsearch query'() {
    given:
    def request = '{"filters":[{"type":"collection","values":["TESTID"]}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [terms: [
                    internalParentIdentifier: ["TESTID"] as Set
                ]]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Collection filters ignore repeats and force a union query'() {
    given:
    def request = '{"filters":[{"type":"collection","values":["TESTID1"]},{"type":"collection","values":["TESTID1","TESTID2"]}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [terms: [
                    internalParentIdentifier: ["TESTID1", "TESTID2"] as Set
                ]]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Facet filter request includes filter in query'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"science","values":["Atmosphere > Aerosols", "Atmosphere > Spaceships"]}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [terms: [
                    gcmdScience: ["Atmosphere > Aerosols", "Atmosphere > Spaceships"]
                ]]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Filter request includes exclude global filter in query when true'() {
    given:
    def request = '{"filters":[{"type": "excludeGlobal", "value": true}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [term: [
                    isGlobal: false
                ]]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Filter request doesn\'t include exclude global filter in query when false'() {
    given:
    def request = '{"filters":[{"type": "excludeGlobal", "value": false}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [:]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Filter request correctly parses nested checksum queries'() {
    given:
    def request = '{"filters":[{"type": "checksum", "values": ["387cfb0cffbe6ec4547b7df61af8987126a9cae8"], "algorithm": "SHA1"}, {"type": "checksum", "values": ["970cfb0cffbe6ec4547b7df61af8987126a9cae8"], "algorithm": "MD5"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [
                    "nested": [
                        "path" : "checksums",
                        "query": [
                            "bool": [
                                "must": [
                                    ["terms": ["checksums.algorithm": ["SHA1"]]],
                                    ["terms": ["checksums.value": ["387cfb0cffbe6ec4547b7df61af8987126a9cae8"]]]
                                ]
                            ]
                        ]
                    ]
                ],
                [
                    "nested": [
                        "path" : "checksums",
                        "query": [
                            "bool": [
                                "must": [
                                    ["terms": ["checksums.algorithm": ["MD5"]]],
                                    ["terms": ["checksums.value": ["970cfb0cffbe6ec4547b7df61af8987126a9cae8"]]]
                                ]
                            ]
                        ]
                    ]
                ]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Facet filter request for a nested field'() {
    given:
    def request = '{"filters":[{"type":"facet","name":"linkAccessTypes","values":["download"]}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [nested: [
                    path : 'links',
                    query: [
                        terms: [
                            'links.linkFunction': ["download"]
                        ]
                    ]
                ]]
            ]
        ]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Fool proof checksum filter'() {
    given:
    def request = '{"filters":[{"type": "checksum", "values": ["387cfb0cffbe6ec4547b7df61af8987126a9cae8"], "algorithm": "SHA1"}, {"type": "checksum", "values": ["387cfb0cffbe6ec4547b7df61af8987126a9cae8"], "algorithm": "SHA1"}]}'
    def params = slurper.parseText(request)

    when:
    def queryResult = requestParser.parseSearchQuery(params)
    def expectedQuery = [
        bool: [
            must  : [:],
            filter: [
                [
                    "nested": [
                        "path" : "checksums",
                        "query": [
                            "bool": [
                                "must": [
                                    ["terms": ["checksums.algorithm": ["SHA1"]]],
                                    ["terms": ["checksums.value": ["387cfb0cffbe6ec4547b7df61af8987126a9cae8"]]]
                                ]
                            ]
                        ]
                    ]
                ]
            ]]
    ]

    then:
    queryResult == expectedQuery
  }

  def 'Default GCMD aggregations are built for granules'() {
    when:
    def aggsResult = requestParser.createFacetAggregations()
    def expectedAggs = [
        dataFormats         : [
            terms: [
                field: 'dataFormat',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        linkProtocols       : [
            nested      : [
                path: 'links'
            ],
            aggregations: [
                foobar: [
                    terms: [
                        field: 'links.linkProtocol',
                        size : 10000,
                        order: ['_term': 'asc']
                    ]
                ]
            ]
        ],
        serviceLinkProtocols: [
            terms: [
                field: 'serviceLinkProtocol',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        science             : [
            terms: [
                field: 'gcmdScience',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        services            : [
            terms: [
                field: 'gcmdScienceServices',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        locations           : [
            terms: [
                field: 'gcmdLocations',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        instruments         : [
            terms: [
                field: 'gcmdInstruments',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        platforms           : [
            terms: [
                field: 'gcmdPlatforms',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        projects            : [
            terms: [
                field: 'gcmdProjects',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        dataCenters         : [
            terms: [
                field: 'gcmdDataCenters',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        horizontalResolution: [
            terms: [
                field: 'gcmdHorizontalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        verticalResolution  : [
            terms: [
                field: 'gcmdVerticalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        temporalResolution  : [
            terms: [
                field: 'gcmdTemporalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        fileFormats         : [
            terms: [
                field: 'fileFormat',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        linkAccessTypes     : [
            nested      : [
                path: 'links'
            ],
            aggregations: [
                foobar: [
                    terms: [
                        field: 'links.linkFunction',
                        size : 10000,
                        order: ['_term': 'asc']
                    ]
                ]
            ]
        ]
    ]

    then:
    aggsResult == expectedAggs
  }

  def 'Default GCMD aggregations are built for collections'() {
    when:
    def aggsResult = requestParser.createFacetAggregations()
    def expectedAggs = [
        dataFormats         : [
            terms: [
                field: 'dataFormat',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        linkProtocols       : [
            nested      : [
                path: 'links'
            ],
            aggregations: [
                foobar: [
                    terms: [
                        field: 'links.linkProtocol',
                        size : 10000,
                        order: ['_term': 'asc']
                    ]
                ]
            ]
        ],
        serviceLinkProtocols: [
            terms: [
                field: 'serviceLinkProtocol',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        science             : [
            terms: [
                field: 'gcmdScience',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        services            : [
            terms: [
                field: 'gcmdScienceServices',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        locations           : [
            terms: [
                field: 'gcmdLocations',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        instruments         : [
            terms: [
                field: 'gcmdInstruments',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        platforms           : [
            terms: [
                field: 'gcmdPlatforms',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        projects            : [
            terms: [
                field: 'gcmdProjects',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        dataCenters         : [
            terms: [
                field: 'gcmdDataCenters',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        horizontalResolution: [
            terms: [
                field: 'gcmdHorizontalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        verticalResolution  : [
            terms: [
                field: 'gcmdVerticalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        temporalResolution  : [
            terms: [
                field: 'gcmdTemporalResolution',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        fileFormats         : [
            terms: [
                field: 'fileFormat',
                size : 10000,
                order: ['_term': 'asc']
            ]
        ],
        linkAccessTypes     : [
            nested      : [
                path: 'links'
            ],
            aggregations: [
                foobar: [
                    terms: [
                        field: 'links.linkFunction',
                        size : 10000,
                        order: ['_term': 'asc']
                    ]
                ]
            ]
        ]
    ]

    then:
    aggsResult == expectedAggs
  }
}
