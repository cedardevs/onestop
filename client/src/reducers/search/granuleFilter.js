import Immutable from 'seamless-immutable'
import {
  GRANULE_UPDATE_FILTERS,
  // GRANULE_REMOVE_FILTERS,
  // GRANULE_UPDATE_QUERY_TEXT,
  // GRANULE_UPDATE_GEOMETRY,
  // GRANULE_REMOVE_GEOMETRY,
  GRANULE_UPDATE_DATE_RANGE,
  GRANULE_REMOVE_DATE_RANGE,
  // GRANULE_TOGGLE_SELECTED_ID,
  // GRANULE_CLEAR_SELECTED_IDS,
  // GRANULE_TOGGLE_FACET,
  // GRANULE_CLEAR_FACETS,
  // GRANULE_TOGGLE_EXCLUDE_GLOBAL,
} from '../../actions/search/GranuleFilterActions'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
})

export const granuleFilter = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_UPDATE_FILTERS:
      return Immutable.merge(initialState, action.filters || {})

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
    // case GRANULE_UPDATE_GEOMETRY:
    //   return Immutable.set(state, 'geoJSON', action.geoJSON)
    //
    // case GRANULE_REMOVE_GEOMETRY:
    //   return Immutable.set(state, 'geoJSON', initialState.geoJSON)

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

    // case GRANULE_TOGGLE_FACET:
    //   return Immutable.set(state, 'selectedFacets', action.selectedFacets)
    //
    // case GRANULE_CLEAR_FACETS:
    //   return Immutable.set(state, 'selectedFacets', initialState.selectedFacets)
    //
    // case GRANULE_TOGGLE_EXCLUDE_GLOBAL:
    //   return Immutable.set(state, 'excludeGlobal', !state.excludeGlobal)

    default:
      return state
  }
}

export default granuleFilter
