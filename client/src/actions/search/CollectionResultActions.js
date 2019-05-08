export const COLLECTION_INCREMENT_RESULTS_OFFSET =
  'COLLECTION_INCREMENT_RESULTS_OFFSET'
export const collectionIncrementResultsOffset = () => ({
  // TODO rename nextPage
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
