export const COLLECTION_GET_DETAIL_START = 'COLLECTION_GET_DETAIL_START' // formerly COLLECTION_DETAIL_REQUEST
export const collectionGetDetailStart = id => ({
  type: COLLECTION_GET_DETAIL_START,
  id: id,
})

export const COLLECTION_GET_DETAIL_COMPLETE = 'COLLECTION_GET_DETAIL_COMPLETE' // formerly COLLECTION_DETAIL_SUCCESS
export const collectionGetDetailComplete = (
  data,
  metadata
  // clearPreviousResults,
  // total,
  // items,
  // metadata
) => ({
  type: COLLECTION_GET_DETAIL_COMPLETE,
  result: {
    collection: data,
    totalCollectionCount: metadata.totalCollections, // TODO what even *is* this?
  },
  // clearPreviousResults: clearPreviousResults,
  // total: total,
  // metadata: metadata,
  // items: items,
})

export const COLLECTION_GET_DETAIL_ERROR = 'COLLECTION_GET_DETAIL_ERROR'
export const collectionGetDetailError = errors => ({
  type: COLLECTION_GET_DETAIL_ERROR,
  errors,
})
