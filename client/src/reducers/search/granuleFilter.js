import Immutable from 'seamless-immutable'
import {
  // GRANULE_UPDATE_QUERY_TEXT,
  GRANULE_UPDATE_GEOMETRY,
  GRANULE_REMOVE_GEOMETRY,
  GRANULE_UPDATE_DATE_RANGE,
  GRANULE_REMOVE_DATE_RANGE,
  GRANULE_TOGGLE_FACET,
  GRANULE_TOGGLE_EXCLUDE_GLOBAL,
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  GRANULE_MORE_RESULTS_REQUESTED,
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

export const granuleFilter = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_REQUESTED:
      return Immutable.merge(state, {
        pageOffset: initialState.pageOffset,
        selectedIds: [ action.id ],
      })

    case GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED:
      return Immutable.without(
        Immutable.merge(initialState, [
          action.filters,
          {
            pageOffset: initialState.pageOffset,
            selectedIds: [ action.id ],
          },
        ]),
        (value, key) => !(key in initialState)
      ) //Object.keys(initialState).has(key))

    case GRANULE_MORE_RESULTS_REQUESTED:
      return Immutable.set(state, 'pageOffset', state.pageOffset + PAGE_SIZE)

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

    case GRANULE_TOGGLE_FACET:
      const {selectedFacets} = state
      const newSelectedFacets = updateSelectedFacets(
        selectedFacets,
        action.category,
        action.facetName,
        action.selected
      )

      return Immutable.set(state, 'selectedFacets', newSelectedFacets)

    case GRANULE_TOGGLE_EXCLUDE_GLOBAL:
      return Immutable.set(state, 'excludeGlobal', !state.excludeGlobal)

    default:
      return state
  }
}

export default granuleFilter
