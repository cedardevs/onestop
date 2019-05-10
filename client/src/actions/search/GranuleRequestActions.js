export const GRANULE_SEARCH_START = 'GRANULE_SEARCH_START'
export const granuleSearchStart = clearPreviousResults => ({
  type: GRANULE_SEARCH_START,
  clearPreviousResults: clearPreviousResults,
  incrementPageOffset: !clearPreviousResults,
})

export const GRANULE_SEARCH_COMPLETE = 'GRANULE_SEARCH_COMPLETE'
export const granuleSearchComplete = (
  clearPreviousResults,
  total,
  items,
  metadata
) => ({
  type: GRANULE_SEARCH_COMPLETE,
  clearPreviousResults: clearPreviousResults,
  total: total,
  metadata: metadata,
  items: items,
})

export const GRANULE_SEARCH_ERROR = 'GRANULE_SEARCH_ERROR'
export const granuleSearchError = errors => ({
  type: GRANULE_SEARCH_ERROR,
  errors,
})
