export const GRANULE_NEW_SEARCH_REQUESTED = 'GRANULE_NEW_SEARCH_REQUESTED'
export const granuleNewSearchRequested = collectionId => ({
  // this indicates a granule search within a single collection
  type: GRANULE_NEW_SEARCH_REQUESTED,
  id: collectionId,
})

export const GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED =
  'GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED'
export const granuleNewSearchResetFiltersRequested = (
  collectionId,
  filters
) => ({
  // this indicates a granule search within a single collection
  type: GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  id: collectionId,
  filters: filters,
})

export const GRANULE_MORE_RESULTS_REQUESTED = 'GRANULE_MORE_RESULTS_REQUESTED'
export const granuleMoreResultsRequested = () => ({
  type: GRANULE_MORE_RESULTS_REQUESTED,
})

export const GRANULE_NEW_SEARCH_RESULTS_RECEIVED =
  'GRANULE_NEW_SEARCH_RESULTS_RECEIVED'
export const granuleNewSearchResultsReceived = (granules, facets, total) => ({
  type: GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  granules: granules,
  facets: facets,
  total: total,
})

export const GRANULE_MORE_RESULTS_RECEIVED = 'GRANULE_MORE_RESULTS_RECEIVED'
export const granuleMoreResultsReceived = granules => ({
  type: GRANULE_MORE_RESULTS_RECEIVED,
  granules: granules,
})

export const GRANULES_FOR_CART_REQUESTED = 'GRANULES_FOR_CART_REQUESTED'
export const granulesForCartRequested = () => ({
  type: GRANULES_FOR_CART_REQUESTED,
})

export const GRANULES_FOR_CART_RESULTS_RECEIVED =
  'GRANULES_FOR_CART_RESULTS_RECEIVED'
export const granulesForCartResultsReceived = (granules, total) => ({
  type: GRANULES_FOR_CART_RESULTS_RECEIVED,
  granules: granules,
  total: total,
})

export const GRANULES_FOR_CART_ERROR = 'GRANULES_FOR_CART_ERROR'
export const granulesForCartError = warning => ({
  type: GRANULES_FOR_CART_ERROR,
  warning: warning,
})

export const GRANULES_FOR_CART_CLEAR_ERROR = 'GRANULES_FOR_CART_CLEAR_ERROR'
export const granulesForCartClearError = () => ({
  type: GRANULES_FOR_CART_CLEAR_ERROR,
})

export const GRANULE_SEARCH_ERROR = 'GRANULE_SEARCH_ERROR'
export const granuleSearchError = errors => ({
  type: GRANULE_SEARCH_ERROR,
  errors,
})

export const GRANULE_SET_QUERY_TEXT = 'GRANULE_SET_QUERY_TEXT'
export const setGranuleQueryText = text => ({
  type: GRANULE_SET_QUERY_TEXT,
  text: text,
})
export const clearGranuleQueryText = () => ({
  type: GRANULE_SET_QUERY_TEXT,
  text: '',
})

export const GRANULE_UPDATE_GEOMETRY = 'GRANULE_UPDATE_GEOMETRY'
export const granuleUpdateGeometry = geoJSON => {
  return {
    type: GRANULE_UPDATE_GEOMETRY,
    geoJSON: geoJSON,
  }
}
export const GRANULE_REMOVE_GEOMETRY = 'GRANULE_REMOVE_GEOMETRY'
export const granuleRemoveGeometry = () => {
  return {
    type: GRANULE_REMOVE_GEOMETRY,
  }
}

export const GRANULE_UPDATE_DATE_RANGE = 'GRANULE_UPDATE_DATE_RANGE'
export const granuleUpdateDateRange = (startDate, endDate) => {
  return {
    type: GRANULE_UPDATE_DATE_RANGE,
    startDate: startDate,
    endDate: endDate,
  }
}
export const GRANULE_REMOVE_DATE_RANGE = 'GRANULE_REMOVE_DATE_RANGE'
export const granuleRemoveDateRange = () => {
  return {
    type: GRANULE_REMOVE_DATE_RANGE,
  }
}

export const GRANULE_TOGGLE_FACET = 'GRANULE_TOGGLE_FACET' // TODO rename, it's basically set (singular) facet to (selected)
export const granuleToggleFacet = (category, facetName, selected) => {
  return {
    type: GRANULE_TOGGLE_FACET,
    category: category,
    facetName: facetName,
    selected: selected,
  }
}

export const GRANULE_TOGGLE_EXCLUDE_GLOBAL = 'GRANULE_TOGGLE_EXCLUDE_GLOBAL'
export const granuleToggleExcludeGlobal = () => {
  return {
    type: GRANULE_TOGGLE_EXCLUDE_GLOBAL,
  }
}
