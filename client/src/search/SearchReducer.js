import Immutable from 'seamless-immutable'
import _ from 'lodash'
import {SEARCH, SEARCH_COMPLETE, UPDATE_QUERY, CLEAR_SEARCH} from './SearchActions'
import { NEW_GEOMETRY, REMOVE_GEOMETRY } from './map/MapActions'
import { TOGGLE_FACET, CLEAR_FACETS } from './facet/FacetActions'
import { DateRange } from './temporal/TemporalActions'

export const initialState = Immutable({
  text: '',
  geoJSON: null,
  inFlight: false,
  startDateTime: '',
  endDateTime: '',
  requestBody: '',
  selectedFacets: {}
})

export const search = (state = initialState, action) => {
  let newState

  switch (action.type) {
    case SEARCH:
      return Immutable.merge(state, {
        inFlight: true
      })

    case SEARCH_COMPLETE:
      return Immutable.merge(state, {
        inFlight: false
      })

    case NEW_GEOMETRY:
      newState = Immutable.merge(state, {geoJSON: action.geoJSON}, {deep: true})
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case REMOVE_GEOMETRY:
      newState = Immutable.merge(state, {geoJSON: initialState.geoJSON}, {deep: true})
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case UPDATE_QUERY:
      newState = Immutable.merge(state, {text: action.searchText}, {deep: true})
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case TOGGLE_FACET:
      newState = Immutable.set(state, 'selectedFacets', (action.selectedFacets ?
        action.selectedFacets : initialState.selectedFacets))
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case CLEAR_FACETS:
      newState = Immutable.set(state, 'selectedFacets', initialState.selectedFacets)
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case DateRange.START_DATE:
      newState = Immutable.merge(state, {startDateTime: action.datetime}, {deep: true})
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case DateRange.END_DATE:
      newState = Immutable.merge(state, {endDateTime: action.datetime}, {deep: true})
      return Immutable.merge(newState, {requestBody: assembleRequestBody(newState)}, {deep: true})

    case CLEAR_SEARCH:
      return initialState

    default:
      return state
  }
}

export default search

const assembleRequestBody = (state) => {

  let queries = []
  let filters = []

  // Query:
  let queryText = state.text
  if (queryText) {
    queries.push({type: 'queryText', value: queryText})
  }

  // Facets
  let categories = state.selectedFacets
  _.forOwn(categories, (v,k) => {
    filters.push({"type":"facet","name": k,"values": v})
  })

  // Spatial filter:
  let geoJSON = state.geoJSON
  if (geoJSON){
    filters.push({type: 'geometry', geometry: geoJSON.geometry})
  }

  // Temporal filter:
  let startDateTime = state.startDateTime
  let endDateTime = state.endDateTime
  if(startDateTime || endDateTime) {
    filters.push(dateTime(startDateTime, endDateTime))
  }

  if(queries.length === 0 && filters.length === 0) {
    return ''
  } else {
    return JSON.stringify({queries, filters, facets: true})
  }
}

const dateTime = (startDateTime, endDateTime) => {

  if(startDateTime && endDateTime) {
    return {type: 'datetime', after: startDateTime, before: endDateTime}
  } else if(startDateTime) {
    return {type: 'datetime', after: startDateTime}
  } else {
    return {type: 'datetime', before: endDateTime}
  }
}
