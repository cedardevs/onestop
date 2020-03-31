import Immutable from 'seamless-immutable'
import * as queryUtils from '../../src/utils/queryUtils'
import {initialState} from '../../src/reducers/search/collectionFilter'
import {ROUTE} from '../../src/utils/urlUtils'
import {PAGE_SIZE} from '../../src/utils/queryUtils'

describe('The queryUtils', function(){
  describe('assembles collection requests', function(){
    collectionTestCases().forEach(function(testCase){
      it(`with ${testCase.name}`, function(){
        const objectResult = queryUtils.assembleSearchRequest(
          testCase.inputState,
          true
        )
        // const stringResult = queryUtils.assembleSearchRequestString(
        //   testCase.inputState,
        //   false,
        //   true
        // )
        expect(objectResult).toEqual(testCase.expectedResult)
        // expect(stringResult).toBe(JSON.stringify(testCase.expectedResult))
      })
    })
  })

  describe('assembles granule requests', function(){
    // TODO do the query test cases cover Next Page completely? I didn't actually look

    granuleTestCases().forEach(function(testCase){
      it(`with ${testCase.name}`, function(){
        const objectResult = queryUtils.assembleSearchRequest(
          testCase.inputState,
          false
        )
        // const stringResult = queryUtils.assembleSearchRequestString(
        //   testCase.inputState,
        //   true,
        //   false
        // )
        expect(objectResult).toEqual(testCase.expectedResult)
        // expect(stringResult).toBe(JSON.stringify(testCase.expectedResult))
      })
    })
  })

  describe('assembles granule count requests', function(){
    granuleCountTestCases().forEach(function(testCase){
      it(`with ${testCase.name}`, function(){
        const objectResult = queryUtils.assembleSearchRequest(
          testCase.inputState,
          false,
          0
        )
        expect(objectResult).toEqual(testCase.expectedResult)
      })
    })
  })

  describe('encodeLocationDescriptor', function(){
    it('correctly generates location from granules path with a selected id', function(){
      const result = queryUtils.encodeLocationDescriptor(ROUTE.granules, {
        selectedCollectionIds: [ 'ABC' ],
      })
      expect(result).toEqual({
        pathname: '/collections/granules/ABC',
        search: '',
      })
    })
  })

  describe('a queryString', function(){
    queryTestCases().forEach(testCase => {
      it(`encodes accurately with ${testCase.name}`, function(){
        const encodedString = queryUtils.encodeQueryString(testCase.state)
        expect(encodedString).toBe(testCase.string)
      })

      it(`decodes accurately with ${testCase.name}`, function(){
        const decodedString = queryUtils.decodeQueryString(testCase.string)
        expect(decodedString).toEqual(testCase.state)
      })
    })

    specialEncodingQueryTestCases().forEach(testCase => {
      it(`encodes accurately with ${testCase.name}`, function(){
        const encodedString = queryUtils.encodeQueryString(testCase.state)
        expect(encodedString).toBe(testCase.string)
      })
    })
  })
})

function collectionTestCases(){
  return [
    /*{
      name: 'defaults',
      inputState: {}, // TODO this is not a real initial state anyway... in a real case where we have bad input, this util should throw an error because paging is a min requirement...
      expectedResult: {
        queries: [], // this is also an unrealistic example because there are lots of places prior to this preventing no query...
        filters: [],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },*/
    {
      name: 'a text search',
      inputState: {
        queryText: 'test text',
        pageOffset: 0,
      },
      expectedResult: {
        queries: [
          {
            type: 'queryText',
            value: 'test text',
          },
        ],
        filters: [],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a temporal search',
      inputState: {
        startDateTime: '2017-01-01',
        endDateTime: '2017-01-20',
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'datetime',
            after: '2017-01-01',
            before: '2017-01-20',
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a temporal search (within relation)',
      inputState: {
        timeRelationship: 'within',
        startDateTime: '2017-01-01',
        endDateTime: '2017-01-20',
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'datetime',
            after: '2017-01-01',
            before: '2017-01-20',
            relation: 'within',
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a temporal (year) search',
      inputState: {
        startYear: -3000000,
        endYear: -10000,
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'year',
            after: -3000000,
            before: -10000,
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a temporal (year) search (contains relation)',
      inputState: {
        timeRelationship: 'contains',
        startYear: -3000000,
        endYear: -10000,
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'year',
            after: -3000000,
            before: -10000,
            relation: 'contains',
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search (default relation)',
      inputState: {
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search (intersects relation)',
      inputState: {
        geoRelationship: 'intersects',
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            relation: 'intersects',
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search (contains relation)',
      inputState: {
        geoRelationship: 'contains',
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            relation: 'contains',
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search (within relation)',
      inputState: {
        geoRelationship: 'within',
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            relation: 'within',
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search (disjoint relation)',
      inputState: {
        geoRelationship: 'disjoint',
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            relation: 'disjoint',
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search crossing the antimeridian shifts coordinates',
      inputState: {
        geoRelationship: 'intersects',
        bbox: {
          west: 120.0,
          north: 63.0,
          south: 0.0,
          east: 7.0,
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            relation: 'intersects',
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ -240.0, 0.0 ],
                  [ 7.0, 0.0 ],
                  [ 7.0, 63.0 ],
                  [ -240.0, 63.0 ],
                  [ -240.0, 0.0 ],
                ],
              ],
            },
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'a facet search',
      inputState: {
        selectedFacets: {
          science: [ 'Atmosphere' ],
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'facet',
            name: 'science',
            values: [ 'Atmosphere' ],
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'all filters applied',
      inputState: {
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        startDateTime: '2017-01-01',
        endDateTime: '2017-01-20',
        queryText: 'test text',
        selectedFacets: {
          science: [ 'Atmosphere' ],
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [
          {
            type: 'queryText',
            value: 'test text',
          },
        ],
        filters: [
          {
            type: 'facet',
            name: 'science',
            values: [ 'Atmosphere' ],
          },
          {
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
          {
            type: 'datetime',
            after: '2017-01-01',
            before: '2017-01-20',
          },
        ],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'more results requested', // TODO more results WITH filters?
      inputState: {
        pageOffset: 20,
      },
      expectedResult: {
        queries: [],
        filters: [],
        facets: true,
        page: {
          max: PAGE_SIZE,
          offset: 20,
        },
      },
    },
  ]
}

function granuleTestCases(){
  return [
    {
      name: 'one collection',
      inputState: {
        selectedCollectionIds: [ 'ABC123' ],
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'collection',
            values: [ 'ABC123' ],
          },
        ],
        facets: false,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections',
      inputState: {
        selectedCollectionIds: [ 'ABC123', 'XYZ789' ],
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'collection',
            values: [ 'ABC123', 'XYZ789' ],
          },
        ],
        facets: false,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections and a text query',
      inputState: {
        queryText: 'test',
        selectedCollectionIds: [ 'ABC123', 'XYZ789' ],
        pageOffset: 0,
      },
      expectedResult: {
        queries: [
          {
            type: 'queryText',
            value: 'test',
          },
        ],
        filters: [
          {
            type: 'collection',
            values: [ 'ABC123', 'XYZ789' ],
          },
        ],
        facets: false,
        page: {
          max: PAGE_SIZE,
          offset: 0,
        },
      },
    },
    {
      name: 'more results requested',
      inputState: {
        selectedCollectionIds: [], // TODO this isn't realistic for how we request more results
        pageOffset: 20,
      },
      expectedResult: {
        queries: [],
        filters: [],
        facets: false,
        page: {
          max: PAGE_SIZE,
          offset: 20,
        },
      },
    },
  ]
}

function granuleCountTestCases(){
  return [
    {
      name: 'one collection',
      inputState: {
        selectedCollectionIds: [ 'ABC123' ],
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'collection',
            values: [ 'ABC123' ],
          },
        ],
        facets: false,
        page: {
          max: 0,
          offset: 0,
        },
      },
    },
    {
      name: 'collection and filters',
      inputState: {
        selectedCollectionIds: [ 'ABC123', 'XYZ789' ],
        bbox: {
          west: 100.0,
          north: 1.0,
          south: 0.0,
          east: 101.0,
        },
        startDateTime: '2017-01-01',
        endDateTime: '2017-01-20',
        selectedFacets: {
          science: [ 'Atmosphere' ],
        },
        pageOffset: 0,
      },
      expectedResult: {
        queries: [],
        filters: [
          {
            type: 'facet',
            name: 'science',
            values: [ 'Atmosphere' ],
          },
          {
            type: 'geometry',
            geometry: {
              type: 'Polygon',
              coordinates: [
                [
                  [ 100.0, 0.0 ],
                  [ 101.0, 0.0 ],
                  [ 101.0, 1.0 ],
                  [ 100.0, 1.0 ],
                  [ 100.0, 0.0 ],
                ],
              ],
            },
          },
          {
            type: 'datetime',
            after: '2017-01-01',
            before: '2017-01-20',
          },
          {
            type: 'collection',
            values: [ 'ABC123', 'XYZ789' ],
          },
        ],
        facets: false,
        page: {
          max: 0,
          offset: 0,
        },
      },
    },
  ]
}

function specialEncodingQueryTestCases(){
  return [
    {
      name:
        'time relationship (intersects) does not encode without a date filter',
      string: '',
      state: Immutable.merge(initialState, {timeRelationship: 'intersects'}),
    },
    {
      name: 'time relationship (within) does not encode without a date filter',
      string: '',
      state: Immutable.merge(initialState, {timeRelationship: 'within'}),
    },
    {
      name:
        'time relationship (disjoint) does not encode without a date filter',
      string: '',
      state: Immutable.merge(initialState, {timeRelationship: 'disjoint'}),
    },
    {
      name:
        'time relationship (contains) does not encode without a date filter',
      string: '',
      state: Immutable.merge(initialState, {timeRelationship: 'contains'}),
    },
    {
      name:
        'geo relationship (intersects) does not encode without a geometry filter',
      string: '',
      state: Immutable.merge(initialState, {geoRelationship: 'intersects'}),
    },
    {
      name:
        'geo relationship (within) does not encode without a geometry filter',
      string: '',
      state: Immutable.merge(initialState, {geoRelationship: 'within'}),
    },
    {
      name:
        'geo relationship (disjoint) does not encode without a geometry filter',
      string: '',
      state: Immutable.merge(initialState, {geoRelationship: 'disjoint'}),
    },
    {
      name:
        'geo relationship (contains) does not encode without a geometry filter',
      string: '',
      state: Immutable.merge(initialState, {geoRelationship: 'contains'}),
    },
  ]
}
function queryTestCases(){
  return [
    {
      name: 'empty string - initial state',
      string: '',
      state: initialState,
    },
    {
      name: 'text query filter',
      string: 'q=ocean',
      state: Immutable.merge(initialState, {
        queryText: 'ocean',
      }),
    },
    {
      name: 'title filter',
      string: 't=surveyid',
      state: Immutable.merge(initialState, {
        title: 'surveyid',
      }),
    },
    {
      name: 'title filter with all terms must match false',
      string: 't=surveyid&tm=0',
      state: Immutable.merge(initialState, {
        title: 'surveyid',
        allTermsMustMatch: false,
      }),
    },
    {
      name: 'time relationship (intersects)',
      string: 'tr=i&s=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        timeRelationship: 'intersects',
        startDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      name: 'time relationship (within)',
      string: 'tr=w&e=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        timeRelationship: 'within',
        endDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      name: 'time relationship (disjoint)',
      string: 'tr=d&sy=-3000000',
      state: Immutable.merge(initialState, {
        timeRelationship: 'disjoint',
        startYear: -3000000,
      }),
    },
    {
      name: 'time relationship (contains)',
      string: 'tr=c&ey=-100000',
      state: Immutable.merge(initialState, {
        timeRelationship: 'contains',
        endYear: -100000,
      }),
    },
    {
      name: 'start date filter',
      string: 'tr=i&s=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        startDateTime: '2010-01-01T00:00:00Z',
        // intersects is the default timeRelationship, and is covered by initialState
      }),
    },
    {
      name: 'end date filter',
      string: 'tr=i&e=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        endDateTime: '2010-01-01T00:00:00Z',
        // intersects is the default timeRelationship, and is covered by initialState
      }),
    },
    {
      name: 'start year filter',
      string: 'tr=i&sy=-3000000',
      state: Immutable.merge(initialState, {
        startYear: -3000000,
        // intersects is the default timeRelationship, and is covered by initialState
      }),
    },
    {
      name: 'end year filter',
      string: 'tr=i&ey=-100000',
      state: Immutable.merge(initialState, {
        endYear: -100000,
        // intersects is the default timeRelationship, and is covered by initialState
      }),
    },
    // {
    //   name: 'selected ids filter',
    //   string: 'i=ABC,with%20a%20space',
    //   state: Immutable.merge(initialState, {
    //     selectedCollectionIds: [ 'ABC', 'with a space' ],
    //   }),
    // },
    {
      name: 'exclude global filter',
      string: 'eg=1',
      state: Immutable.merge(initialState, {
        excludeGlobal: true,
      }),
    },
    {
      name: 'selected facets filter',
      string:
        'f=science:Oceans,Oceans%20%3E%20Sea%20Surface%20Temperature;platforms:DEM%20%3E%20Digital%20Elevation%20Model',
      state: Immutable.merge(initialState, {
        selectedFacets: {
          science: [ 'Oceans', 'Oceans > Sea Surface Temperature' ],
          platforms: [ 'DEM > Digital Elevation Model' ],
        },
      }),
    },
    {
      name: 'geometry filter',
      string: 'g=-83.9531,29.234,-70.5938,38.5527&gr=i', // defaults to intersects relation
      state: Immutable.merge(initialState, {
        bbox: {
          west: -83.9531,
          south: 29.234,
          east: -70.5938,
          north: 38.5527,
        },
      }),
    },
    {
      name: 'geo relationship (intersects)',
      string: 'g=-83,29,-70,38&gr=i', // 1-> 83, 2 -> 29, 3 -> 70, 4 ->38
      state: Immutable.merge(initialState, {
        geoRelationship: 'intersects',
        bbox: {
          west: -83,
          south: 29,
          east: -70,
          north: 38,
        },
      }),
    },
    {
      name: 'geo relationship (contains)',
      string: 'g=-83,29,-70,38&gr=c',
      state: Immutable.merge(initialState, {
        geoRelationship: 'contains',
        bbox: {
          west: -83,
          south: 29,
          east: -70,
          north: 38,
        },
      }),
    },
    {
      name: 'geo relationship (within)',
      string: 'g=-83,29,-70,38&gr=w',
      state: Immutable.merge(initialState, {
        geoRelationship: 'within',
        bbox: {
          west: -83,
          south: 29,
          east: -70,
          north: 38,
        },
      }),
    },
    {
      name: 'geo relationship (disjoint)',
      string: 'g=-83,29,-70,38&gr=d',
      state: Immutable.merge(initialState, {
        geoRelationship: 'disjoint',
        bbox: {
          west: -83,
          south: 29,
          east: -70,
          north: 38,
        },
      }),
    },
    {
      name: 'all types of filters',
      string:
        'q=ocean&g=-83,29,-70,38&gr=c&tr=i&s=2010-01-01T00%3A00%3A00Z&e=2010-01-01T00%3A00%3A00Z&f=platforms:DEM%20%3E%20Digital%20Elevation%20Model&eg=1',
      state: Immutable.merge(initialState, {
        queryText: 'ocean',
        startDateTime: '2010-01-01T00:00:00Z',
        endDateTime: '2010-01-01T00:00:00Z',
        // selectedCollectionIds: [ 'ABC' ],
        excludeGlobal: true,
        selectedFacets: {
          platforms: [ 'DEM > Digital Elevation Model' ],
        },
        geoRelationship: 'contains',
        bbox: {
          west: -83,
          south: 29,
          east: -70,
          north: 38,
        },
      }),
    },
  ]
}
