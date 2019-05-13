export const COLLECTION_NEW_SEARCH_REQUESTED = 'COLLECTION_NEW_SEARCH_REQUESTED'
export const collectionNewSearchRequested = () => ({
  type: COLLECTION_NEW_SEARCH_REQUESTED,
})

export const COLLECTION_MORE_RESULTS_REQUESTED =
  'COLLECTION_MORE_RESULTS_REQUESTED'
export const collectionMoreResultsRequested = () => ({
  type: COLLECTION_MORE_RESULTS_REQUESTED,
})

export const COLLECTION_NEW_SEARCH_RESULTS_RECIEVED =
  'COLLECTION_NEW_SEARCH_RESULTS_RECIEVED'
export const collectionNewSearchResultsRecieved = (total, items, facets) => ({
  type: COLLECTION_NEW_SEARCH_RESULTS_RECIEVED,
  total: total,
  facets: facets,
  items: items,
})

export const COLLECTION_MORE_RESULTS_RECIEVED =
  'COLLECTION_MORE_RESULTS_RECIEVED'
export const collectionMoreResultsRecieved = items => ({
  type: COLLECTION_MORE_RESULTS_RECIEVED,
  items: items,
})

export const COLLECTION_SEARCH_ERROR = 'COLLECTION_SEARCH_ERROR'
export const collectionSearchError = errors => ({
  type: COLLECTION_SEARCH_ERROR,
  errors,
})
