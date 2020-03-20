import Immutable from 'seamless-immutable'
import {
  granuleFilter,
  initialState,
} from '../../../src/reducers/search/granuleFilter'
import {
  granuleUpdateGeometry,
  granuleRemoveGeometry,
  granuleUpdateGeoRelation,
  granuleUpdateTimeRelation,
  granuleUpdateDateRange,
  granuleRemoveDateRange,
  granuleUpdateYearRange,
  granuleRemoveYearRange,
  granuleToggleExcludeGlobal,
  granuleToggleFacet,
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleResultsPageRequested,
  granuleToggleAllTermsMustMatch,
  resetGranuleAllTermsMustMatch,
} from '../../../src/actions/routing/GranuleSearchStateActions'

const assertParam = (param, result, expected, fallback) => {
  expect(result[param]).toEqual(
    // if provided, assert that it matches `expected`, otherwise it should match `fallback`
    expected[param] === undefined ? fallback[param] : expected[param]
  )
}

const assertAllFilterParams = (results, values, defaults) => {
  assertParam('pageOffset', results, values, defaults)
  assertParam('pageSize', results, values, defaults)
  assertParam('bbox', results, values, defaults)
  assertParam('geoRelationship', results, values, defaults)
  assertParam('timeRelationship', results, values, defaults)
  assertParam('startDateTime', results, values, defaults)
  assertParam('endDateTime', results, values, defaults)
  assertParam('startYear', results, values, defaults)
  assertParam('endYear', results, values, defaults)
  assertParam('selectedFacets', results, values, defaults)
  assertParam('excludeGlobal', results, values, defaults)
  assertParam('selectedCollectionIds', results, values, defaults)
  assertParam('title', results, values, defaults)
  assertParam('allTermsMustMatch', results, values, defaults)
}

describe('The granule filter reducer', function(){
  const nonInitialState = {
    // not a single default value
    pageOffset: 40,
    pageSize: 13,
    title: 'demo',
    allTermsMustMatch: false,
    selectedCollectionIds: [ 'abc', '123' ],
    bbox: {
      west: 123,
      north: 80,
      east: 23,
      south: -20,
    },
    geoRelationship: 'within',
    timeRelationship: 'disjoint',
    startDateTime: '2000-01-01T00:00:00Z',
    endDateTime: '3000-01-01T00:00:00Z',
    startYear: -30000,
    endYear: -3000,
    selectedFacets: {science: [ 'Oceans', 'Oceans > Ocean Temperature' ]},
    excludeGlobal: true,
  }
  const initialStateWithParentUuid = {
    title: '',
    allTermsMustMatch: true,
    pageOffset: 0,
    pageSize: 20,
    selectedCollectionIds: [ 'parent-uuid' ],
    bbox: null,
    geoRelationship: 'intersects',
    timeRelationship: 'intersects',
    startDateTime: null,
    endDateTime: null,
    startYear: null,
    endYear: null,
    selectedFacets: {},
    excludeGlobal: null,
  }

  it('has a default state', function(){
    const result = granuleFilter(initialState, {})

    expect(result).toEqual({
      pageOffset: 0,
      pageSize: 20,
      selectedCollectionIds: [],
      bbox: null,
      geoRelationship: 'intersects',
      timeRelationship: 'intersects',
      startDateTime: null,
      endDateTime: null,
      startYear: null,
      endYear: null,
      selectedFacets: {},
      excludeGlobal: null,
      title: '',
      allTermsMustMatch: true,
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
          name: 'resets only pageOffset, pageSize, and selectedCollectionIds',
          initialState: nonInitialState,
          function: granuleNewSearchRequested,
          params: [ 'parent-uuid' ],
          expectedChanges: {
            selectedCollectionIds: [ 'parent-uuid' ],
            pageOffset: 0,
            pageSize: 20,
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
          function: granuleResultsPageRequested,
          params: [ 20, 20 ], // default page size means no change
          expectedChanges: {pageOffset: 20},
        },
        {
          name: 'changes only pageOffset (increments by 20)',
          initialState: nonInitialState,
          function: granuleResultsPageRequested,
          params: [ 60, 20 ],
          expectedChanges: {pageOffset: 60, pageSize: 20},
        },
        {
          name: 'changes pageSize',
          initialState,
          function: granuleResultsPageRequested,
          params: [ 0, 15 ],
          expectedChanges: {pageSize: 15},
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
            'resets to initial values except where explicitly set (startDateTime)',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', {startDateTime: '2000-01-01T00:00:00Z'} ],
          expectedChanges: {
            title: '',
            allTermsMustMatch: true,
            pageOffset: 0,
            pageSize: 20,
            selectedCollectionIds: [ 'parent-uuid' ],
            bbox: null,
            geoRelationship: 'intersects',
            timeRelationship: 'intersects',
            startDateTime: '2000-01-01T00:00:00Z',
            endDateTime: null,
            startYear: null,
            endYear: null,
            selectedFacets: {},
            excludeGlobal: null,
          },
        },
        {
          name:
            'resets to initial values except where explicitly set (startYear)',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [ 'parent-uuid', {startYear: -100000000} ],
          expectedChanges: {
            title: '',
            allTermsMustMatch: true,
            pageOffset: 0,
            pageSize: 20,
            selectedCollectionIds: [ 'parent-uuid' ],
            bbox: null,
            geoRelationship: 'intersects',
            timeRelationship: 'intersects',
            startDateTime: null,
            endDateTime: null,
            startYear: -100000000,
            endYear: null,
            selectedFacets: {},
            excludeGlobal: null,
          },
        },
        {
          name:
            'resets to initial values except where explicitly set (selectedFacets)',
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
            allTermsMustMatch: true,
            pageOffset: 0,
            pageSize: 20,
            selectedCollectionIds: [ 'parent-uuid' ],
            bbox: null,
            geoRelationship: 'intersects',
            timeRelationship: 'intersects',
            startDateTime: null,
            endDateTime: null,
            startYear: null,
            endYear: null,
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
            selectedCollectionIds: [ 'parent-uuid' ],
            selectedFacets: {
              science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
            },
          },
        },
        {
          name: 'overwrites all values',
          initialState: nonInitialState,
          function: granuleNewSearchResetFiltersRequested,
          params: [
            'parent-uuid',
            {
              bbox: {
                west: 100.0,
                north: 1.0,
                south: 0.0,
                east: 101.0,
              },
              geoRelationship: 'contains',
              timeRelationship: 'contains',
              startDateTime: '1998-01-01T00:00:00Z',
              endDateTime: '2020-01-01T00:00:00Z',
              startYear: -100000000,
              endYear: -90000000,
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
              excludeGlobal: false,
            },
          ],
          expectedChanges: {
            title: '',
            allTermsMustMatch: true,
            pageOffset: 0,
            pageSize: 20,
            selectedCollectionIds: [ 'parent-uuid' ],
            bbox: {
              west: 100.0,
              north: 1.0,
              south: 0.0,
              east: 101.0,
            },
            geoRelationship: 'contains',
            timeRelationship: 'contains',
            startDateTime: '1998-01-01T00:00:00Z',
            endDateTime: '2020-01-01T00:00:00Z',
            startYear: -100000000,
            endYear: -90000000,
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
    const validBbox = {
      west: 100.0,
      north: 1.0,
      south: 0.0,
      east: 101.0,
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
        name: 'sets time relation',
        initialState: initialState,
        function: granuleUpdateTimeRelation,
        params: [ 'within' ],
        expectedChanges: {
          timeRelationship: 'within',
        },
      },
      {
        name: 'sets end date',
        initialState: initialState,
        function: granuleUpdateDateRange,
        params: [ null, '2017-01-01T00:00:00Z' ],
        expectedChanges: {endDateTime: '2017-01-01T00:00:00Z'},
      },
      {
        name: 'sets start year',
        initialState: initialState,
        function: granuleUpdateYearRange,
        params: [ -1000000, null ],
        expectedChanges: {startYear: -1000000},
      },
      {
        name: 'sets end year',
        initialState: initialState,
        function: granuleUpdateYearRange,
        params: [ null, -1000000 ],
        expectedChanges: {endYear: -1000000},
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
        name: 'sets year range',
        initialState: nonInitialState,
        function: granuleUpdateYearRange,
        params: [ -1000000, -900000 ],
        expectedChanges: {
          startYear: -1000000,
          endYear: -900000,
        },
      },
      {
        name: 'unsets date range',
        initialState: nonInitialState,
        function: granuleRemoveDateRange,
        expectedChanges: {startDateTime: null, endDateTime: null},
      },
      {
        name: 'unsets year range',
        initialState: nonInitialState,
        function: granuleRemoveYearRange,
        expectedChanges: {startYear: null, endYear: null},
      },
      {
        name: 'sets geometry',
        initialState: initialState,
        function: granuleUpdateGeometry,
        params: [ validBbox ],
        expectedChanges: {bbox: validBbox},
      },
      {
        name: 'unsets geometry',
        initialState: nonInitialState,
        function: granuleRemoveGeometry,
        expectedChanges: {bbox: null},
      },
      {
        name: 'sets geo relation',
        initialState: initialState,
        function: granuleUpdateGeoRelation,
        params: [ 'disjoint' ],
        expectedChanges: {
          geoRelationship: 'disjoint',
        },
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
        name: 'toggle all terms must match from default',
        initialState: initialState,
        function: granuleToggleAllTermsMustMatch,
        expectedChanges: {
          allTermsMustMatch: false,
        },
      },
      {
        name: 'toggle all terms must match from false',
        initialState: nonInitialState,
        function: granuleToggleAllTermsMustMatch,
        expectedChanges: {
          allTermsMustMatch: true,
        },
      },
      {
        name: 'reset all terms must match from default',
        initialState: initialState,
        function: resetGranuleAllTermsMustMatch,
        expectedChanges: {
          allTermsMustMatch: true,
        },
      },
      {
        name: 'reset all terms must match from false',
        initialState: initialState,
        function: resetGranuleAllTermsMustMatch,
        expectedChanges: {
          allTermsMustMatch: true,
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
          pageSize: 20,
          title: '',
          bbox: null,
          startDateTime: null,
          endDateTime: null,
          startYear: null,
          endYear: null,
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
