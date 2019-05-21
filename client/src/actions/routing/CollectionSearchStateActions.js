export const COLLECTION_NEW_SEARCH_REQUESTED = 'COLLECTION_NEW_SEARCH_REQUESTED'
export const collectionNewSearchRequested = () => ({
  type: COLLECTION_NEW_SEARCH_REQUESTED,
})

export const COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED =
  'COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED'
export const collectionNewSearchResetFiltersRequested = filters => ({
  type: COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  filters: filters,
})

export const COLLECTION_MORE_RESULTS_REQUESTED =
  'COLLECTION_MORE_RESULTS_REQUESTED'
export const collectionMoreResultsRequested = () => ({
  type: COLLECTION_MORE_RESULTS_REQUESTED,
})

export const COLLECTION_NEW_SEARCH_RESULTS_RECEIVED =
  'COLLECTION_NEW_SEARCH_RESULTS_RECEIVED'
export const collectionNewSearchResultsReceived = (total, items, facets) => ({
  type: COLLECTION_NEW_SEARCH_RESULTS_RECEIVED,
  total: total,
  facets: facets,
  items: items,
})

export const COLLECTION_MORE_RESULTS_RECEIVED =
  'COLLECTION_MORE_RESULTS_RECEIVED'
export const collectionMoreResultsReceived = items => ({
  type: COLLECTION_MORE_RESULTS_RECEIVED,
  items: items,
})

export const COLLECTION_SEARCH_ERROR = 'COLLECTION_SEARCH_ERROR'
export const collectionSearchError = errors => ({
  type: COLLECTION_SEARCH_ERROR,
  errors,
})

export const COLLECTION_CLEAR_FILTERS = 'COLLECTION_CLEAR_FILTERS'
export const collectionClearFilters = () => {
  return {
    type: COLLECTION_CLEAR_FILTERS,
  }
}

export const COLLECTION_UPDATE_QUERY_TEXT = 'COLLECTION_UPDATE_QUERY_TEXT'
export const collectionUpdateQueryText = queryText => {
  return {
    type: COLLECTION_UPDATE_QUERY_TEXT,
    queryText: queryText,
  }
}

export const COLLECTION_UPDATE_GEOMETRY = 'COLLECTION_UPDATE_GEOMETRY'
export const collectionUpdateGeometry = geoJSON => {
  return {
    type: COLLECTION_UPDATE_GEOMETRY,
    geoJSON: geoJSON,
  }
}
export const COLLECTION_REMOVE_GEOMETRY = 'COLLECTION_REMOVE_GEOMETRY'
export const collectionRemoveGeometry = () => {
  return {
    type: COLLECTION_REMOVE_GEOMETRY,
  }
}

export const COLLECTION_UPDATE_DATE_RANGE = 'COLLECTION_UPDATE_DATE_RANGE'
export const collectionUpdateDateRange = (startDate, endDate) => {
  return {
    type: COLLECTION_UPDATE_DATE_RANGE,
    startDate: startDate,
    endDate: endDate,
  }
}
export const COLLECTION_REMOVE_DATE_RANGE = 'COLLECTION_REMOVE_DATE_RANGE'
export const collectionRemoveDateRange = () => {
  return {
    type: COLLECTION_REMOVE_DATE_RANGE,
  }
}

export const COLLECTION_TOGGLE_FACET = 'COLLECTION_TOGGLE_FACET'
export const collectionToggleFacet = (category, facetName, selected) => {
  return {
    type: COLLECTION_TOGGLE_FACET,
    category: category,
    facetName: facetName,
    selected: selected,
  }
}

export const COLLECTION_TOGGLE_EXCLUDE_GLOBAL =
  'COLLECTION_TOGGLE_EXCLUDE_GLOBAL'
export const collectionToggleExcludeGlobal = () => {
  return {
    type: COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  }
}
