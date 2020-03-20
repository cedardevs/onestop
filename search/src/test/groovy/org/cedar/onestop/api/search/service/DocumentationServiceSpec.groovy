package org.cedar.onestop.api.search.service

import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
@Unroll
class DocumentationServiceSpec extends Specification {

  def 'generateAttributesInfo correctly identifies a non-queryable field'() {
    given:
    def input = [
        properties: [
            accessFeeStatement : [
                type : "keyword",
                index : false,
                doc_values : false
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        accessFeeStatement: [
            queryable: false,
            exactMatchRequired: null,
            applicableFilter: 'None'
        ]
    ]
  }

  def 'generateAttributesInfo correctly identifies a queryable field #field'() {
    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        (field): [
            queryable: true,
            exactMatchRequired: false,
            applicableFilter: 'None'
        ]

    ]

    where:
    field       | input
    'keywords1' | [properties: [(field): [type: 'text']]]
    'keywords2' | [properties: [(field): [type: 'text', index: true]]]
  }

  def 'generateAttributesInfo correctly identifies the filter that applies to field #field'() {
    given:
    def input = [
        properties: [
            (field): [
                type: 'keyword'
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        (field): [
            queryable: true,
            exactMatchRequired: true,
            applicableFilter: DocumentationService.filterableFields.get(field)
        ]
    ]

    where:
    field << DocumentationService.filterableFields.keySet()
  }

  def 'generateAttributesInfo correctly identifies when a field is exact match required'() {
    given:
    def input = [
        properties: [
            fileIdentifier: [
                type: 'keyword'
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        fileIdentifier: [
            queryable: true,
            exactMatchRequired: true,
            applicableFilter: 'None'
        ]
    ]
  }

  def 'generateAttributesInfo correctly identifies when a field is not exact match required'() {
    given:
    def input = [
        properties: [
            keywords: [
                type: 'text'
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        keywords: [
            queryable: true,
            exactMatchRequired: false,
            applicableFilter: 'None'
        ]
    ]
  }

  def 'generateAttributesInfo correctly generates info for a nested field'() {
    given:
    def input = [
        properties: [
            serviceLinks: [
                type: 'nested',
                properties: [
                    description: [
                        type: 'keyword',
                        index: false,
                        doc_values: false
                    ],
                    links: [
                        type: 'nested',
                        properties: [
                            linkDescription: [
                                type: 'keyword',
                                index: false,
                                doc_values: false
                            ]
                        ]
                    ],
                    title: [
                        type: 'keyword',
                        index: false,
                        doc_values: false
                    ]
                ]
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        'serviceLinks.description': [
          queryable: false,
          exactMatchRequired: null,
          applicableFilter: 'None'
        ],
        'serviceLinks.links.linkDescription': [
          queryable: false,
          exactMatchRequired: null,
          applicableFilter: 'None'
        ],
        'serviceLinks.title': [
            queryable: false,
            exactMatchRequired: null,
            applicableFilter: 'None'
        ]
    ]
  }

  def 'generateAttributesInfo correctly ignores alternative "fields" mappings'() {
    given:
    def input = [
        properties: [
            fileIdentifier: [
                type: 'text',
                fields: [
                    aggs: [
                        type: 'keyword'
                    ]
                ]
            ]
        ]
    ]

    when:
    Map result = DocumentationService.generateAttributesInfo(input)

    then:
    result == [
        fileIdentifier: [
            queryable: true,
            exactMatchRequired: false,
            applicableFilter: 'None'
        ]
    ]
  }
}
