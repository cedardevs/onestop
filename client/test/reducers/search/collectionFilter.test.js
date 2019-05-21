import Immutable from 'seamless-immutable'
import {
  collectionFilter,
  initialState,
} from '../../../src/reducers/search/collectionFilter'
import {
  collectionUpdateGeometry,
  collectionRemoveGeometry,
  collectionToggleExcludeGlobal,
  collectionToggleFacet,
  collectionNewSearchRequested,
  collectionNewSearchResetFiltersRequested,
  collectionMoreResultsRequested,
} from '../../../src/actions/routing/CollectionSearchStateActions'

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

    it('resets page offset (reset filter version)', function(){
      const pageState = {
        pageOffset: 60,
      }
      const result = collectionFilter(
        pageState,
        collectionNewSearchResetFiltersRequested({})
      )
      expect(result.pageOffset).toEqual(0)
    })
  })

  describe('collectionNewSearchRequested cases', function(){
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

      const updateAction = collectionNewSearchResetFiltersRequested(
        newSearchParams
      )
      const result = collectionFilter(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(newSearchParams, {pageOffset: 0}))
    })

    it('defaults to initial state for missing fields', function(){
      const newSearchParams = {
        queryText: 'new',
      }

      const updateAction = collectionNewSearchResetFiltersRequested(
        newSearchParams
      )
      const result = collectionFilter(initialState, updateAction)
      expect(result).toEqual(Immutable.merge(initialState, newSearchParams))
    })

    it('resets existing params to default', function(){
      const initialWithParams = {
        queryText: 'old',
        geoJSON: {
          type: 'Point',
          geometry: {type: 'Point', coordinates: [ 0, 0 ]},
        },
        startDateTime: '2000-01-01T00:00:00Z',
        endDateTime: '3000-01-01T00:00:00Z',
        selectedFacets: {science: [ 'Oceans' ]},
        excludeGlobal: true,
      }
      const newSearchParams = {
        queryText: 'new',
      }

      const updateAction = collectionNewSearchResetFiltersRequested(
        newSearchParams
      )
      const result = collectionFilter(initialWithParams, updateAction)
      expect(result.queryText).toEqual('new')
      expect(result.startDateTime).toBeNull()
      expect(result.endDateTime).toBeNull()
      expect(result.geoJSON).toBeNull()
      expect(result.selectedFacets).toEqual({})
      expect(result.excludeGlobal).toBeFalsy()
    })

    it('works for empty or undefined params', function(){
      expect(
        collectionFilter(
          initialState,
          collectionNewSearchResetFiltersRequested({})
        )
      ).toEqual(initialState)
      expect(
        collectionFilter(
          initialState,
          collectionNewSearchResetFiltersRequested(null)
        )
      ).toEqual(initialState)
      expect(
        collectionFilter(
          initialState,
          collectionNewSearchResetFiltersRequested(undefined)
        )
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
    it('should handle toggle a facet on', () => {
      const initialStateWithFacets = {
        selectedFacets: {
          science: [ 'Oceans' ],
        },
      }

      const result = collectionFilter(
        initialStateWithFacets,
        collectionToggleFacet('science', 'Oceans > Ocean Temperature', true)
      )
      expect(result.selectedFacets).toEqual({
        science: [ 'Oceans', 'Oceans > Ocean Temperature' ],
      })
    })

    it('should handle toggle a facet off', () => {
      const initialStateWithFacets = {
        selectedFacets: {science: [ 'Oceans', 'Oceans > Ocean Temperature' ]},
      }
      const result = collectionFilter(
        initialStateWithFacets,
        collectionToggleFacet('science', 'Oceans > Ocean Temperature', false)
      )
      expect(result.selectedFacets).toEqual({
        science: [ 'Oceans' ],
      })
    })
  })

  describe('toggleGlobal', function(){
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting at null', () => {
      const result = collectionFilter(
        initialState,
        collectionToggleExcludeGlobal()
      )
      expect(result.excludeGlobal).toBeTruthy()
    })
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at true', () => {
      const globalExcludedState = {
        excludeGlobal: true,
      }
      const result = collectionFilter(
        globalExcludedState,
        collectionToggleExcludeGlobal()
      )
      expect(result.excludeGlobal).toBeFalsy()
    })
    it('should handle COLLECTION_TOGGLE_EXCLUDE_GLOBAL starting with excludeGlobal at false', () => {
      const globalExcludedState = {
        excludeGlobal: false,
      }
      const result = collectionFilter(
        globalExcludedState,
        collectionToggleExcludeGlobal()
      )
      expect(result.excludeGlobal).toBeTruthy()
    })
  })
})
