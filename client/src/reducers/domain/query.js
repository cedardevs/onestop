import Immutable from 'seamless-immutable'
import _ from 'lodash'
import { UPDATE_QUERY, GENERATE_COLLECTIONS_QUERY} from '../../search/SearchActions'
import { NEW_GEOMETRY } from '../../search/map/MapActions'
import { TOGGLE_FACET } from '../../search/facet/FacetActions'
import { DateRange } from '../../search//temporal/TemporalActions'

export const initialState = Immutable({
  queryText: '',
  geoJSON: {},
  startDateTime: '',
  endDateTime: '',
  facets: [],
  formatted: ''
})

export const query = (state = initialState, action) => {
  switch (action.type) {
    case NEW_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON.geometry)

    case UPDATE_QUERY:
      return Immutable.set(state, 'queryText', action.searchText)

    case TOGGLE_FACET:
      return Immutable.set(state, 'facets', action.selectedFacets)

    case DateRange.START_DATE:
      return Immutable.set(state, 'startDateTime', action.dateTime)

    case DateRange.END_DATE:
      return Immutable.set(state, 'endDateTime', action.dateTime)

    case GENERATE_COLLECTIONS_QUERY:
      return Immutable.merge(state, {formatted: buildFormattedQuery(state)})

    default:
      return state
  }
}

const buildFormattedQuery = (state) => {
  const queries = assembleQueries(state.queryText)
  const filters = assembleFilters(state)
  if(queries.length === 0 && filters.length === 0) {
    return ''
  } else { return JSON.stringify({queries, filters, facets: true})}
}

const assembleQueries = queryText => {
  let queries = []
  if (queryText) { queries.push({type: 'queryText', value: queryText}) }
  return queries
}

const assembleFilters = ({facets, geoJSON, startDateTime, endDateTime}) => {
  let filters = []
  _.forOwn(facets, (v,k) => {
    filters.push({'type':'facet', 'name': k, 'values': v})
  })
  if (!_.isEmpty(geoJSON.geometry)){ filters.push({type: 'geometry', geometry: geoJSON.geometry}) }
  if (startDateTime || endDateTime) { filters.push(dateTime(startDateTime, endDateTime)) }
  return filters
}

const dateTime = (startDateTime, endDateTime) => {
  if(startDateTime && endDateTime) {
    return {type: 'datetime', after: startDateTime, before: endDateTime}
  } else if (startDateTime) {
    return {type: 'datetime', after: startDateTime}
  } else {
    return {type: 'datetime', before: endDateTime}
  }
}

export default query
