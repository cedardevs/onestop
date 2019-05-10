import Immutable from 'seamless-immutable'
import {
  COLLECTION_UPDATE_FILTERS,
  COLLECTION_UPDATE_QUERY_TEXT,
  COLLECTION_UPDATE_GEOMETRY,
  COLLECTION_REMOVE_GEOMETRY,
  COLLECTION_UPDATE_DATE_RANGE,
  COLLECTION_REMOVE_DATE_RANGE,
  COLLECTION_TOGGLE_FACET,
  COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  COLLECTION_CLEAR_FILTERS,
} from '../../actions/search/CollectionFilterActions'
// import {COLLECTION_SEARCH_COMPLETE} from '../../actions/search/CollectionRequestActions'
import {COLLECTION_GET_DETAIL_START} from '../../actions/get/CollectionDetailRequestActions'
// import {toggleSelectedId} from '../../utils/filterUtils'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
})

export const collectionFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_UPDATE_FILTERS:
      return Immutable.merge(initialState, action.filters || {})

    case COLLECTION_UPDATE_QUERY_TEXT:
      return Immutable.set(state, 'queryText', action.queryText)

    case COLLECTION_UPDATE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    case COLLECTION_REMOVE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', initialState.geoJSON)

    case COLLECTION_UPDATE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: action.startDate,
        endDateTime: action.endDate,
      })

    case COLLECTION_REMOVE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: initialState.startDateTime,
        endDateTime: initialState.endDateTime,
      })

    case COLLECTION_GET_DETAIL_START: // This suggests that selectedIds belongs elsewhere - is it part of collectionDetailResult? granuleRequest? tangled and nebulous
      return Immutable.set(state, 'selectedIds', [ action.id ])

    case COLLECTION_TOGGLE_FACET:
      return Immutable.set(state, 'selectedFacets', action.selectedFacets)

    // case COLLECTION_SEARCH_COMPLETE:
    //   if (action.clearPreviousResults) {
    //     console.log('clear previous results, reseting collection filters!!')
    //     // reset the selected facets when there's a new search!
    // TODO this also happens at the wrong time
    //     return Immutable.set(
    //       state,
    //       'selectedFacets',
    //       initialState.selectedFacets
    //     )
    //     // return initialState
    //   }
    //   console.log('search complete, should be no changes')
    //   return state

    case COLLECTION_TOGGLE_EXCLUDE_GLOBAL:
      return Immutable.set(state, 'excludeGlobal', !state.excludeGlobal)

    case COLLECTION_CLEAR_FILTERS:
      return Immutable.merge(state, {
        // this action is triggered by 'queryText' searches to ensure a fresh filter;
        // consequently, we do not reset 'queryText' back to its initial state
        geoJSON: initialState.geoJSON,
        startDateTime: initialState.startDateTime,
        endDateTime: initialState.endDateTime,
        selectedFacets: initialState.selectedFacets,
        selectedIds: initialState.selectedIds,
        excludeGlobal: initialState.excludeGlobal,
      })

    default:
      return state
  }
}

export default collectionFilter
