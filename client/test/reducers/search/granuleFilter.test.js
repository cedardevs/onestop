import Immutable from 'seamless-immutable'
import {
  granuleFilter,
  initialState,
} from '../../../src/reducers/search/granuleFilter'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
  granuleUpdateDateRange,
  granuleRemoveDateRange,
  granuleToggleExcludeGlobal,
  granuleToggleFacet,
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleMoreResultsRequested,
} from '../../../src/actions/routing/GranuleSearchStateActions'

const assertParam = (param, result, expected, fallback) => {
  expect(result[param]).toEqual(
    // if provided, assert that it matches `expected`, otherwise it should match `fallback`
    expected[param] === undefined ? fallback[param] : expected[param]
  )
}

const assertAllFilterParams = (results, values, defaults) => {
  assertParam('pageOffset', results, values, defaults)
  assertParam('geoJSON', results, values, defaults)
  assertParam('startDateTime', results, values, defaults)
  assertParam('endDateTime', results, values, defaults)
  assertParam('selectedFacets', results, values, defaults)
  assertParam('excludeGlobal', results, values, defaults)
  assertParam('selectedCollectionIds', results, values, defaults)
  assertParam('title', results, values, defaults)
}

describe('The granule filter reducer', function(){
  const nonInitialState = {
    // not a single default value
    pageOffset: 40,
    title: 'demo',
    selectedCollectionIds: [ 'abc', '123' ],
    geoJSON: {
      type: 'Point',
      geometry: {type: 'Point', coordinates: [ 0, 0 ]},
    },
    startDateTime: '2000-01-01T00:00:00Z',
    endDateTime: '3000-01-01T00:00:00Z',
    selectedFacets: {science: [ 'Oceans', 'Oceans > Ocean Temperature' ]},
    excludeGlobal: true,
  }
  const initialStateWithParentUuid = {
    title: '',
    pageOffset: 0,
    selectedCollectionIds: [ 'parent-uuid' ],
    geoJSON: null,
    startDateTime: null,
    endDateTime: null,
    selectedFacets: {},
    excludeGlobal: null,
  }

  it('has a default state', function(){
    const result = granuleFilter(initialState, {})

    expect(result).toEqual({
      pageOffset: 0,
      selectedCollectionIds: [],
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      excludeGlobal: null,
      title: '',
    })
  })

  const searchActionTestCases = [
    {
      desc: 'simple new search request action',
      its: [
        {
          name: 'sets selectedCollectionIds',
          initialState: initialState,
          function: granuleNewSearchRequested,
          params: [ 'parent-uuid' ],
          expectedChanges: {selectedCollectionIds: [ 'parent-uuid' ]},
        },
        {
          name: 'resets only pageOffset and selectedCollectionIds',
          initialState: nonInitialState,
          function: granuleNewSearchRequested,
          params: [ 'parent-uuid' ],
          expectedChanges: {
            selectedCollectionIds: [ 'parent-uuid' ],
            pageOffset: 0,
          },
        },
      ],
    },
    {
      desc: 'next page search request action',
      its: [
        {
          name:
            'makes no changes to initial state except pagination (increments by 20)',
          initialState: initialState, // although this is a terrible request, with no filters
          function: granuleMoreResultsRequested,
          expectedChanges: {pageOffset: 20},
        },
        {
          name: 'changes only pageOffset (increments by 20)',
          initialState: nonInitialState,
          function: granuleMoreResultsRequested,
          expectedChanges: {pageOffset: 60},
        },
      ],
    },
    {
      desc: 'filter resetting new search',
      its: [
        {
          name: 'resets to initial values with null param',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', null ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'resets to initial values with undefined param',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', undefined ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'resets to initial values with empty map',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', {} ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'does not pass through queryText',
          initialState: initialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', {queryText: 'hello'} ],
          expectedChanges: {
            selectedCollectionIds: [ 'parent-uuid' ],
          },
        },
        {
          name:
            'resets to initial values on except where explicitly set (startDateTime)',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', {startDateTime: '2000-01-01T00:00:00Z'} ],
          expectedChanges: {
            title: '',
            pageOffset: 0,
            selectedCollectionIds: [ 'parent-uuid' ],
            geoJSON: null,
            startDateTime: '2000-01-01T00:00:00Z',
            endDateTime: null,
            selectedFacets: {},
            excludeGlobal: null,
          },
        },
        {
          name:
            'resets to initial values on except where explicitly set (selectedFacets)',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [
            'parent-uuid',
            {
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
            },
          ],
          expectedChanges: {
            title: '',
            pageOffset: 0,
            selectedCollectionIds: [ 'parent-uuid' ],
            geoJSON: null,
            startDateTime: null,
            endDateTime: null,
            selectedFacets: {
              science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
            },
            excludeGlobal: null,
          },
        },
        {
          name: 'sets multiple values',
          initialState: initialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [
            'parent-uuid',
            {
              title: 'new',
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
            },
          ],
          expectedChanges: {
            title: 'new',
            pageOffset: 0,
            selectedCollectionIds: [ 'parent-uuid' ],
            geoJSON: null,
            startDateTime: null,
            endDateTime: null,
            selectedFacets: {
              science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
            },
            excludeGlobal: null,
          },
        },
        {
          name: 'overwrites all values',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [
            'parent-uuid',
            {
              geoJSON: {
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
              startDateTime: '1998-01-01T00:00:00Z',
              endDateTime: '2020-01-01T00:00:00Z',
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
              excludeGlobal: false,
            },
          ],
          expectedChanges: {
            title: '',
            pageOffset: 0,
            selectedCollectionIds: [ 'parent-uuid' ],
            geoJSON: {
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
            startDateTime: '1998-01-01T00:00:00Z',
            endDateTime: '2020-01-01T00:00:00Z',
            selectedFacets: {
              science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
            },
            excludeGlobal: false,
          },
        },
      ],
    },
  ]

  searchActionTestCases.forEach(function(testBlock){
    describe(`${testBlock.desc}`, function(){
      testBlock.its.forEach(function(testCase){
        it(`${testCase.name}`, function(){
          const args = testCase.params || []
          const result = granuleFilter(
            testCase.initialState,
            testCase.function(...args)
          )
          assertAllFilterParams(
            result,
            testCase.expectedChanges,
            testCase.initialState
          )
        })
      })
    })
  })

  describe('individual filter value action', function(){
    const validGeoJSON = {
      type: 'Feature',
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
    }
    const paramActionTestCases = [
      {
        name: 'sets start date',
        initialState: initialState,
        function: granuleUpdateDateRange,
        params: [ '2017-01-01T00:00:00Z', null ],
        expectedChanges: {startDateTime: '2017-01-01T00:00:00Z'},
      },
      {
        name: 'sets end date',
        initialState: initialState,
        function: granuleUpdateDateRange,
        params: [ null, '2017-01-01T00:00:00Z' ],
        expectedChanges: {endDateTime: '2017-01-01T00:00:00Z'},
      },
      {
        name: 'sets date range',
        initialState: nonInitialState,
        function: granuleUpdateDateRange,
        params: [ '1990-01-01T00:00:00Z', '2017-01-01T00:00:00Z' ],
        expectedChanges: {
          startDateTime: '1990-01-01T00:00:00Z',
          endDateTime: '2017-01-01T00:00:00Z',
        },
      },
      {
        name: 'unsets date range',
        initialState: nonInitialState,
        function: granuleRemoveDateRange,
        expectedChanges: {startDateTime: null, endDateTime: null},
      },
      {
        name: 'sets geometry',
        initialState: initialState,
        function: granuleUpdateGeometry,
        params: [ validGeoJSON ],
        expectedChanges: {geoJSON: validGeoJSON},
      },
      {
        name: 'unsets geometry',
        initialState: nonInitialState,
        function: granuleRemoveGeometry,
        expectedChanges: {geoJSON: null},
      },
      {
        name: 'sets facet',
        initialState: initialState,
        function: granuleToggleFacet,
        params: [ 'science', 'Oceans > Ocean Temperature', true ],
        expectedChanges: {
          selectedFacets: {science: [ 'Oceans > Ocean Temperature' ]},
        },
      },
      {
        name: 'sets facet when some exist already',
        initialState: nonInitialState,
        function: granuleToggleFacet,
        params: [ 'science', 'Atmosphere', true ],
        expectedChanges: {
          selectedFacets: {
            science: [ 'Oceans', 'Oceans > Ocean Temperature', 'Atmosphere' ],
          },
        },
      },
      {
        name: 'unsets facet',
        initialState: nonInitialState,
        function: granuleToggleFacet,
        params: [ 'science', 'Oceans > Ocean Temperature', false ],
        expectedChanges: {
          selectedFacets: {science: [ 'Oceans' ]},
        },
      },
      {
        name: 'enable exclude global from default',
        initialState: initialState,
        function: granuleToggleExcludeGlobal,
        expectedChanges: {
          excludeGlobal: true,
        },
      },
      {
        name: 'disable exclude global',
        initialState: nonInitialState,
        function: granuleToggleExcludeGlobal,
        expectedChanges: {
          excludeGlobal: false,
        },
      },
      {
        name: 'enable exclude global from false',
        initialState: {
          pageOffset: 0,
          title: '',
          geoJSON: null,
          startDateTime: null,
          endDateTime: null,
          selectedFacets: {},
          excludeGlobal: false,
        },
        function: granuleToggleExcludeGlobal,
        expectedChanges: {
          excludeGlobal: true,
        },
      },
    ]
    paramActionTestCases.forEach(function(testCase){
      it(`${testCase.name}`, function(){
        const args = testCase.params || []
        const result = granuleFilter(
          testCase.initialState,
          testCase.function(...args)
        )
        assertAllFilterParams(
          result,
          testCase.expectedChanges,
          testCase.initialState
        )
      })
    })
  })
})
