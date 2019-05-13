import Immutable from 'seamless-immutable'
import {
  collectionFilter,
  initialState,
} from '../../../src/reducers/search/collectionFilter'
import {
  collectionUpdateFilters,
  collectionUpdateGeometry,
  collectionRemoveGeometry,
  collectionToggleExcludeGlobal,
  COLLECTION_TOGGLE_FACET,
} from '../../../src/actions/search/CollectionFilterActions'
import {
  collectionNewSearchRequested,
  collectionMoreResultsRequested,
} from '../../../src/actions/search/CollectionRequestActions'

describe('The collection filter reducer', function(){
  it('has a default state', function(){
    const initialAction = {type: 'init'}
    const result = collectionFilter(initialState, initialAction)

    expect(result).toEqual({
      pageOffset: 0,
      queryText: '',
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      excludeGlobal: null,
    })
  })

  describe('pagination cases', function(){
    it('increments page offset from initial', function(){
      const result = collectionFilter(
        initialState,
        collectionMoreResultsRequested()
      )
      expect(result.pageOffset).toEqual(20)
    })

    it('increments page offset', function(){
      const pageState = {
        pageOffset: 40,
      }
      const result = collectionFilter(
        pageState,
        collectionMoreResultsRequested()
      )
      expect(result.pageOffset).toEqual(60)
    })

    it('resets page offset', function(){
      const pageState = {
        pageOffset: 60,
      }
      const result = collectionFilter(pageState, collectionNewSearchRequested())
      expect(result.pageOffset).toEqual(0)
    })
  })

  describe('collectionUpdateFilters cases', function(){
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

      const updateAction = collectionUpdateFilters(newSearchParams)
      const result = collectionFilter(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(newSearchParams, {pageOffset: 0}))
    })

    it('defaults to initial state for missing fields', function(){
      const newSearchParams = {
        queryText: 'new',
      }

      const updateAction = collectionUpdateFilters(newSearchParams)
      const result = collectionFilter(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(initialState, newSearchParams))
    })

    it('works for empty or undefined params', function(){
      expect(
        collectionFilter(initialState, collectionUpdateFilters({}))
      ).toEqual(initialState)
      expect(
        collectionFilter(initialState, collectionUpdateFilters(null))
      ).toEqual(initialState)
      expect(
        collectionFilter(initialState, collectionUpdateFilters(undefined))
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
      const newGeomAction = collectionUpdateGeometry(validGeoJSON)
      const result = collectionFilter(initialState, newGeomAction)
      expect(result.geoJSON).toEqual(validGeoJSON)
    })

    it('defaults back to initial state for geometry removal', function(){
      const removeGeomAction = collectionRemoveGeometry()
      const result = collectionFilter({geoJSON: validGeoJSON}, removeGeomAction)
      expect(result.geoJSON).toBeNull()
    })
  })

  describe('facet cases', function(){
    it('should handle COLLECTION_TOGGLE_FACET w/ facets selected', () => {
      const selectedFacets = {
        science: [ 'Oceans', 'Oceans > Ocean Temperature' ],
        instruments: [
          'Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3',
        ],
      }
      const modFacetsAction = {
        type: COLLECTION_TOGGLE_FACET,
        selectedFacets: selectedFacets,
      }

      const reducerResp = collectionFilter(initialState, modFacetsAction)
      expect(reducerResp.selectedFacets).toEqual(selectedFacets)
    })

    it('should handle COLLECTION_TOGGLE_FACET w/ no facets selected', () => {
      const actionWithNoFacets = {
        type: COLLECTION_TOGGLE_FACET,
        selectedFacets: {},
      }
      const reducerResp = collectionFilter(initialState, actionWithNoFacets)
      expect(reducerResp.selectedFacets).toEqual({})
    })
  })

  describe('toggleGlobal', function(){
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting at null', () => {
      const reducerResp = collectionFilter(
        initialState,
        collectionToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at true', () => {
      const globalExcludedState = {
        excludeGlobal: true,
      }
      const reducerResp = collectionFilter(
        globalExcludedState,
        collectionToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeFalsy()
    })
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at false', () => {
      const globalExcludedState = {
        excludeGlobal: false,
      }
      const reducerResp = collectionFilter(
        globalExcludedState,
        collectionToggleExcludeGlobal()
      )
      expect(reducerResp.excludeGlobal).toBeTruthy()
    })
  })
})
