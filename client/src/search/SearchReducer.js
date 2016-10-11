import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE, UPDATE_QUERY, CLEAR_SEARCH} from './SearchActions'
import { NEW_GEOMETRY, REMOVE_GEOMETRY } from './map/MapActions'
import { MODIFY_SELECTED_FACETS, CLEAR_FACETS } from './facet/FacetActions'
import { DateRange } from './temporal/TemporalActions'

export const initialState = Immutable.fromJS({
  text: '',
  geoJSON: null,
  inFlight: false,
  startDateTime: '',
  endDateTime: '',
  requestBody: '',
  selectedFacets: Immutable.Map()
})

export const search = (state = initialState, action) => {
  let newState

  switch (action.type) {
    case SEARCH:
      return state.mergeDeep({
        inFlight: true
      })

    case SEARCH_COMPLETE:
      return state.mergeDeep({
        inFlight: false
      })

    case NEW_GEOMETRY:
      newState = state.mergeDeep({geoJSON: action.geoJSON})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case REMOVE_GEOMETRY:
      newState = state.mergeDeep({geoJSON: initialState.get('geoJSON')})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case UPDATE_QUERY:
      newState = state.mergeDeep({text: action.searchText})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case MODIFY_SELECTED_FACETS:
      newState = state.set('selectedFacets', (action.selectedFacets ?
        action.selectedFacets : initialState.selectedFacets))
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case CLEAR_FACETS:
      newState = state.set('selectedFacets', initialState.get('selectedFacets'))
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case DateRange.START_DATE:
      newState = state.mergeDeep({startDateTime: action.datetime})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case DateRange.END_DATE:
      newState = state.mergeDeep({endDateTime: action.datetime})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

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
  let queryText = state.get('text')
  if (queryText) {
    queries.push({type: 'queryText', value: queryText})
  }

  // Facets
  let categories = state.get('selectedFacets')
  if ( categories.size ) {
    categories.forEach((v,k) => {
      filters.push({"type":"facet","name": k,"values": Object.keys(v.toJS())})
    })
  }

  // Spatial filter:
  let geoJSON = state.get('geoJSON')
  if (geoJSON){
    filters.push({type: 'geometry', geometry: geoJSON.toJS().geometry})
  }

  // Temporal filter:
  let startDateTime = state.get('startDateTime')
  let endDateTime = state.get('endDateTime')
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
