import Immutable from 'seamless-immutable'
import * as queryUtils from '../../src/utils/queryUtils'
import {initialState} from '../../src/reducers/search/collectionFilter'

describe('The queryUtils', function(){
  describe('assembles collection requests', function(){
    collectionTestCases().forEach(function(testCase){
      it(`with ${testCase.name}`, function(){
        const objectResult = queryUtils.assembleSearchRequest(
          testCase.inputState,
          false,
          true
        )
        const stringResult = queryUtils.assembleSearchRequestString(
          testCase.inputState,
          false,
          true
        )
        expect(objectResult).toEqual(testCase.expectedResult)
        expect(stringResult).toBe(JSON.stringify(testCase.expectedResult))
      })
    })
  })

  describe('assembles granule requests', function(){
    granuleTestCases().forEach(function(testCase){
      it(`with ${testCase.name}`, function(){
        const objectResult = queryUtils.assembleSearchRequest(
          testCase.inputState,
          true,
          false
        )
        const stringResult = queryUtils.assembleSearchRequestString(
          testCase.inputState,
          true,
          false
        )
        expect(objectResult).toEqual(testCase.expectedResult)
        expect(stringResult).toBe(JSON.stringify(testCase.expectedResult))
      })
    })
  })

  it(`encodes & decodes a queryString accurately`, function(){
    queryTestCases().forEach(testCase => {
      const tempState = {search: {collectionFilter: testCase.state}}
      const encodedString = queryUtils.encodeQueryString(tempState)
      expect(encodedString).toBe(testCase.string)
      const decodedString = queryUtils.decodeQueryString(encodedString)
      expect(decodedString).toEqual(testCase.state)
    })
  })
})

function collectionTestCases(){
  return [
    {
      name: 'defaults',
      inputState: {},
      expectedResult: {
        queries: [],
        filters: [],
        facets: true,
        page: {
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'a text search',
      inputState: {
        behavior: {
          search: {
            queryText: 'test text',
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'a temporal search',
      inputState: {
        behavior: {
          search: {
            startDateTime: '2017-01-01',
            endDateTime: '2017-01-20',
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search',
      inputState: {
        behavior: {
          search: {
            geoJSON: {
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
              properties: {
                description: 'Valid test GeoJSON',
              },
            },
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'a facet search',
      inputState: {
        behavior: {
          search: {
            selectedFacets: {
              science: [ 'Atmosphere' ],
            },
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'all filters applied',
      inputState: {
        behavior: {
          search: {
            geoJSON: {
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
              properties: {
                description: 'Valid test GeoJSON',
              },
            },
            startDateTime: '2017-01-01',
            endDateTime: '2017-01-20',
            queryText: 'test text',
            selectedFacets: {
              science: [ 'Atmosphere' ],
            },
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'more results requested',
      inputState: {
        domain: {
          results: {
            collectionsPageOffset: 20,
          },
        },
      },
      expectedResult: {
        queries: [],
        filters: [],
        facets: true,
        page: {
          max: 20,
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
        behavior: {
          search: {
            selectedIds: [ 'ABC123' ],
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections',
      inputState: {
        behavior: {
          search: {
            selectedIds: [ 'ABC123', 'XYZ789' ],
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections and a text query',
      inputState: {
        behavior: {
          search: {
            queryText: 'test',
            selectedIds: [ 'ABC123', 'XYZ789' ],
          },
        },
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'more results requested',
      inputState: {
        behavior: {
          search: {
            selectedIds: [],
          },
        },
        domain: {
          results: {
            granulesPageOffset: 20,
          },
        },
      },
      expectedResult: {
        queries: [],
        filters: [],
        facets: false,
        page: {
          max: 20,
          offset: 20,
        },
      },
    },
  ]
}

function queryTestCases(){
  return [
    {
      string: '',
      state: initialState,
    },
    {
      string: 'q=ocean',
      state: Immutable.merge(initialState, {
        queryText: 'ocean',
      }),
    },
    {
      string: 's=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        startDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      string: 'e=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        endDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      string: 'i=ABC,with%20a%20space',
      state: Immutable.merge(initialState, {
        selectedIds: [ 'ABC', 'with a space' ],
      }),
    },
    {
      string: 'eg=1',
      state: Immutable.merge(initialState, {
        excludeGlobal: true,
      }),
    },
    {
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
      string: 'g=-83.9531,29.234,-70.5938,38.5527',
      state: Immutable.merge(initialState, {
        geoJSON: {
          type: 'Feature',
          properties: {},
          geometry: {
            type: 'Polygon',
            coordinates: [
              [
                [ -83.9531, 29.234 ],
                [ -70.5938, 29.234 ],
                [ -70.5938, 38.5527 ],
                [ -83.9531, 38.5527 ],
                [ -83.9531, 29.234 ],
              ],
            ],
          },
        },
      }),
    },
    {
      string:
        'q=ocean&g=-83,29,-70,38&s=2010-01-01T00%3A00%3A00Z&e=2010-01-01T00%3A00%3A00Z&f=platforms:DEM%20%3E%20Digital%20Elevation%20Model&i=ABC&eg=1',
      state: Immutable.merge(initialState, {
        queryText: 'ocean',
        startDateTime: '2010-01-01T00:00:00Z',
        endDateTime: '2010-01-01T00:00:00Z',
        selectedIds: [ 'ABC' ],
        excludeGlobal: true,
        selectedFacets: {
          platforms: [ 'DEM > Digital Elevation Model' ],
        },
        geoJSON: {
          type: 'Feature',
          properties: {},
          geometry: {
            type: 'Polygon',
            coordinates: [
              [
                [ -83, 29 ],
                [ -70, 29 ],
                [ -70, 38 ],
                [ -83, 38 ],
                [ -83, 29 ],
              ],
            ],
          },
        },
      }),
    },
  ]
}
