import Immutable from 'seamless-immutable'
import {
  COLLECTION_UPDATE_GEOMETRY,
  COLLECTION_REMOVE_GEOMETRY,
  COLLECTION_UPDATE_GEO_RELATIONSHIP,
  COLLECTION_UPDATE_TIME_RELATIONSHIP,
  COLLECTION_UPDATE_DATE_RANGE,
  COLLECTION_REMOVE_DATE_RANGE,
  COLLECTION_UPDATE_YEAR_RANGE,
  COLLECTION_REMOVE_YEAR_RANGE,
  COLLECTION_TOGGLE_FACET,
  COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  COLLECTION_RESULTS_PAGE_REQUESTED,
} from '../../actions/routing/CollectionSearchStateActions'
import {PAGE_SIZE} from '../../utils/queryUtils'
import {updateSelectedFacets} from '../../utils/filterUtils'

export const initialState = Immutable({
  queryText: '',
  bbox: null,
  geoRelationship: 'intersects',
  timeRelationship: 'intersects',
  startDateTime: null,
  endDateTime: null,
  startYear: null,
  endYear: null,
  selectedFacets: {},
  excludeGlobal: null,
  pageOffset: 0,
  pageSize: 20, // default
})

export const collectionFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_UPDATE_GEOMETRY:
      return Immutable.set(state, 'bbox', action.bbox)

    case COLLECTION_REMOVE_GEOMETRY:
      return Immutable.set(state, 'bbox', initialState.bbox)

    case COLLECTION_UPDATE_GEO_RELATIONSHIP:
      return Immutable.set(state, 'geoRelationship', action.relationship)

    case COLLECTION_UPDATE_TIME_RELATIONSHIP:
      return Immutable.set(state, 'timeRelationship', action.relationship)

    case COLLECTION_UPDATE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: action.startDate,
        endDateTime: action.endDate,
      })

    case COLLECTION_UPDATE_YEAR_RANGE:
      return Immutable.merge(state, {
        startYear: action.startYear,
        endYear: action.endYear,
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
      return Immutable.merge(state, {
        pageOffset: initialState.pageOffset,
        pageSize: initialState.pageSize,
      })

    case COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED:
      return Immutable.merge(initialState, [
        action.filters,
        {pageOffset: initialState.pageOffset, pageSize: initialState.pageSize},
      ])

    case COLLECTION_RESULTS_PAGE_REQUESTED:
      let updateSize = Immutable.set(state, 'pageSize', action.max)
      return Immutable.set(updateSize, 'pageOffset', action.offset)

    default:
      return state
  }
}

export default collectionFilter
