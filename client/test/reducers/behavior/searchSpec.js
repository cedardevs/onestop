import '../../specHelper'
import Immutable from 'seamless-immutable'
import { expect } from 'chai'
import { search, initialState } from '../../../src/reducers/behavior/search'
import {
  updateSearch, newGeometry, removeGeometry, toggleSelection, clearSelections
} from '../../../src/actions/SearchParamActions'

describe('The search reducer', function () {
  it('has a default state', function () {
    const initialAction = {type: 'init'}
    const result = search(initialState, initialAction)

    result.should.deep.equal({
      queryText: '',
      geoJSON: null,
      startDateTime: null,
      endDateTime: null,
      selectedFacets: {},
      selectedIds: []
    })
  })

  describe('updateSearch cases', function () {
    it('updates all search params', function () {
      const newSearchParams = {
        queryText: 'new',
        geoJSON: {type: 'Point', geometry: {type: 'Point', coordinates: [0, 0]}},
        startDateTime: '2000-01-01T00:00:00Z',
        endDateTime: '3000-01-01T00:00:00Z',
        selectedFacets: {science: ["Oceans"]},
        selectedIds: ['ABCXYZ']
      }

      const updateAction = updateSearch(newSearchParams)
      const result = search(initialState, updateAction)
      result.should.deep.equal(newSearchParams)
    })

    it('defaults to initial state for missing fields', function () {
      const newSearchParams = {
        queryText: 'new'
      }

      const updateAction = updateSearch(newSearchParams)
      const result = search(initialState, updateAction)
      result.should.deep.equal(Immutable.merge(initialState, newSearchParams))
    })

    it('works for empty or undefined params', function () {
      search(initialState, updateSearch({})).should.deep.equal(initialState)
      search(initialState, updateSearch(null)).should.deep.equal(initialState)
      search(initialState, updateSearch(undefined)).should.deep.equal(initialState)
    })
  })

  describe('geometry cases', function () {
    const validGeoJSON = {
      type: 'Feature',
      geometry: {
        type: 'Polygon',
        coordinates: [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
      },
      properties: {
        description: 'Valid test GeoJSON'
      }
    }

    it('updates the state for a new geometry', function () {
      const newGeomAction = newGeometry(validGeoJSON)
      const result = search(initialState, newGeomAction)
      result.geoJSON.should.deep.equal(validGeoJSON)
    })

    it('defaults back to initial state for geometry removal', function () {
      const removeGeomAction = removeGeometry()
      const result = search({geoJSON: validGeoJSON}, removeGeomAction)
      expect(result.geoJSON).to.be.null
    })
  })

  describe('selected collections cases', function () {
    it('toggles selected collections', function () {
      const toggleA = toggleSelection('A')
      const toggleB = toggleSelection('B')
      // toggle A --> ['A']
      const addedAResult = search(initialState, toggleA)
      addedAResult.selectedIds.should.deep.equal(['A'])
      // toggle B --> ['A', 'B']
      const addedBResult = search(addedAResult, toggleB)
      addedBResult.selectedIds.should.deep.equal(['A', 'B'])
      // toggle A --> ['B']
      const removedAResult = search(addedBResult, toggleA)
      removedAResult.selectedIds.should.deep.equal(['B'])
    })

    it('can clear existing collection selections', function () {
      const stateWithCollections = Immutable({selectedIds: ['ABC']})
      const result = search(stateWithCollections, clearSelections())
      result.selectedIds.should.deep.equal([])
    })
  })

  describe('facet cases', function () {
    it('should handle TOGGLE_FACET w/ facets selected', () => {
      const selectedFacets = {
        science:["Oceans", "Oceans > Ocean Temperature"],
        instruments:["Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3"]
      }
      const modFacetsAction = {
        type:"TOGGLE_FACET",
        selectedFacets: selectedFacets
      }

      const reducerResp = search(initialState, modFacetsAction)
      reducerResp.selectedFacets.should.deep.equal(selectedFacets)
    })

    it('should handle TOGGLE_FACET w/ no facets selected', () => {
      const actionWithNoFacets = {type:"TOGGLE_FACETS", selectedFacets: {}}
      const reducerResp = search(initialState, actionWithNoFacets)
      reducerResp.selectedFacets.should.deep.equal({})
    })
  })
})
