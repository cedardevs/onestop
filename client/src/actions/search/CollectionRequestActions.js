export const COLLECTION_SEARCH_REQUEST = 'COLLECTION_SEARCH_REQUEST'
export const collectionSearchRequest = () => ({type: COLLECTION_SEARCH_REQUEST})

export const COLLECTION_SEARCH_SUCCESS = 'COLLECTION_SEARCH_SUCCESS'
export const collectionSearchSuccess = items => ({
  type: COLLECTION_SEARCH_SUCCESS,
  items,
})

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
    totalGranuleCount: metadata.totalGranules,
  },
})
