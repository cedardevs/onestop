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
  GRANULE_TOGGLE_FACET,
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
      queryText: '',
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
      }

      const updateAction = granuleUpdateFilters(newSearchParams)
      const result = granuleFilter(initialState, updateAction)
      expect(result).toEqual(
        Immutable.merge(newSearchParams, {selectedIds: [], pageOffset: 0})
      )
    })

    it('defaults to initial state for missing fields', function(){
      const newSearchParams = {
        queryText: 'new',
      }

      const updateAction = granuleUpdateFilters(newSearchParams)
      const result = granuleFilter(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(initialState, newSearchParams))
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
    it('should handle GRANULE_TOGGLE_FACET w/ facets selected', () => {
      const selectedFacets = {
        science: [ 'Oceans', 'Oceans > Ocean Temperature' ],
        instruments: [
          'Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3',
        ],
      }
      const modFacetsAction = {
        type: GRANULE_TOGGLE_FACET,
        selectedFacets: selectedFacets,
      }

      const reducerResp = granuleFilter(initialState, modFacetsAction)
      expect(reducerResp.selectedFacets).toEqual(selectedFacets)
    })

    it('should handle GRANULE_TOGGLE_FACET w/ no facets selected', () => {
      const actionWithNoFacets = {
        type: GRANULE_TOGGLE_FACET,
        selectedFacets: {},
      }
      const reducerResp = granuleFilter(initialState, actionWithNoFacets)
      expect(reducerResp.selectedFacets).toEqual({})
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
