export const COLLECTION_DETAIL_REQUESTED = 'COLLECTION_DETAIL_REQUESTED'
export const collectionDetailRequested = (id, filters) => ({
  type: COLLECTION_DETAIL_REQUESTED,
  id: id,
  filters: filters,
})

export const COLLECTION_DETAIL_RECEIVED = 'COLLECTION_DETAIL_RECEIVED'
export const collectionDetailReceived = (data, granuleCount) => ({
  type: COLLECTION_DETAIL_RECEIVED,
  collection: data,
  totalGranuleCount: granuleCount,
})

export const COLLECTION_DETAIL_ERROR = 'COLLECTION_DETAIL_ERROR'
export const collectionDetailError = errors => ({
  type: COLLECTION_DETAIL_ERROR,
  errors,
})

export const GRANULE_MATCHING_COUNT_REQUESTED =
  'GRANULE_MATCHING_COUNT_REQUESTED' // TODO change 'matching' to 'filtered'?
export const granuleMatchingCountRequested = () => ({
  type: GRANULE_MATCHING_COUNT_REQUESTED,
})

export const GRANULE_MATCHING_COUNT_RECEIVED = 'GRANULE_MATCHING_COUNT_RECEIVED'
export const granuleMatchingCountReceived = total => ({
  type: GRANULE_MATCHING_COUNT_RECEIVED,
  filteredGranuleCount: total,
})

export const GRANULE_MATCHING_COUNT_ERROR = 'GRANULE_MATCHING_COUNT_ERROR'
export const granuleMatchingCountError = errors => ({
  type: GRANULE_MATCHING_COUNT_ERROR,
  errors,
})
