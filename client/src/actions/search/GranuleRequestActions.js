
export const GRANULE_NEW_SEARCH_START = 'GRANULE_NEW_SEARCH_START'
export const granuleNewSearchStart = () => ({
  type: GRANULE_NEW_SEARCH_START,
})
export const GRANULE_PAGE_SEARCH_START = 'GRANULE_PAGE_SEARCH_START' // TODO this is horribly named!
export const granulePageSearchStart = () => ({
  type: GRANULE_PAGE_SEARCH_START,
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
