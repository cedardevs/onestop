import 'isomorphic-fetch'

export const GRANULE_UPDATE_TOTAL = 'GRANULE_UPDATE_TOTAL'
export const granuleUpdateTotal = totalGranules => ({
  type: GRANULE_UPDATE_TOTAL,
  totalGranules: totalGranules,
})

export const GRANULE_CLEAR_RESULTS = 'GRANULE_CLEAR_RESULTS'
export const granuleClearResults = () => ({type: GRANULE_CLEAR_RESULTS})

// export const GRANULE_INCREMENT_RESULTS_OFFSET =
//   'GRANULE_INCREMENT_RESULTS_OFFSET'
// export const granuleIncrementResultsOffset = () => ({
//   type: GRANULE_INCREMENT_RESULTS_OFFSET,
// })
//
// export const GRANULE_CLEAR_DETAIL_GRANULES_RESULT =
//   'GRANULE_CLEAR_DETAIL_GRANULES_RESULT'
// export const granuleClearDetailGranulesResult = () => ({
//   type: GRANULE_CLEAR_DETAIL_GRANULES_RESULT,
// })
//
// export const GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET =
//   'GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET'
// export const granuleIncrementDetailGranulesResultOffset = () => ({
//   type: GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
// })
//
// export const GRANULE_UPDATE_DETAIL_GRANULES_TOTAL =
//   'GRANULE_UPDATE_DETAIL_GRANULES_TOTAL'
// export const granuleUpdateDetailGranulesTotal = totalGranules => ({
//   type: GRANULE_UPDATE_DETAIL_GRANULES_TOTAL,
//   totalGranules: totalGranules,
// })
//
export const GRANULE_METADATA_RECEIVED = 'GRANULE_METADATA_RECEIVED'
export const granuleMetadataReceived = metadata => ({
  type: GRANULE_METADATA_RECEIVED,
  metadata,
})
