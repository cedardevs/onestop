import Immutable from 'seamless-immutable'
import {
  collectionDetailFilter,
  initialState,
} from '../../../src/reducers/search/collectionDetailFilter'
import {collectionDetailRequested} from '../../../src/actions/routing/CollectionDetailStateActions'

const assertParam = (param, result, expected, fallback) => {
  expect(result[param]).toEqual(
    // if provided, assert that it matches `expected`, otherwise it should match `fallback`
    expected[param] === undefined ? fallback[param] : expected[param]
  )
}

const assertAllFilterParams = (results, values, defaults) => {
  // assertParam('queryText', results, values, defaults)
  assertParam('geoJSON', results, values, defaults)
  assertParam('startDateTime', results, values, defaults)
  assertParam('endDateTime', results, values, defaults)
  assertParam('selectedFacets', results, values, defaults)
  assertParam('excludeGlobal', results, values, defaults)
  assertParam('selectedCollectionIds', results, values, defaults)
}

describe('The collection detail filter reducer', function(){
  const nonInitialState = {
    // not a single default value
    queryText: 'demo',
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
    selectedCollectionIds: [ 'parent-uuid' ],
    geoJSON: null,
    startDateTime: null,
    endDateTime: null,
    selectedFacets: {},
    excludeGlobal: null,
  }

  it('has a default state', function(){
    const result = collectionDetailFilter(initialState, {})

    expect(result).toEqual({
      selectedCollectionIds: [],
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      excludeGlobal: null,
    })
  })

  const searchActionTestCases = [
    {
      desc: 'detail request action',
      its: [
        {
          name: 'sets selectedCollectionIds',
          initialState: initialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', {} ],
          expectedChanges: {selectedCollectionIds: [ 'parent-uuid' ]},
        },
        {
          name: 'resets to initial values with null param',
          initialState: nonInitialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', null ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'resets to initial values with undefined param',
          initialState: nonInitialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', undefined ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'resets to initial values with empty map',
          initialState: nonInitialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', {} ],
          expectedChanges: initialStateWithParentUuid,
        },
        {
          name: 'does not pass through queryText',
          initialState: initialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', {queryText: 'hello'} ],
          expectedChanges: {
            selectedCollectionIds: [ 'parent-uuid' ],
          },
        },
        {
          name:
            'resets to initial values on except where explicitly set (startDateTime)',
          initialState: nonInitialState,
          function: collectionDetailRequested,
          params: [ 'parent-uuid', {startDateTime: '2000-01-01T00:00:00Z'} ],
          expectedChanges: {
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
          function: collectionDetailRequested,
          params: [
            'parent-uuid',
            {
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
            },
          ],
          expectedChanges: {
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
          function: collectionDetailRequested,
          params: [
            'parent-uuid',
            {
              queryText: 'new',
              selectedFacets: {
                science: [ 'Atmosphere', 'Atmosphere > Aerosols' ],
              },
            },
          ],
          expectedChanges: {
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
          function: collectionDetailRequested,
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
          const result = collectionDetailFilter(
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
})
