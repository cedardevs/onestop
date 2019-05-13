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
    // TODO we're not even *using* totalGranuleCount - and we either want to or should remove it! - actually we probably are using it, but there's not a unique spot in the state to make it clear. So fix the reducer to be less vague
    totalGranuleCount: metadata.totalGranules, // TODO requests like this with cause weird errors if metadata is not defined as an input - need more error handling or is it ok?
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
