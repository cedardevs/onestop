import Immutable from 'seamless-immutable'

import {
  GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  GRANULE_RESULTS_PAGE_RECEIVED,
  GRANULE_SEARCH_ERROR,
} from '../../actions/routing/GranuleSearchStateActions'
import {
  countKeys,
  mergeGranulesArrayIntoGranulesMap,
} from '../../utils/resultUtils'

export const initialState = Immutable({
  granules: {},
  facets: {},
  totalGranuleCount: 0,
  loadedGranuleCount: 0,
})

const newSearchResultsReceived = (state, action) => {
  let newGranules = mergeGranulesArrayIntoGranulesMap(
    action.granules,
    initialState.granules
  )
  return Immutable.merge(state, {
    granules: newGranules,
    loadedGranuleCount: countKeys(newGranules),
    totalGranuleCount: action.total,
    facets: action.facets,
  })
}

const pageResultsReceived = (state, total, action) => {
  //clean out old granules from last page with initialState granules
  let newGranules = mergeGranulesArrayIntoGranulesMap(
    action.granules,
    initialState.granules
  )
  return Immutable.merge(state, {
    granules: newGranules,
    loadedGranuleCount: countKeys(newGranules),
    totalGranuleCount: total,
    facets: action.facets != null ? action.facets : state.facets,
  })
}

export const granuleResult = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_RESULTS_RECEIVED:
      return newSearchResultsReceived(state, action)

    case GRANULE_RESULTS_PAGE_RECEIVED:
      return pageResultsReceived(state, action.total, action)

    case GRANULE_SEARCH_ERROR:
      return Immutable.merge(state, {
        loadedGranuleCount: initialState.loadedGranuleCount,
        granules: initialState.granules,
        totalGranuleCount: initialState.totalGranuleCount,
        facets: initialState.facets,
      })

    default:
      return state
  }
}

export default granuleResult
