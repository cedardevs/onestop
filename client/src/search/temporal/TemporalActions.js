// export const AFTER_TEMPORAL_SEARCH = 'AFTER_temporal_search'
// export const BEFORE_TEMPORAL_SEARCH = 'before_temporal_search'
//
// export const afterDateSearch = (afterDatetime) => {
//   return {
//     type: AFTER_TEMPORAL_SEARCH,
//     afterDatetime
//   }
// }
//
// export const beforeDateSearch = (beforeDatetime) => {
//   return {
//     type: BEFORE_TEMPORAL_SEARCH,
//     beforeDatetime
//   }
// }
//
// export const temporalSearch = (afterDatetime, beforeDatetime) => {
//   return (dispatch, getState) => {
//
//            (dispatch(afterDateSearch(afterDatetime)))
//            (dispatch(beforeDateSearch(beforeDatetime)))
//
//   }
// }



export const TEMPORAL_SEARCH = 'temporal_search'

export const temporalSearch = (beforeDatetime, afteDatetime) => {
  return {
    type: TEMPORAL_SEARCH,
    before: beforeDatetime,
    after: afteDatetime
  }
}

// dispatch(temporalSearch('2000-01-01T00:00:00Z', null))

