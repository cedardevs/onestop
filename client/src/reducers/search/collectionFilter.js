import Immutable from 'seamless-immutable'
import {
  COLLECTION_UPDATE_GEOMETRY,
  COLLECTION_REMOVE_GEOMETRY,
  COLLECTION_UPDATE_DATE_RANGE,
  COLLECTION_REMOVE_DATE_RANGE,
  COLLECTION_UPDATE_YEAR_RANGE,
  COLLECTION_REMOVE_YEAR_RANGE,
  COLLECTION_TOGGLE_FACET,
  COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  COLLECTION_MORE_RESULTS_REQUESTED,
} from '../../actions/routing/CollectionSearchStateActions'
import {PAGE_SIZE} from '../../utils/queryUtils'
import {updateSelectedFacets} from '../../utils/filterUtils'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  timeRelationship: null, // note there's not really a way to clear this value, but it should be clear if all 4 date/year options are null.. TODO
  startDateTime: null,
  endDateTime: null,
  startYear: null,
  endYear: null,
  selectedFacets: {},
  excludeGlobal: null,
  pageOffset: 0,
})

export const collectionFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_UPDATE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    case COLLECTION_REMOVE_GEOMETRY:
      return Immutable.set(state, 'geoJSON', initialState.geoJSON)

    case COLLECTION_UPDATE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: action.startDate,
        endDateTime: action.endDate,
        timeRelationship: action.relationship ? action.relationship : null, // prevent undefined
      })

    case COLLECTION_UPDATE_YEAR_RANGE:
      return Immutable.merge(state, {
        startYear: action.startYear,
        endYear: action.endYear,
        timeRelationship: action.relationship ? action.relationship : null, // prevent undefined
      })

    case COLLECTION_REMOVE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: initialState.startDateTime,
        endDateTime: initialState.endDateTime,
      })

    case COLLECTION_REMOVE_YEAR_RANGE:
      return Immutable.merge(state, {
        startYear: initialState.startYear,
        endYear: initialState.endYear,
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
