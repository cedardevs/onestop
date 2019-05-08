export const GRANULE_SEARCH_START = 'GRANULE_SEARCH_START'
export const granuleSearchStart = () => ({
  type: GRANULE_SEARCH_START,
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

export const GRANULE_INCREMENT_RESULTS_OFFSET =
  'GRANULE_INCREMENT_RESULTS_OFFSET'
export const granuleIncrementResultsOffset = () => ({
  type: GRANULE_INCREMENT_RESULTS_OFFSET,
})
