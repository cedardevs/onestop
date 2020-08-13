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

export const COLLECTION_RESULTS_PAGE_REQUESTED =
  'COLLECTION_RESULTS_PAGE_REQUESTED'
export const collectionResultsPageRequested = (offset, max) => ({
  offset: offset,
  max: max,
  type: COLLECTION_RESULTS_PAGE_REQUESTED,
})

export const COLLECTION_NEW_SEARCH_RESULTS_RECEIVED =
  'COLLECTION_NEW_SEARCH_RESULTS_RECEIVED'
export const collectionNewSearchResultsReceived = (total, items, facets) => ({
  type: COLLECTION_NEW_SEARCH_RESULTS_RECEIVED,
  total: total,
  facets: facets,
  items: items,
})

export const COLLECTION_RESULTS_PAGE_RECEIVED =
  'COLLECTION_RESULTS_PAGE_RECEIVED'
export const collectionResultsPageReceived = (total, items, facets) => ({
  type: COLLECTION_RESULTS_PAGE_RECEIVED,
  items: items,
  total: total,
  facets: facets,
})

export const COLLECTION_SEARCH_ERROR = 'COLLECTION_SEARCH_ERROR'
export const collectionSearchError = errors => ({
  type: COLLECTION_SEARCH_ERROR,
  errors,
})

export const COLLECTION_UPDATE_GEOMETRY = 'COLLECTION_UPDATE_GEOMETRY'
export const collectionUpdateGeometry = bbox => {
  return {
    type: COLLECTION_UPDATE_GEOMETRY,
    bbox: bbox,
  }
}
export const COLLECTION_REMOVE_GEOMETRY = 'COLLECTION_REMOVE_GEOMETRY'
export const collectionRemoveGeometry = () => {
  return {
    type: COLLECTION_REMOVE_GEOMETRY,
  }
}

export const COLLECTION_UPDATE_GEO_RELATIONSHIP =
  'COLLECTION_UPDATE_GEO_RELATIONSHIP'
export const collectionUpdateGeoRelation = relationship => {
  return {
    type: COLLECTION_UPDATE_GEO_RELATIONSHIP,
    relationship: relationship,
  }
}

export const COLLECTION_UPDATE_TIME_RELATIONSHIP =
  'COLLECTION_UPDATE_TIME_RELATIONSHIP'
export const collectionUpdateTimeRelation = relationship => {
  return {
    type: COLLECTION_UPDATE_TIME_RELATIONSHIP,
    relationship: relationship,
  }
}

export const COLLECTION_UPDATE_YEAR_RANGE = 'COLLECTION_UPDATE_YEAR_RANGE'
export const collectionUpdateYearRange = (startYear, endYear) => {
  return {
    type: COLLECTION_UPDATE_YEAR_RANGE,
    startYear: startYear,
    endYear: endYear,
  }
}

export const COLLECTION_REMOVE_YEAR_RANGE = 'COLLECTION_REMOVE_YEAR_RANGE'
export const collectionRemoveYearRange = (startYear, endYear) => {
  return {
    type: COLLECTION_REMOVE_YEAR_RANGE,
    startYear: startYear,
    endYear: endYear,
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
