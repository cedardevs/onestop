import Immutable from 'seamless-immutable'
// import // GRANULE_CLEAR_FACETS,
// // GRANULE_REMOVE_FILTERS,
// '../../actions/routing/GranuleSearchStateActions'
import {
  GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  GRANULE_MORE_RESULTS_RECEIVED,
  GRANULE_MATCHING_COUNT_RECEIVED,
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
    // Result Effects from 'GranuleSearchStateActions'
    // case GRANULE_CLEAR_FACETS:
    //   return Immutable.set(state, 'facets', initialState.facets)
    //
    // case GRANULE_REMOVE_FILTERS:
    //   return Immutable.set(state, 'facets', initialState.facets)

    case GRANULE_NEW_SEARCH_RESULTS_RECEIVED:
      return newSearchResultsReceived(
        state,
        action.total,
        getGranulesFromAction(action),
        action.facets
      )

    case GRANULE_MATCHING_COUNT_RECEIVED:
      return Immutable.merge(initialState, {
        totalGranuleCount: action.total,
      })

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
