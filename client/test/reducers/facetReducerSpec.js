import '../specHelper'
import _ from 'lodash'
import { expect, use } from 'chai'
import Immutable from 'seamless-immutable'
import facets from '../../src/search/facet/FacetReducer'
import * as actions from '../../src/search/facet/FacetActions'

describe('Facet reducer', () => {
  let initState = Immutable({
    allFacets: {},
    selectedFacets: {}
  })

  const facetsRecAction = {
    type:"FACETS_RECEIVED",
    metadata:{
      facets:{
        science:{
          "Oceans":{
            count:5
          },
          "Oceans > Ocean Temperature":{
            count:5
          },
          "Oceans > Ocean Temperature > Sea Surface Temperature":{
            count:5
          },
          dataResolution:{}
        }
      }
    },
    procSelectedFacets:true
  }

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
    expect(reducerResp).to.deep.equal(initState)
  })

  it('should handle TOGGLE_FACET w/ facets selected', () => {
    const expectedState = Immutable({selectedFacets: selectedFacets, allFacets: {}})
    const reducerResp = facets(initState, modFacetsAction)
    expect(reducerResp).to.deep.equal(expectedState)
  })

  it('should handle TOGGLE_FACET w/ no facets selected', () => {
    let selectedFacets = {} // Override selected facets, set to empty obj
    const actionWithNoFacets = Object.assign({}, modFacetsAction,
      {selectedFacets: selectedFacets})
    const expectedState = Immutable({selectedFacets: selectedFacets, allFacets: {}})
    const reducerResp = facets(initState, actionWithNoFacets)
    expect(reducerResp).to.deep.equal(expectedState)
  })

  it('should handle FACETS_RECEIVED', () => {
    let expectedState = { selectedFacets: {}, allFacets: {
      science:{
        "Oceans":{
          count:5
        },
        "Oceans > Ocean Temperature":{
          count:5
        },
        "Oceans > Ocean Temperature > Sea Surface Temperature":{
          count:5
        },
        dataResolution:{}
      }
    }
  }
  let stateWithFacets = facets(initState, facetsRecAction)
  expect(stateWithFacets).to.deep.equal(expectedState)
  })
})
