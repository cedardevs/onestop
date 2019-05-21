import Immutable from 'seamless-immutable'
import {
  // GRANULE_REMOVE_FILTERS,
  // GRANULE_UPDATE_QUERY_TEXT,
  GRANULE_UPDATE_GEOMETRY,
  GRANULE_REMOVE_GEOMETRY,
  GRANULE_UPDATE_DATE_RANGE,
  GRANULE_REMOVE_DATE_RANGE,
  GRANULE_TOGGLE_FACET,
  // GRANULE_CLEAR_FACETS,
  GRANULE_TOGGLE_EXCLUDE_GLOBAL,
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  GRANULE_MORE_RESULTS_REQUESTED,
  // GRANULE_MATCHING_COUNT_REQUESTED,
} from '../../actions/routing/GranuleSearchStateActions'
import {PAGE_SIZE} from '../../utils/queryUtils'
import {updateSelectedFacets} from '../../utils/filterUtils'

export const initialState = Immutable({
  // queryText: '', Not currently a valid param for granule searchs!
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
  pageOffset: 0,
})

const updateFilters = ({
  geoJSON,
  startDateTime,
  endDateTime,
  selectedFacets,
  selectedIds,
  excludeGlobal,
}) => {
  return Immutable.merge(initialState, {
    geoJSON: geoJSON || initialState.geoJSON,
    startDateTime: startDateTime || initialState.startDateTime,
    endDateTime: endDateTime || initialState.endDateTime,
    selectedFacets: selectedFacets || initialState.selectedFacets,
    selectedIds: selectedIds || initialState.selectedIds,
    excludeGlobal: excludeGlobal || initialState.excludeGlobal,
  })
}

export const granuleFilter = (state = initialState, action) => {
  switch (action.type) {
    // case GRANULE_MATCHING_COUNT_REQUESTED:
    case GRANULE_NEW_SEARCH_REQUESTED:
      return Immutable.merge(state, {
        pageOffset: initialState.pageOffset,
        selectedIds: [ action.id ],
      })

    case GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED:
      // return Immutable.merge(state, [action.filters, {pageOffset: initialState.pageOffset,
      // selectedIds: [ action.id ],}])
      return updateFilters(
        Immutable.merge(initialState, [
          action.filters,
          {
            pageOffset: initialState.pageOffset,
            selectedIds: [ action.id ],
          },
        ])
      ) // TODO this is now an even gnarlier, hard to understand action. Basically merge inital state + all the input  / side effects together, then run thru updateFilters function to remove state which doesn't exist (queryText)

    case GRANULE_MORE_RESULTS_REQUESTED:
      return Immutable.set(state, 'pageOffset', state.pageOffset + PAGE_SIZE)

    // case GRANULE_REMOVE_FILTERS:
    //   return Immutable.merge(state, {
    //     // this action is triggered by 'queryText' searches to ensure a fresh filter;
    //     // consequently, we do not reset 'queryText' back to its initial state
    //     geoJSON: initialState.geoJSON,
    //     startDateTime: initialState.startDateTime,
    //     endDateTime: initialState.endDateTime,
    //     selectedFacets: initialState.selectedFacets,
    //     selectedIds: initialState.selectedIds,
    //     excludeGlobal: initialState.excludeGlobal,
    //   })

    // case GRANULE_UPDATE_QUERY_TEXT:
    //   return Immutable.set(state, 'queryText', action.queryText)
    //
    case GRANULE_UPDATE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    case GRANULE_REMOVE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', initialState.geoJSON)

    case GRANULE_UPDATE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: action.startDate,
        endDateTime: action.endDate,
      })

    case GRANULE_REMOVE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: initialState.startDateTime,
        endDateTime: initialState.endDateTime,
      })

    // case GRANULE_TOGGLE_SELECTED_ID:
    //   return Immutable.set(
    //     state,
    //     'selectedIds',
    //     toggleSelectedId(state.selectedIds, action.granuleId)
    //   )
    // case GRANULE_CLEAR_SELECTED_IDS:
    //   return Immutable.set(state, 'selectedIds', initialState.selectedIds)

    case GRANULE_TOGGLE_FACET:
      const {selectedFacets} = state
      const newSelectedFacets = updateSelectedFacets(
        selectedFacets,
        action.category,
        action.facetName,
        action.selected
      )

      return Immutable.set(state, 'selectedFacets', newSelectedFacets)
    //
    // case GRANULE_CLEAR_FACETS:
    //   return Immutable.set(state, 'selectedFacets', initialState.selectedFacets)
    //
    case GRANULE_TOGGLE_EXCLUDE_GLOBAL:
      return Immutable.set(state, 'excludeGlobal', !state.excludeGlobal)

    default:
      return state
  }
}

export default granuleFilter
