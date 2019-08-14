import Immutable from 'seamless-immutable'

import {
  GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  GRANULE_MORE_RESULTS_RECEIVED,
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

const moreResultsReceived = (state, action) => {
  let newGranules = mergeGranulesArrayIntoGranulesMap(
    action.granules,
    state.granules
  )
  return Immutable.merge(state, {
    granules: newGranules,
    loadedGranuleCount: countKeys(newGranules),
  })
}

export const granuleResult = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_RESULTS_RECEIVED:
      return newSearchResultsReceived(state, action)

    case GRANULE_MORE_RESULTS_RECEIVED:
      return moreResultsReceived(state, action)

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
