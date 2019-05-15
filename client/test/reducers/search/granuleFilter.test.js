import Immutable from 'seamless-immutable'
import {
  granuleFilter,
  initialState,
} from '../../../src/reducers/search/granuleFilter'
import {
  granuleUpdateFilters,
  granuleUpdateGeometry,
  granuleRemoveGeometry,
  granuleToggleExcludeGlobal,
  granuleToggleFacet,
} from '../../../src/actions/routing/GranuleSearchStateActions'
import {
  granuleNewSearchRequested,
  granuleMoreResultsRequested,
} from '../../../src/actions/routing/GranuleSearchStateActions'

describe('The granule filter reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = granuleFilter(initialState, initialAction)

    expect(result).toEqual({
      pageOffset: 0,
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      selectedIds: [],
      excludeGlobal: null,
    })
  })

  describe('pagination cases', function(){
    it('increments page offset from initial', function(){
      const result = granuleFilter(initialState, granuleMoreResultsRequested())
      expect(result.pageOffset).toEqual(20)
    })

    it('increments page offset', function(){
      const pageState = {
        pageOffset: 40,
      }
      const result = granuleFilter(pageState, granuleMoreResultsRequested())
      expect(result.pageOffset).toEqual(60)
    })

    it('resets page offset', function(){
      const pageState = {
        pageOffset: 60,
      }
      const result = granuleFilter(pageState, granuleNewSearchRequested())
      expect(result.pageOffset).toEqual(0)
    })
  })

  describe('granuleUpdateFilters cases', function(){
    it('updates all search params', function(){
      const newSearchParams = {
        queryText: 'new',
        geoJSON: {
          type: 'Point',
          geometry: {type: 'Point', coordinates: [ 0, 0 ]},
        },
        startDateTime: '2000-01-01T00:00:00Z',
        endDateTime: '3000-01-01T00:00:00Z',
        selectedFacets: {science: [ 'Oceans' ]},
        excludeGlobal: true,
        pageOffset: 33,
      } // TODO maybe just change this test to call it with the actual URL string, since that's more what it's used for?

      const result = granuleFilter(
        initialState,
        granuleUpdateFilters(newSearchParams)
      )
      // expect(result).toEqual(
      //   Immutable.merge(newSearchParams, {selectedIds: [], pageOffset: 0})
      // )
      expect(result.selectedIds).toEqual([]) // not changed because no new value provided
      expect(result.pageOffset).toEqual(0) // not set by update filter!
      expect(result.queryText).toBeUndefined() // not set by update filter!
      expect(result.geoJSON).toEqual(newSearchParams.geoJSON)
      expect(result.startDateTime).toEqual('2000-01-01T00:00:00Z')
      expect(result.endDateTime).toEqual('3000-01-01T00:00:00Z')
      expect(result.selectedFacets).toEqual(newSearchParams.selectedFacets)
      expect(result.excludeGlobal).toBeTruthy()
    })

    it('defaults to initial state for missing fields', function(){
      const newSearchParams = {
        endDateTime: '3000-01-01T00:00:00Z',
      }

      const updateAction = granuleUpdateFilters(newSearchParams)
      const result = granuleFilter(initialState, updateAction)

      expect(result.pageOffset).toEqual(initialState.pageOffset)
      expect(result.geoJSON).toEqual(initialState.geoJSON)
      expect(result.startDateTime).toEqual(initialState.startDateTime)
      expect(result.endDateTime).toEqual('3000-01-01T00:00:00Z')
      expect(result.selectedFacets).toEqual(initialState.selectedFacets)
      expect(result.selectedIds).toEqual(initialState.selectedIds)
      expect(result.excludeGlobal).toEqual(initialState.excludeGlobal)
    })

    it('works for empty or undefined params', function(){
      expect(granuleFilter(initialState, granuleUpdateFilters({}))).toEqual(
        initialState
      )
      expect(granuleFilter(initialState, granuleUpdateFilters(null))).toEqual(
        initialState
      )
      expect(
        granuleFilter(initialState, granuleUpdateFilters(undefined))
      ).toEqual(initialState)
    })

    it('replaces selectedIds', function(){
      const stateWithSelectedIds = {selectedIds: [ 'abc', '123' ]}
      const result = granuleFilter(
        stateWithSelectedIds,
        granuleUpdateFilters({endDateTime: '3000-01-01T00:00:00Z'})
      )
      expect(result.endDateTime).toEqual('3000-01-01T00:00:00Z')
      expect(result.selectedIds).toEqual([])
    })

    it('resets selectedIds', function(){
      const stateWithSelectedIds = {selectedIds: [ 'abc', '123' ]}
      const result = granuleFilter(
        stateWithSelectedIds,
        granuleUpdateFilters(null)
      )
      expect(result.selectedIds).toEqual([])
    })
  })

  describe('geometry cases', function(){
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

    it('updates the state for a new geometry', function(){
      const newGeomAction = granuleUpdateGeometry(validGeoJSON)
      const result = granuleFilter(initialState, newGeomAction)
      expect(result.geoJSON).toEqual(validGeoJSON)
    })

    it('defaults back to initial state for geometry removal', function(){
      const removeGeomAction = granuleRemoveGeometry()
      const result = granuleFilter({geoJSON: validGeoJSON}, removeGeomAction)
      expect(result.geoJSON).toBeNull()
    })
  })

  describe('selected granules cases', function(){
    it('sets selected collection id from initial state', function(){
      const selectCollectionA = granuleNewSearchRequested('A')

      const result = granuleFilter(initialState, selectCollectionA)
      expect(result.selectedIds).toEqual([ 'A' ])
    })

    it('updates selected collection id', function(){
      const selectCollectionB = granuleNewSearchRequested('B')
      const stateWithSelectedId = Immutable({selectedIds: [ 'A' ]})

      const result = granuleFilter(stateWithSelectedId, selectCollectionB)
      expect(result.selectedIds).toEqual([ 'B' ])
    })

    // it('can clear existing granule selections', function(){
    //   const stateWithSelectedId = Immutable({selectedIds: [ 'ABC' ]})
    //   const result = granuleFilter(
    //     stateWithSelectedId,
    //     granuleClearSelectedIds()
    //   )
    //   expect(result.selectedIds).toEqual([])
    // })
  })

  describe('facet cases', function(){
    it('should handle toggle a facet on', () => {
      const initialStateWithFacets = {
        selectedFacets: {
          science: [ 'Oceans' ],
        },
      }

      const result = granuleFilter(
        initialStateWithFacets,
        granuleToggleFacet('science', 'Oceans > Ocean Temperature', true)
      )
      expect(result.selectedFacets).toEqual({
        science: [ 'Oceans', 'Oceans > Ocean Temperature' ],
      })
    })

    it('should handle toggle a facet off', () => {
      const initialStateWithFacets = {
        selectedFacets: {science: [ 'Oceans', 'Oceans > Ocean Temperature' ]},
      }
      const result = granuleFilter(
        initialStateWithFacets,
        granuleToggleFacet('science', 'Oceans > Ocean Temperature', false)
      )
      expect(result.selectedFacets).toEqual({
        science: [ 'Oceans' ],
      })
    })
  })

  describe('toggleGlobal', function(){
    it('should handle GRANULE_TOGGLE_EXCLUDE_GLOBAL starting at null', () => {
      const reducerResp = granuleFilter(
        initialState,
        granuleToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
    it('should handle GRANULE_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at true', () => {
      const globalExcludedState = {
        excludeGlobal: true,
      }
      const reducerResp = granuleFilter(
        globalExcludedState,
        granuleToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeFalsy()
    })
    it('should handle GRANULE_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at false', () => {
      const globalExcludedState = {
        excludeGlobal: false,
      }
      const reducerResp = granuleFilter(
        globalExcludedState,
        granuleToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
  })
})
