import Immutable from 'seamless-immutable'
// import // GRANULE_CLEAR_FACETS,
// // GRANULE_REMOVE_FILTERS,
// '../../actions/search/GranuleFilterActions'
import {
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_MORE_RESULTS_REQUESTED,
  GRANULE_NEW_SEARCH_RESULTS_RECIEVED,
  GRANULE_MORE_RESULTS_RECIEVED,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'

export const initialState = Immutable({
  granules: {},
  facets: {},
  totalGranules: 0,
  loadedGranules: 0,
  granulesPageOffset: 0, // TODO move to request
  pageSize: 20,
})

const getGranulesFromAction = (action) => {
  return action.items.reduce(
    (existing, next) => existing.set(next.id, next.attributes),
    initialState.granules
  )
}

const newSearchResultsRecieved = (state, total, granules, facets) => {
    return Immutable.merge(state, {
      loadedGranules: (granules && Object.keys(granules).length) || 0,
      granules: granules,
      totalGranules: total,
      facets: facets
    }
  )
}

const moreResultsRecieved = (state, newGranules) => {
  let granules = state.granules.merge(newGranules)

  return Immutable.merge(state, {
    loadedGranules: (granules && Object.keys(granules).length) || 0,
    granules: granules,
  })
}

export const granuleResult = (state = initialState, action) => {
  switch (action.type) {
    // Result Effects from 'GranuleFilterActions'
    // case GRANULE_CLEAR_FACETS:
    //   return Immutable.set(state, 'facets', initialState.facets)
    //
    // case GRANULE_REMOVE_FILTERS:
    //   return Immutable.set(state, 'facets', initialState.facets)

    case GRANULE_NEW_SEARCH_REQUESTED:
        return Immutable.set(
          state,
          'granulesPageOffset',
          initialState.granulesPageOffset
        )

    case GRANULE_MORE_RESULTS_REQUESTED:
        return Immutable.set(
          state,
          'granulesPageOffset',
          state.granulesPageOffset + state.pageSize
        )

    case GRANULE_NEW_SEARCH_RESULTS_RECIEVED:
      return newSearchResultsRecieved(state, action.total, getGranulesFromAction(action), action.metadata.facets)

    case GRANULE_MORE_RESULTS_RECIEVED:
      eturn moreResultsRecieved(state, getGranulesFromAction(action))

    case GRANULE_SEARCH_ERROR:
      return Immutable.merge(state, {
        loadedGranules: initialState.loadedGranules,
        granules: initialState.granules,
        totalGranules: initialState.totalGranules,
        facets: initialState.facets,
      })

    default:
      return state
  }
}

export default granuleResult
