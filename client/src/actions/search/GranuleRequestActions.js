export const GRANULE_NEW_SEARCH_REQUESTED = 'GRANULE_NEW_SEARCH_REQUESTED'
export const granuleNewSearchRequested = collectionId => ({
  // this indicates a granule search within a single collection
  type: GRANULE_NEW_SEARCH_REQUESTED,
  id: collectionId,
})

export const GRANULE_MORE_RESULTS_REQUESTED = 'GRANULE_MORE_RESULTS_REQUESTED'
export const granuleMoreResultsRequested = () => ({
  type: GRANULE_MORE_RESULTS_REQUESTED,
})

export const GRANULE_NEW_SEARCH_RESULTS_RECIEVED =
  'GRANULE_NEW_SEARCH_RESULTS_RECIEVED'
export const granuleNewSearchResultsRecieved = (total, items, facets) => ({
  type: GRANULE_NEW_SEARCH_RESULTS_RECIEVED,
  total: total,
  facets: facets,
  items: items,
})

export const GRANULE_MORE_RESULTS_RECIEVED = 'GRANULE_MORE_RESULTS_RECIEVED'
export const granuleMoreResultsRecieved = items => ({
  type: GRANULE_MORE_RESULTS_RECIEVED,
  items: items,
})

export const GRANULE_SEARCH_ERROR = 'GRANULE_SEARCH_ERROR'
export const granuleSearchError = errors => ({
  type: GRANULE_SEARCH_ERROR,
  errors,
})
