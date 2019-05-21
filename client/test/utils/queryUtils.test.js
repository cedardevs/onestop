import Immutable from 'seamless-immutable'
import * as queryUtils from '../../src/utils/queryUtils'
import {initialState} from '../../src/reducers/search/collectionFilter'

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

  describe(`a queryString`, function(){
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
          max: 20,
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
          max: 20,
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'a spatial search',
      inputState: {
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
          max: 20,
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'all filters applied',
      inputState: {
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
          max: 20,
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
        selectedIds: [ 'ABC123' ],
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections',
      inputState: {
        selectedIds: [ 'ABC123', 'XYZ789' ],
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'two collections and a text query',
      inputState: {
        queryText: 'test',
        selectedIds: [ 'ABC123', 'XYZ789' ],
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
          max: 20,
          offset: 0,
        },
      },
    },
    {
      name: 'more results requested',
      inputState: {
        selectedIds: [], // TODO this isn't realistic for how we request more results
        pageOffset: 20,
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

function granuleCountTestCases(){
  return [
    {
      name: 'one collection',
      inputState: {
        selectedIds: [ 'ABC123' ],
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
        selectedIds: [ 'ABC123', 'XYZ789' ],
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
      name: 'start date filter',
      string: 's=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        startDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      name: 'end date filter',
      string: 'e=2010-01-01T00%3A00%3A00Z',
      state: Immutable.merge(initialState, {
        endDateTime: '2010-01-01T00:00:00Z',
      }),
    },
    {
      name: 'selected ids filter',
      string: 'i=ABC,with%20a%20space',
      state: Immutable.merge(initialState, {
        selectedIds: [ 'ABC', 'with a space' ],
      }),
    },
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
      name: 'all types of filters',
      string:
        'q=ocean&g=-83,29,-70,38&s=2010-01-01T00%3A00%3A00Z&e=2010-01-01T00%3A00%3A00Z&f=platforms:DEM%20%3E%20Digital%20Elevation%20Model&eg=1&i=ABC',
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
