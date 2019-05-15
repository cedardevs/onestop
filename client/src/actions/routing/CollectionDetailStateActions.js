export const COLLECTION_DETAIL_REQUESTED = 'COLLECTION_DETAIL_REQUESTED'
export const collectionDetailRequested = id => ({
  type: COLLECTION_DETAIL_REQUESTED,
  id: id,
})

export const COLLECTION_DETAIL_RECIEVED = 'COLLECTION_DETAIL_RECIEVED'
export const collectionDetailRecieved = (data, granuleCount) => ({
  type: COLLECTION_DETAIL_RECIEVED,
  collection: data,
  totalGranuleCount: granuleCount,
})

export const COLLECTION_DETAIL_ERROR = 'COLLECTION_DETAIL_ERROR'
export const collectionDetailError = errors => ({
  type: COLLECTION_DETAIL_ERROR,
  errors,
})
