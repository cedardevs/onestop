import * as facetUtils from '../../src/utils/facetUtils'

describe('The facet utils', function(){
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
      const result = facetUtils.buildFilterHierarchyMap(input, {})
      expect(result.length).toBe(1)
      expect(result[0]).toEqual(expected)
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
            keyword: 'Long Name',
          },
          {
            count: 5,
            category: 'totesDifferentCategory',
            term: 'TLA > Three Letter Acronym',
            id: 'totes-Different-Category-TLA-Three-Letter-Acronym',
            selected: false,
            termHierarchy: [],
            keyword: 'Three Letter Acronym',
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

      const result = facetUtils.buildFilterHierarchyMap(input, {})
      expect(result.length).toBe(1)
      expect(result[0]).toEqual(expected)
    })
  })
})
