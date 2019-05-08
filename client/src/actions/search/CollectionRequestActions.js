export const COLLECTION_SEARCH_START = 'COLLECTION_SEARCH_START'
export const collectionSearchStart = () => ({
  type: COLLECTION_SEARCH_START,
})

export const COLLECTION_SEARCH_COMPLETE = 'COLLECTION_SEARCH_COMPLETE'
export const collectionSearchComplete = (
  clearPreviousResults,
  total,
  items,
  metadata
) => ({
  type: COLLECTION_SEARCH_COMPLETE,
  clearPreviousResults: clearPreviousResults,
  total: total,
  metadata: metadata,
  items: items,
})

export const COLLECTION_SEARCH_ERROR = 'COLLECTION_SEARCH_ERROR'
export const collectionSearchError = errors => ({
  type: COLLECTION_SEARCH_ERROR,
  errors,
})
