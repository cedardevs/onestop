import Immutable from 'seamless-immutable'
import {
  GRANULE_SET_QUERY_TEXT,
  GRANULE_UPDATE_GEOMETRY,
  GRANULE_REMOVE_GEOMETRY,
  GRANULE_UPDATE_GEO_RELATIONSHIP,
  GRANULE_UPDATE_TIME_RELATIONSHIP,
  GRANULE_UPDATE_DATE_RANGE,
  GRANULE_REMOVE_DATE_RANGE,
  GRANULE_UPDATE_YEAR_RANGE,
  GRANULE_REMOVE_YEAR_RANGE,
  GRANULE_TOGGLE_FACET,
  GRANULE_TOGGLE_EXCLUDE_GLOBAL,
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  GRANULE_RESULTS_PAGE_REQUESTED,
  GRANULE_TOGGLE_ALL_TERMS_MUST_MATCH,
  RESET_GRANULE_ALL_TERMS_MUST_MATCH,
} from '../../actions/routing/GranuleSearchStateActions'
import {PAGE_SIZE} from '../../utils/queryUtils'
import {updateSelectedFacets} from '../../utils/filterUtils'

export const initialState = Immutable({
  title: '', // TODO rename to something about granule name
  allTermsMustMatch: true,
  bbox: null,
  geoRelationship: 'intersects',
  timeRelationship: 'intersects',
  startDateTime: null,
  endDateTime: null,
  startYear: null,
  endYear: null,
  selectedFacets: {},
  selectedCollectionIds: [],
  excludeGlobal: null,
  pageOffset: 0,
  pageSize: 20, //default
})

const newSearchFilters = (collectionId, filters) => {
  // create state with selectedCollectionIds set explicitly to collectionId,
  // and all other filters set by action (or default to initial state values)
  return Immutable.merge(initialState, [
    filters,
    {
      pageOffset: initialState.pageOffset,
      pageSize: initialState.pageSize,
      selectedCollectionIds: [ collectionId ],
    },
  ])
}

const stripExtraFields = state => {
  // without strips out all fields not part of original state, so no new keys can be added:
  return Immutable.without(state, (value, key) => !(key in initialState))
}

const newSearchRequest = (collectionId, filters) => {
  return stripExtraFields(newSearchFilters(collectionId, filters))
}

export const granuleFilter = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_REQUESTED:
      return newSearchRequest(action.id, state) // this one doesn't REQUIRE the without step, but it's harmless

    case GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED:
      return newSearchRequest(action.id, action.filters)

    case GRANULE_RESULTS_PAGE_REQUESTED:
      let updateSize = Immutable.set(state, 'pageSize', action.max)
      return Immutable.set(updateSize, 'pageOffset', action.offset)

    case GRANULE_SET_QUERY_TEXT:
      return Immutable.set(state, 'title', action.text)

    case GRANULE_UPDATE_GEOMETRY:
      return Immutable.set(state, 'bbox', action.bbox)

    case GRANULE_REMOVE_GEOMETRY:
      return Immutable.set(state, 'bbox', initialState.bbox)

    case GRANULE_UPDATE_GEO_RELATIONSHIP:
      return Immutable.set(state, 'geoRelationship', action.relationship)

    case GRANULE_UPDATE_TIME_RELATIONSHIP:
      return Immutable.set(state, 'timeRelationship', action.relationship)

    case GRANULE_UPDATE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: action.startDate,
        endDateTime: action.endDate,
      })

    case GRANULE_UPDATE_YEAR_RANGE:
      return Immutable.merge(state, {
        startYear: action.startYear,
        endYear: action.endYear,
      })

    case GRANULE_REMOVE_DATE_RANGE:
      return Immutable.merge(state, {
        startDateTime: initialState.startDateTime,
        endDateTime: initialState.endDateTime,
      })

    case GRANULE_REMOVE_YEAR_RANGE:
      return Immutable.merge(state, {
        startYear: initialState.startYear,
        endYear: initialState.endYear,
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

    case GRANULE_TOGGLE_ALL_TERMS_MUST_MATCH:
      return Immutable.set(state, 'allTermsMustMatch', !state.allTermsMustMatch)

    case RESET_GRANULE_ALL_TERMS_MUST_MATCH:
      return Immutable.set(
        state,
        'allTermsMustMatch',
        initialState.allTermsMustMatch
      )

    default:
      return state
  }
}

export default granuleFilter
