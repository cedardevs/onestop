export const FACETS_RECEIVED = 'FACETS_RECEIVED'
export const UPDATE_FACETS = 'UPDATE_FACETS'
export const CLEAR_FACETS = 'CLEAR_FACETS'


export const facetsReceived = (metadata, processFacets = false) => {
  return {
    type: FACETS_RECEIVED,
    metadata,
    processFacets
  }
}

export const updateFacetsSelected = facetsSelected => {
  return {
    type: UPDATE_FACETS,
    facetsSelected
  }
}

export const clearFacets = () => {
  return {
    type: CLEAR_FACETS
  }
}
