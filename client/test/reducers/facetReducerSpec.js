import '../specHelper'
import { expect } from 'chai'
import Immutable from 'immutable'
import facets from '../../src/search/facet/facetReducer'
import * as actions from '../../src/search/facet/FacetActions'

describe('Facet reducer', () => {
  let initState = Immutable.fromJS({
    allFacets: null,
    selectedFacets: {}
  })

  const facetsRecAction = {
    type:"FACETS_RECEIVED",
    metadata:{
      facets:{
        science:{
          Oceans:{
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
    type:"MODIFY_SELECTED_FACETS",
    selectedFacets:{
      science:{
        Oceans:{
          selected:true
        },
        "Oceans > Ocean Temperature":{
          selected:true
        }
      },
      instruments:{
        "Earth Remote Sensing Instruments > Passive Remote Sensing > Spectrometers/Radiometers > Imaging Spectrometers/Radiometers > AVHRR-3 > Advanced Very High Resolution Radiometer-3":{
          selected:true
        }
      }
    }
  }
  const selectedFacets = Immutable.fromJS(modFacetsAction.selectedFacets)

  it('should return the initial state', () => {
    const reducerResp = facets(undefined, {})
    expect(reducerResp).to.deep.equal(initState)
  })

  it('should handle MODIFY_SELECTED_FACETS w/ facets selected', () => {
    const expectedState = initState.set('selectedFacets', selectedFacets)
    const reducerResp = facets(initState, modFacetsAction)
    expect(reducerResp).to.deep.equal(expectedState)
  })

  it('should handle MODIFY_SELECTED_FACETS w/ no facets selected', () => {
    let selectedFacets = Immutable.Map() // Override selected facets, set to empty obj
    const actionWithNoFacets = Object.assign({}, modFacetsAction,
      {selectedFacets: selectedFacets})
    const expectedState = initState.set('selectedFacets', selectedFacets)
    const reducerResp = facets(initState, actionWithNoFacets)
    expect(reducerResp).to.deep.equal(expectedState)
  })

  it('should handle FACETS_RECEIVED w/ flag set to process selected facets', () => {
    // First select some facets
    let stateWithFacets = facets(initState, modFacetsAction)
    // Receive new facets and merge in 'selected facets'
    const mergedFacets = facets(stateWithFacets, facetsRecAction)
    expect(mergedFacets).to.deep.include(selectedFacets)
  })

  it('should handle FACETS_RECEIVED w/ NO flag set to not process selected facets', () => {
    // First select some facets
    let stateWithFacets = facets(initState, modFacetsAction)
    // Receive new facets and wipe out 'selected facets'
    const newSearchFacets = Object.assign({}, facetsRecAction, {procSelectedFacets: false})
    console.log(JSON.stringify(newSearchFacets))
    const nonMergedFacets = facets(stateWithFacets, newSearchFacets)
    expect(nonMergedFacets).to.not.deep.include(selectedFacets)
  })
})
