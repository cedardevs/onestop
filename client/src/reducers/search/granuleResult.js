import Immutable from 'seamless-immutable'
import {
  // GRANULE_CLEAR_FACETS,
  // GRANULE_REMOVE_FILTERS,
} from '../../actions/search/GranuleFilterActions'
import {
  // GRANULE_SEARCH_SUCCESS,
  // GRANULE_DETAIL_SUCCESS,
  // GRANULE_DETAIL_GRANULES_SUCCESS,
  // GRANULE_SEARCH_START,
  GRANULE_SEARCH_COMPLETE,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'
import {
  GRANULE_UPDATE_TOTAL,
  // GRANULE_INCREMENT_RESULTS_OFFSET,
  GRANULE_CLEAR_RESULTS,
  // GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
  // GRANULE_CLEAR_DETAIL_GRANULES_RESULT,
  // GRANULE_UPDATE_DETAIL_GRANULES_TOTAL,
  GRANULE_METADATA_RECEIVED,
} from '../../actions/search/GranuleResultActions'

export const initialState = Immutable({
  granules: {},
  facets: {},
  totalGranules: 0,
  granulesPageOffset: 0,
  totalGranules: 0,
  granulesPageOffset: 0,
  pageSize: 20,
  // granuleDetail: null,
})

export const granuleResult = (state = initialState, action) => {
  switch (action.type) {
    // Result Effects from 'GranuleFilterActions'
    // case GRANULE_CLEAR_FACETS:
    //   return Immutable.set(state, 'facets', initialState.facets)
    //
    // case GRANULE_REMOVE_FILTERS:
    //   return Immutable.set(state, 'facets', initialState.facets)

    // Result Effects from 'GranuleRequestActions'
    // case GRANULE_SEARCH_SUCCESS:
    //   // let newGranules = {}
    //   // action.items.forEach((val, key) => {
    //   //   newGranules[key] = val
    //   // })
    //
    //   let newGranules = action.items.reduce(
    //       (existing, next) => existing.set(next.id, next.attributes),
    //       state.granules
    //     )
    //
    //   let allGranules = state.granules.merge(newGranules)
    //   return Immutable.set(state, 'granules', allGranules)

    // case GRANULE_DETAIL_SUCCESS:
    //   return Immutable.set(state, 'granuleDetail', action.result)
    //
    // case GRANULE_DETAIL_GRANULES_SUCCESS:
    //   const newGranules = action.granules.reduce(
    //     (existing, next) => existing.set(next.id, next.attributes),
    //     state.granules
    //   )
    //   return Immutable.set(state, 'granules', newGranules)

    // Result Effects from 'GranuleResultActions'
    case GRANULE_CLEAR_RESULTS:
      return Immutable.merge(state, {
        granules: initialState.granules,
        totalGranules: initialState.totalGranules,
        granulesPageOffset: initialState.granulesPageOffset,
      })

    // case GRANULE_UPDATE_TOTAL:
    //   return Immutable.set(state, 'totalGranules', action.totalGranules)

    // case GRANULE_CLEAR_DETAIL_GRANULES_RESULT:
    //   return Immutable.merge(state, {
    //     granules: initialState.granules,
    //     totalGranules: initialState.totalGranules,
    //     granulesPageOffset: initialState.granulesPageOffset,
    //   })
    //
    // case GRANULE_UPDATE_DETAIL_GRANULES_TOTAL:
    //   return Immutable.set(state, 'totalGranules', action.totalGranules)

    // case GRANULE_METADATA_RECEIVED:
    //   return Immutable.set(state, 'facets', action.metadata.facets)

    case GRANULE_SEARCH_COMPLETE:
      let newGranules = action.items.reduce(
          (existing, next) => existing.set(next.id, next.attributes),
          state.granules
        )

      let allGranules = state.granules.merge(newGranules)
      return Immutable.merge(state, {
        granules: allGranules,
        totalGranules: action.total,
        facets: action.metadata? action.metadata.facets : initialState.facets
      })

    case GRANULE_SEARCH_ERROR:

    return Immutable.merge(state, {
      granules: initialState.granules,
      totalGranules: initialState.totalGranules,
      facets: action.metadata? action.metadata.facets : initialState.facets
    })




    // case GRANULE_INCREMENT_RESULTS_OFFSET:
    //   return Immutable.set(
    //     state,
    //     'granulesPageOffset',
    //     state.granulesPageOffset + state.pageSize
    //   )

    // case GRANULE_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET:
    //   return Immutable.set(
    //     state,
    //     'granulesPageOffset',
    //     state.granulesPageOffset + state.pageSize
    //   )

    default:
      return state
  }
}

export default granuleResult
