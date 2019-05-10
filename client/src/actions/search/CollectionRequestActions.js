export const COLLECTION_SEARCH_START = 'COLLECTION_SEARCH_START'
export const collectionSearchStart = clearPreviousResults => ({
  type: COLLECTION_SEARCH_START,
  clearPreviousResults: clearPreviousResults,
  incrementPageOffset: !clearPreviousResults,
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
 // 
 // export const GRANULE_NEW_SEARCH_REQUESTED = 'GRANULE_NEW_SEARCH_REQUESTED'
 // export const granuleNewSearchRequested = () => ({
 //   type: GRANULE_NEW_SEARCH_REQUESTED,
 // })
 //
 // export const GRANULE_MORE_RESULTS_REQUESTED = 'GRANULE_MORE_RESULTS_REQUESTED'
 // export const granuleMoreResultsRequested = () => ({
 //   type: GRANULE_MORE_RESULTS_REQUESTED,
 // })
 //
 // export const GRANULE_NEW_SEARCH_RESULTS_RECIEVED =
 //   'GRANULE_NEW_SEARCH_RESULTS_RECIEVED'
 // export const granuleNewSearchResultsRecieved = (total, items, metadata) => ({
 //   type: GRANULE_NEW_SEARCH_RESULTS_RECIEVED,
 //   total: total,
 //   metadata: metadata,
 //   items: items,
 // })
 //
 // export const GRANULE_MORE_RESULTS_RECIEVED = 'GRANULE_MORE_RESULTS_RECIEVED'
 // export const granuleMoreResultsRecieved = (total, items) => ({
 //   type: GRANULE_MORE_RESULTS_RECIEVED,
 //   // TODO - I think the total is always unchanged, so no need to update it
 //   items: items,
 // })

export const COLLECTION_SEARCH_ERROR = 'COLLECTION_SEARCH_ERROR'
export const collectionSearchError = errors => ({
  type: COLLECTION_SEARCH_ERROR,
  errors,
})
