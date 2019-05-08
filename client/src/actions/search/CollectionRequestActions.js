// export const COLLECTION_SEARCH_REQUEST = 'COLLECTION_SEARCH_REQUEST'
// export const collectionSearchRequest = () => ({type: COLLECTION_SEARCH_REQUEST})
//
// export const COLLECTION_SEARCH_SUCCESS = 'COLLECTION_SEARCH_SUCCESS'
// export const collectionSearchSuccess = items => ({
//   type: COLLECTION_SEARCH_SUCCESS,
//   items,
// })
//
export const COLLECTION_DETAIL_REQUEST = 'COLLECTION_DETAIL_REQUEST'
export const collectionDetailRequest = id => ({
  type: COLLECTION_DETAIL_REQUEST,
  id: id,
})

export const COLLECTION_DETAIL_SUCCESS = 'COLLECTION_DETAIL_SUCCESS'
export const collectionDetailSuccess = (data, metadata) => ({
  type: COLLECTION_DETAIL_SUCCESS,
  result: {
    collection: data,
    totalCollectionCount: metadata.totalCollections,
  },
})


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
