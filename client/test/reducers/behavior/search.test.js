import Immutable from 'seamless-immutable'
import {search, initialState} from '../../../src/reducers/behavior/search'
import {
  updateSearch,
  newGeometry,
  removeGeometry,
  toggleSelection,
  toggleExcludeGlobal,
  clearSelections,
} from '../../../src/actions/search/collections/SearchParamActions'

describe('The search reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = search(initialState, initialAction)

    expect(result).toEqual({
      queryText: '',
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      selectedIds: [],
      excludeGlobal: null,
    })
  })

  describe('updateSearch cases', function(){
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
        selectedIds: [ 'ABCXYZ' ],
        excludeGlobal: true,
      }

      const updateAction = updateSearch(newSearchParams)
      const result = search(initialState, updateAction)
      expect(result).toEqual(newSearchParams)
    })

    it('defaults to initial state for missing fields', function(){
      const newSearchParams = {
        queryText: 'new',
      }

      const updateAction = updateSearch(newSearchParams)
      const result = search(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(initialState, newSearchParams))
    })

    it('works for empty or undefined params', function(){
      expect(search(initialState, updateSearch({}))).toEqual(initialState)
      expect(search(initialState, updateSearch(null))).toEqual(initialState)
      expect(search(initialState, updateSearch(undefined))).toEqual(
        initialState
      )
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
      const newGeomAction = newGeometry(validGeoJSON)
      const result = search(initialState, newGeomAction)
      expect(result.geoJSON).toEqual(validGeoJSON)
    })

    it('defaults back to initial state for geometry removal', function(){
      const removeGeomAction = removeGeometry()
      const result = search({geoJSON: validGeoJSON}, removeGeomAction)
      expect(result.geoJSON).toBeNull()
    })
  })

  describe('selected collections cases', function(){
    it('toggles selected collections', function(){
      const toggleA = toggleSelection('A')
      const toggleB = toggleSelection('B')
      // toggle A --> ['A']
      const addedAResult = search(initialState, toggleA)
      expect(addedAResult.selectedIds).toEqual([ 'A' ])
      // toggle B --> ['A', 'B']
      const addedBResult = search(addedAResult, toggleB)
      expect(addedBResult.selectedIds).toEqual([ 'A', 'B' ])
      // toggle A --> ['B']
      const removedAResult = search(addedBResult, toggleA)
      expect(removedAResult.selectedIds).toEqual([ 'B' ])
    })

    it('can clear existing collection selections', function(){
      const stateWithCollections = Immutable({selectedIds: [ 'ABC' ]})
      const result = search(stateWithCollections, clearSelections())
      expect(result.selectedIds).toEqual([])
    })
  })

  describe('facet cases', function(){
    it('should handle TOGGLE_FACET w/ facets selected', () => {
      const selectedFacets = {
        science: [ 'Oceans', 'Oceans > Ocean Temperature' ],
        instruments: [
          'Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3',
        ],
      }
      const modFacetsAction = {
        type: 'TOGGLE_FACET',
        selectedFacets: selectedFacets,
      }

      const reducerResp = search(initialState, modFacetsAction)
      expect(reducerResp.selectedFacets).toEqual(selectedFacets)
    })

    it('should handle TOGGLE_FACET w/ no facets selected', () => {
      const actionWithNoFacets = {type: 'TOGGLE_FACETS', selectedFacets: {}}
      const reducerResp = search(initialState, actionWithNoFacets)
      expect(reducerResp.selectedFacets).toEqual({})
    })
  })

  describe('toggleGlobal', function(){
    it('should handle TOGGLE_EXCLUDE_GLOBAL starting at null', () => {
      const toggleExcludeGlobalAction = {
        type: 'TOGGLE_EXCLUDE_GLOBAL',
      }

      const reducerResp = search(initialState, toggleExcludeGlobalAction)
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
    it('should handle TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at true', () => {
      const globalExcludedState = {
        excludeGlobal: true,
      }
      const toggleExcludeGlobalAction = {
        type: 'TOGGLE_EXCLUDE_GLOBAL',
      }

      const reducerResp = search(globalExcludedState, toggleExcludeGlobalAction)
      expect(reducerResp.excludeGlobal).toBeFalsy()
    })
    it('should handle TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at false', () => {
      const globalExcludedState = {
        excludeGlobal: false,
      }
      const toggleExcludeGlobalAction = {
        type: 'TOGGLE_EXCLUDE_GLOBAL',
      }

      const reducerResp = search(globalExcludedState, toggleExcludeGlobalAction)
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
  })
})
