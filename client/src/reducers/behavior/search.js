import Immutable from 'seamless-immutable'
import {
  UPDATE_QUERY, UPDATE_SEARCH,
  NEW_GEOMETRY, REMOVE_GEOMETRY,
  UPDATE_DATE_RANGE, TOGGLE_FACET,
  TOGGLE_SELECTION, CLEAR_SELECTIONS
} from '../../actions/SearchParamActions'
import { CLEAR_FACETS } from '../../actions/SearchRequestActions'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: []
})

export const search = (state = initialState, action) => {
  switch (action.type) {
    case UPDATE_QUERY:
      return Immutable.set(state, 'queryText', action.searchText)

    case NEW_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    case REMOVE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', initialState.geoJSON)

    case UPDATE_DATE_RANGE:
      return Immutable.merge(state, {startDateTime: action.startDate,
        endDateTime: action.endDate })

    case TOGGLE_FACET:
      return Immutable.set(state, 'selectedFacets', action.selectedFacets)

    case TOGGLE_SELECTION:
      return Immutable.set(state, 'selectedIds', toggleId(state.selectedIds, action.id))

    case CLEAR_FACETS:
      return Immutable.set(state, 'selectedFacets', initialState.selectedFacets)

    case CLEAR_SELECTIONS:
      return Immutable.set(state, 'selectedIds', initialState.selectedIds)

    case UPDATE_SEARCH:
      return Immutable.merge(initialState, action.params || {})

    default:
      return state
  }
}

export default search

const toggleId = (selectedIds, value, idx = 0) => {
  if (idx === selectedIds.length){
    return selectedIds.concat([value])
  } else if (selectedIds[idx] === value){
    return selectedIds.slice(0, idx).concat(selectedIds.slice(idx + 1))
  } else { return toggleId(selectedIds, value, idx + 1) }
}
