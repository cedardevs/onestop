// export const COLLECTION_UPDATE_TOTAL = 'COLLECTION_UPDATE_TOTAL'
// export const collectionUpdateTotal = totalCollections => ({
//   type: COLLECTION_UPDATE_TOTAL,
//   totalCollections: totalCollections,
// })

export const COLLECTION_CLEAR_RESULTS = 'COLLECTION_CLEAR_RESULTS'
export const collectionClearResults = () => ({type: COLLECTION_CLEAR_RESULTS})

export const COLLECTION_INCREMENT_RESULTS_OFFSET =
  'COLLECTION_INCREMENT_RESULTS_OFFSET'
export const collectionIncrementResultsOffset = () => ({
  type: COLLECTION_INCREMENT_RESULTS_OFFSET,
})

// TODO figure out where these detail specific things are used, and what for???
export const COLLECTION_CLEAR_DETAIL_GRANULES_RESULT =
  'COLLECTION_CLEAR_DETAIL_GRANULES_RESULT'
export const collectionClearDetailGranulesResult = () => ({
  type: COLLECTION_CLEAR_DETAIL_GRANULES_RESULT,
})

export const COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET =
  'COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET'
export const collectionIncrementDetailGranulesResultOffset = () => ({
  type: COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
})

export const COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL =
  'COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL'
export const collectionUpdateDetailGranulesTotal = totalGranules => ({
  type: COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL,
  totalGranules: totalGranules,
})

export const COLLECTION_METADATA_RECEIVED = 'COLLECTION_METADATA_RECEIVED'
export const collectionMetadataReceived = metadata => ({
  type: COLLECTION_METADATA_RECEIVED,
  metadata,
})
