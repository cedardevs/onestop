import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE, UPDATE_QUERY, ASSEMBLE_SEARCH} from './SearchActions'
import { UPDATE_GEOMETRY } from './map/MapActions'
import { DateRange } from './temporal/TemporalActions'

export const initialState = Immutable.fromJS({
  text: '',
  geoJSON: '',
  inFlight: false,
  startDateTime: '',
  endDateTime: '',
  search: {queries: [], filters: []}
})

export const search = (state = initialState, action) => {

  let newState = {}

  switch (action.type) {
    case SEARCH:
      return state.mergeDeep({
        inFlight: true
      })

    case SEARCH_COMPLETE:
      return state.mergeDeep({
        inFlight: false
      })

    case UPDATE_GEOMETRY:
      newState = state.mergeDeep({geoJSON: action.geoJSON})
      return newState.mergeDeep({search: assembleSearchBody(newState)})

    case UPDATE_QUERY:
      newState = state.mergeDeep({text: action.searchText})
      return newState.mergeDeep({search: assembleSearchBody(newState)})

    case DateRange.START_DATE:
      newState = state.mergeDeep({startDateTime: action.datetime})
      return newState.mergeDeep({search: assembleSearchBody(newState)})

    case DateRange.END_DATE:
      newState = state.mergeDeep({endDateTime: action.datetime})
      return newState.mergeDeep({search: assembleSearchBody(newState)})

    default:
      return state
  }
}

export default search

const assembleSearchBody = (state) => {

  let queries = []
  let filters = []

  // Query:
  let queryText = state.get('text')
  if (queryText) {
    queries.push({type: 'queryText', value: queryText})
  }

  // Spatial filter:
  let geoJSON = state.get('geoJSON')
  if (geoJSON !== ""){
    filters.push({type: 'geometry', geometry: geoJSON.toJS().geometry})
  }

  // Temporal filter:
  let startDateTime = state.get('startDateTime')
  let endDateTime = state.get('endDateTime')
  if(startDateTime || endDateTime) {
    filters.push(dateTime(startDateTime, endDateTime))
  }

  return JSON.stringify({queries, filters})
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