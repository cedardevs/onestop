import Immutable from 'seamless-immutable'
import _ from 'lodash'
export const FACETS_RECEIVED = 'FACETS_RECEIVED'
export const TOGGLE_FACET = 'TOGGLE_FACET'
export const CLEAR_FACETS = 'CLEAR_FACETS'


export const facetsReceived = (metadata) => {
  return {
    type: FACETS_RECEIVED,
    metadata
  }
}

export const toggleFacet = (category, facetName, selected) => {
  return (dispatch, getState) => {
    dispatch({
      type: TOGGLE_FACET,
      selectedFacets: updateSelectedFacets(getState().facets.selectedFacets, category, facetName, selected)
    })
  }
}

export const clearFacets = () => {
  return {
    type: CLEAR_FACETS
  }
}

const updateSelectedFacets = (selectedFacets, category, facetName, selected ) => {
  if (selected) {
    const newList = selectedFacets[category]
      && selectedFacets[category].concat([facetName])
      || [facetName]
    return Immutable.set(selectedFacets, category, newList)
  } else {
    let idx = selectedFacets[category].indexOf(facetName)
    let newList = selectedFacets[category]
     .slice(0, idx)
     .concat(selectedFacets[category].slice(idx + 1))
    return !_.isEmpty(newList) ? Immutable.set(selectedFacets, category, newList)
     : Immutable.without(selectedFacets, category)
  }
}
