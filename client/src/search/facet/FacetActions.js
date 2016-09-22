export const FACETS_RECEIVED = 'FACETS_RECEIVED'
export const MODIFY_SELECTED_FACETS = 'MODIFY_SELECTED_FACETS'
export const CLEAR_FACETS = 'CLEAR_FACETS'


export const facetsReceived = (metadata, procSelectedFacets = false) => {
  return {
    type: FACETS_RECEIVED,
    metadata,
    procSelectedFacets
  }
}

export const modifySelectedFacets = selectedFacets => {
  return {
    type: MODIFY_SELECTED_FACETS,
    selectedFacets
  }
}

export const clearFacets = () => {
  return {
    type: CLEAR_FACETS
  }
}
