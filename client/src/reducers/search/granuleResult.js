import Immutable from 'seamless-immutable'

import {
  GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  GRANULE_MORE_RESULTS_RECEIVED,
  GRANULE_SEARCH_ERROR,
} from '../../actions/routing/GranuleSearchStateActions'

export const initialState = Immutable({
  granules: {},
  facets: {},
  totalGranuleCount: 0,
  loadedGranuleCount: 0,
})

const getGranulesFromAction = action => {
  return action.items.reduce(
    (existing, next) => existing.set(next.id, next.attributes),
    initialState.granules
  )
}

const newSearchResultsReceived = (state, total, granules, facets) => {
  return Immutable.merge(state, {
    loadedGranuleCount: (granules && Object.keys(granules).length) || 0,
    granules: granules,
    totalGranuleCount: total,
    facets: facets,
  })
}

const moreResultsReceived = (state, newGranules) => {
  let granules = state.granules.merge(newGranules)

  return Immutable.merge(state, {
    loadedGranuleCount: (granules && Object.keys(granules).length) || 0,
    granules: granules,
  })
}

export const granuleResult = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_RESULTS_RECEIVED:
      return newSearchResultsReceived(
        state,
        action.total,
        getGranulesFromAction(action),
        action.facets
      )

    case GRANULE_MORE_RESULTS_RECEIVED:
      return moreResultsReceived(state, getGranulesFromAction(action))

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
