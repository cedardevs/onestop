import '../specHelper'
import * as keywordUtils from '../../src/utils/keywordUtils'

describe('The keyword utils', function(){
  describe('title case util', function(){
    it('reduces screaming', function(){
      const testCases = [
        {
          input: 'TRMM Microwave Imager',
          output: 'TRMM Microwave Imager',
        },
        {
          input: 'TRMM > TRMM Microwave Imager',
          output: 'TRMM Microwave Imager',
        },
        {
          input: 'THERMOMETER',
          output: 'Thermometer',
        },
        {
          input: 'Thermistor',
          output: 'Thermistor',
        },
        {
          input: 'TEMPERATURE > Thermistor',
          output: 'Thermistor',
        },
        {
          input:
            'Ocean Carbon > Biogeochemistry; Southern Ocean Iron Experiment (OCB-SOFeX)',
          output: 'Biogeochemistry; Southern Ocean Iron Experiment (OCB-SOFeX)',
        },
      ]

      testCases.forEach(testCase => {
        const {input, output} = testCase
        keywordUtils
          .titleCaseKeyword(input)
          .should.equal(output, `Failed for input: ${input}`)
      })
    })
  })

  describe('hierarchy map util', function(){
    it('can build a hierarchy map with science theme data', function(){
      const input = {
        science: {
          Atmosphere: {count: 3},
          'Atmosphere > Air': {count: 2},
          'Atmosphere > Air > Particles': {count: 2},
          'Atmosphere > Wind': {count: 1},
          'Land Surface': {count: 2},
          'Land Surface > Topography': {count: 2},
          Oceans: {count: 1},
        },
      }

      const expected = {
        name: 'Data Theme',
        id: 'Data-Theme',
        keywordFacets: [
          {
            count: 3,
            category: 'science',
            term: 'Atmosphere',
            id: 'science-Atmosphere',
            selected: false,
            termHierarchy: [],
            keyword: 'Atmosphere',
          },
          {
            count: 2,
            category: 'science',
            term: 'Atmosphere > Air',
            id: 'science-Atmosphere-Air',
            selected: false,
            termHierarchy: [ 'Atmosphere' ],
            keyword: 'Air',
          },
          {
            count: 2,
            category: 'science',
            term: 'Atmosphere > Air > Particles',
            id: 'science-Atmosphere-Air-Particles',
            selected: false,
            termHierarchy: [ 'Atmosphere', 'Air' ],
            keyword: 'Particles',
          },
          {
            count: 1,
            category: 'science',
            term: 'Atmosphere > Wind',
            id: 'science-Atmosphere-Wind',
            selected: false,
            termHierarchy: [ 'Atmosphere' ],
            keyword: 'Wind',
          },
          {
            count: 2,
            category: 'science',
            term: 'Land Surface',
            id: 'science-Land-Surface',
            selected: false,
            termHierarchy: [],
            keyword: 'Land Surface',
          },
          {
            count: 2,
            category: 'science',
            term: 'Land Surface > Topography',
            id: 'science-Land-Surface-Topography',
            selected: false,
            termHierarchy: [ 'Land Surface' ],
            keyword: 'Topography',
          },
          {
            count: 1,
            category: 'science',
            term: 'Oceans',
            id: 'science-Oceans',
            selected: false,
            termHierarchy: [],
            keyword: 'Oceans',
          },
        ],
        hierarchy: [
          {
            id: 'science-Atmosphere',
            children: [
              {
                id: 'science-Atmosphere-Air',
                children: [
                  {
                    id: 'science-Atmosphere-Air-Particles',
                    children: [],
                    parent: 'science-Atmosphere-Air',
                  },
                ],
                parent: 'science-Atmosphere',
              },
              {
                id: 'science-Atmosphere-Wind',
                children: [],
                parent: 'science-Atmosphere',
              },
            ],
            parent: null,
          },
          {
            id: 'science-Land-Surface',
            children: [
              {
                id: 'science-Land-Surface-Topography',
                children: [],
                parent: 'science-Land-Surface',
              },
            ],
            parent: null,
          },
          {
            id: 'science-Oceans',
            children: [],
            parent: null,
          },
        ],
      }
      const result = keywordUtils.buildKeywordHierarchyMap(input, {})
      result.length.should.equal(1)
      result[0].should.deep.equal(expected)
    })

    it('will not fail upon encountering bad science theme data', function(){
      const input = {
        science: {
          'Spectral/Engineering': {count: 10},
          'Spectral/Engineering >\t\t\t\t\t\t\tmicrowave': {count: 1},
          'Spectral/Engineering >\t\t\t\t\t\t\tmicrowave > Brightness Temperature': {
            count: 1,
          },
          'Spectral/Engineering > Microwave': {count: 6},
          'Spectral/Engineering > Microwave > Antenna Temperature': {count: 1},
          'Spectral/Engineering > Microwave > Brightness Temperature': {
            count: 6,
          },
        },
      }

      const expected = {
        name: 'Data Theme',
        id: 'Data-Theme',
        keywordFacets: [
          {
            count: 10,
            category: 'science',
            term: 'Spectral/Engineering',
            id: 'science-Spectral-Engineering',
            selected: false,
            termHierarchy: [],
            keyword: 'Spectral/Engineering',
          },
          {
            count: 1,
            category: 'science',
            term: 'Spectral/Engineering >\t\t\t\t\t\t\tmicrowave',
            id: 'science-Spectral-Engineering-microwave',
            selected: false,
            termHierarchy: [ 'Spectral/Engineering' ],
            keyword: 'microwave',
          },
          {
            count: 1,
            category: 'science',
            term:
              'Spectral/Engineering >\t\t\t\t\t\t\tmicrowave > Brightness Temperature',
            id: 'science-Spectral-Engineering-microwave-Brightness-Temperature',
            selected: false,
            termHierarchy: [ 'Spectral/Engineering', 'microwave' ],
            keyword: 'Brightness Temperature',
          },
          {
            count: 6,
            category: 'science',
            term: 'Spectral/Engineering > Microwave',
            id: 'science-Spectral-Engineering-Microwave',
            selected: false,
            termHierarchy: [ 'Spectral/Engineering' ],
            keyword: 'Microwave',
          },
          {
            count: 1,
            category: 'science',
            term: 'Spectral/Engineering > Microwave > Antenna Temperature',
            id: 'science-Spectral-Engineering-Microwave-Antenna-Temperature',
            selected: false,
            termHierarchy: [ 'Spectral/Engineering', 'Microwave' ],
            keyword: 'Antenna Temperature',
          },
          {
            count: 6,
            category: 'science',
            term: 'Spectral/Engineering > Microwave > Brightness Temperature',
            id: 'science-Spectral-Engineering-Microwave-Brightness-Temperature',
            selected: false,
            termHierarchy: [ 'Spectral/Engineering', 'Microwave' ],
            keyword: 'Brightness Temperature',
          },
        ],
        hierarchy: [
          {
            id: 'science-Spectral-Engineering',
            children: [
              {
                id: 'science-Spectral-Engineering-microwave',
                children: [
                  {
                    id:
                      'science-Spectral-Engineering-microwave-Brightness-Temperature',
                    children: [],
                    parent: 'science-Spectral-Engineering-microwave',
                  },
                ],
                parent: 'science-Spectral-Engineering',
              },
              {
                id: 'science-Spectral-Engineering-Microwave',
                children: [
                  {
                    id:
                      'science-Spectral-Engineering-Microwave-Antenna-Temperature',
                    children: [],
                    parent: 'science-Spectral-Engineering-Microwave',
                  },
                  {
                    id:
                      'science-Spectral-Engineering-Microwave-Brightness-Temperature',
                    children: [],
                    parent: 'science-Spectral-Engineering-Microwave',
                  },
                ],
                parent: 'science-Spectral-Engineering',
              },
            ],
            parent: null,
          },
        ],
      }

      const result = keywordUtils.buildKeywordHierarchyMap(input, {})
      result.length.should.equal(1)
      result[0].should.deep.equal(expected)
    })

    it('will handle non-science category data', function(){
      const input = {
        totesDifferentCategory: {
          'Short Name > Long Name': {count: 10},
          'TLA > Three Letter Acronym': {count: 5},
        },
      }

      const expected = {
        name: 'Totes Different Category',
        id: 'Totes-Different Category',
        keywordFacets: [
          {
            count: 10,
            category: 'totesDifferentCategory',
            term: 'Short Name > Long Name',
            id: 'totes-Different-Category-Short-Name-Long-Name',
            selected: false,
            termHierarchy: [],
            keyword: 'Short Name > Long Name',
          },
          {
            count: 5,
            category: 'totesDifferentCategory',
            term: 'TLA > Three Letter Acronym',
            id: 'totes-Different-Category-TLA-Three-Letter-Acronym',
            selected: false,
            termHierarchy: [],
            keyword: 'TLA > Three Letter Acronym',
          },
        ],
        hierarchy: [
          {
            id: 'totes-Different-Category-Short-Name-Long-Name',
            children: [],
            parent: null,
          },
          {
            id: 'totes-Different-Category-TLA-Three-Letter-Acronym',
            children: [],
            parent: null,
          },
        ],
      }

      const result = keywordUtils.buildKeywordHierarchyMap(input, {})
      result.length.should.equal(1)
      result[0].should.deep.equal(expected)
    })
  })
})
