export const METADATA_RECEIVED = 'METADATA_RECEIVED'
export const UPDATE_FACETS = 'UPDATE_FACETS'
export const CLEAR_FACETS = 'CLEAR_FACETS'


export const processMetadata = metadata => {
  return {
    type: METADATA_RECEIVED,
    metadata
  }
}

export const updateFacetsSelected = facet => {
  return {
    type: UPDATE_FACETS,
    facet
  }
}

export const clearFacets = () => {
  return {
    type: CLEAR_FACETS
  }
}
