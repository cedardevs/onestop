import Immutable from 'seamless-immutable'
// import // GRANULE_CLEAR_FACETS,
// // GRANULE_REMOVE_FILTERS,
// '../../actions/search/GranuleFilterActions'
import {
  GRANULE_NEW_SEARCH_START,
  GRANULE_PAGE_SEARCH_START,
  GRANULE_SEARCH_COMPLETE,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'
// import // GRANULE_INCREMENT_RESULTS_OFFSET,
// // GRANULE_CLEAR_RESULTS,
// // GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
// // GRANULE_CLEAR_DETAIL_GRANULES_RESULT,
// // GRANULE_UPDATE_DETAIL_GRANULES_TOTAL,
// '../../actions/search/GranuleResultActions'

export const initialState = Immutable({
  granules: {},
  facets: {},
  totalGranules: 0,
  loadedGranules: 0,
  granulesPageOffset: 0,
  pageSize: 20,
})

const granuleResults = (state, granules, action) => {
  // TODO rename this, it's vague
  return Immutable.merge(state, {
    loadedGranules: (granules && Object.keys(granules).length) || 0,
    granules: granules,
    totalGranules: action.total,
    facets: action.metadata ? action.metadata.facets : initialState.facets,
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

    // Result Effects from 'GranuleResultActions'
    // case GRANULE_CLEAR_RESULTS:
    //   return Immutable.merge(state, {
    //     granules: initialState.granules,
    //     totalGranules: initialState.totalGranules,
    //     granulesPageOffset: initialState.granulesPageOffset,
    //   })

    case GRANULE_NEW_SEARCH_START:
        return Immutable.set(
          state,
          'granulesPageOffset',
          initialState.granulesPageOffset
        )
    case GRANULE_PAGE_SEARCH_START:
        return Immutable.set(
          state,
          'granulesPageOffset',
          state.granulesPageOffset + state.pageSize
        )
    case GRANULE_SEARCH_COMPLETE:
      let newGranules = action.items.reduce(
        (existing, next) => existing.set(next.id, next.attributes),
        initialState.granules
      )
      if (action.clearPreviousResults) {
        return granuleResults(state, newGranules, action)
      }

      let allGranules = state.granules.merge(newGranules)

      return granuleResults(state, allGranules, action)

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
