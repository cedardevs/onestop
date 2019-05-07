// export const GRANULE_SEARCH_REQUEST = 'GRANULE_SEARCH_REQUEST'
// export const granuleSearchRequest = () => ({type: GRANULE_SEARCH_REQUEST})
//
// export const GRANULE_SEARCH_SUCCESS = 'GRANULE_SEARCH_SUCCESS'
// export const granuleSearchSuccess = items => ({
//   type: GRANULE_SEARCH_SUCCESS,
//   items,
// })

export const GRANULE_SEARCH_START = 'GRANULE_SEARCH_START'
export const granuleSearchStart = () => ({
  type: GRANULE_SEARCH_START,
})

export const GRANULE_SEARCH_COMPLETE = 'GRANULE_SEARCH_COMPLETE'
export const granuleSearchComplete = (clearPreviousResults, total, items, metadata) => ({
  type: GRANULE_SEARCH_COMPLETE,
  clearPreviousResults: clearPreviousResults,
  total: total,
  metadata: metadata,
  items: items,
})

export const GRANULE_SEARCH_ERROR = 'GRANULE_SEARCH_ERROR'
export const granuleSearchError = (errors) => ({
  type: GRANULE_SEARCH_ERROR,
  errors,
})

// export const COLLECTION_DETAIL_REQUEST = 'COLLECTION_DETAIL_REQUEST'
// export const collectionDetailRequest = id => ({
//   type: COLLECTION_DETAIL_REQUEST,
//   id: id,
// })
//
// export const COLLECTION_DETAIL_SUCCESS = 'COLLECTION_DETAIL_SUCCESS'
// export const collectionDetailSuccess = (data, metadata) => ({
//   type: COLLECTION_DETAIL_SUCCESS,
//   result: {
//     collection: data,
//     totalGranuleCount: metadata.totalGranules,
//   },
// })
//
// export const COLLECTION_DETAIL_GRANULES_REQUEST =
//   'COLLECTION_DETAIL_GRANULES_REQUEST'
// export const collectionDetailGranulesRequest = () => ({
//   type: COLLECTION_DETAIL_GRANULES_REQUEST,
// })
//
// export const COLLECTION_DETAIL_GRANULES_SUCCESS =
//   'COLLECTION_DETAIL_GRANULES_SUCCESS'
// export const collectionDetailGranulesSuccess = granules => ({
//   type: COLLECTION_DETAIL_GRANULES_SUCCESS,
//   granules,
// })
