
export const TEMPORAL_SEARCH = 'temporal_search'

export const temporalSearch = (searchDatetime) => {
  return {
    type: TEMPORAL_SEARCH,
    searchDatetime
  }
}