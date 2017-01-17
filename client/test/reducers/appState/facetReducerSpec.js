import '../../specHelper'
import _ from 'lodash'
import { expect, use } from 'chai'
import Immutable from 'seamless-immutable'
import { facets, initialState } from '../../../src/reducers/appState/facets'
import * as actions from '../../../src/search/facet/FacetActions'

describe('The facet reducer', () => {


  const modFacetsAction = {
    type:"TOGGLE_FACET",
    selectedFacets:{
      science:[
        "Oceans",
        "Oceans > Ocean Temperature"],
        instruments:[
          "Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3"]
        }
      }

  const selectedFacets = Immutable(modFacetsAction.selectedFacets)

  it('should return the initial state', () => {
    const reducerResp = facets(undefined, {})
    expect(reducerResp).to.deep.equal(initialState)
  })

  it('should handle TOGGLE_FACET w/ facets selected', () => {
    const expectedState = Immutable({selectedFacets: selectedFacets})
    const reducerResp = facets(initialState, modFacetsAction)
    expect(reducerResp).to.deep.equal(expectedState)
  })

  it('should handle TOGGLE_FACET w/ no facets selected', () => {
    let selectedFacets = {} // Override selected facets, set to empty obj
    const actionWithNoFacets = Object.assign({}, modFacetsAction,
      {selectedFacets: selectedFacets})
    const expectedState = Immutable({selectedFacets: selectedFacets})
    const reducerResp = facets(initialState, actionWithNoFacets)
    expect(reducerResp).to.deep.equal(expectedState)
  })

})
