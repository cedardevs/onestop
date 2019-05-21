import Immutable from 'seamless-immutable'
import {
  COLLECTION_UPDATE_QUERY_TEXT,
  COLLECTION_UPDATE_GEOMETRY,
  COLLECTION_REMOVE_GEOMETRY,
  COLLECTION_UPDATE_DATE_RANGE,
  COLLECTION_REMOVE_DATE_RANGE,
  COLLECTION_TOGGLE_FACET,
  COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  COLLECTION_CLEAR_FILTERS,
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  COLLECTION_MORE_RESULTS_REQUESTED,
} from '../../actions/routing/CollectionSearchStateActions'
import {PAGE_SIZE} from '../../utils/queryUtils'
import {updateSelectedFacets} from '../../utils/filterUtils'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  excludeGlobal: null,
  pageOffset: 0,
})

export const collectionFilter = (state = initialState, action) => {
  switch (action.type) {
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

    case COLLECTION_TOGGLE_FACET:
      const {selectedFacets} = state
      const newSelectedFacets = updateSelectedFacets(
        selectedFacets,
        action.category,
        action.facetName,
        action.selected
      )

      return Immutable.set(state, 'selectedFacets', newSelectedFacets)

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
        excludeGlobal: initialState.excludeGlobal,
      })

    case COLLECTION_NEW_SEARCH_REQUESTED:
      return Immutable.merge(state, {pageOffset: initialState.pageOffset})

    case COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED:
      return Immutable.merge(initialState, [
        action.filters,
        {pageOffset: initialState.pageOffset},
      ])

    case COLLECTION_MORE_RESULTS_REQUESTED:
      return Immutable.set(state, 'pageOffset', state.pageOffset + PAGE_SIZE)

    default:
      return state
  }
}

export default collectionFilter
