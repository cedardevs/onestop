import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE, UPDATE_QUERY} from './SearchActions'
import { UPDATE_GEOMETRY } from './map/MapActions'
import { UPDATE_FACETS, CLEAR_FACETS } from './facet/FacetActions'
import { DateRange } from './temporal/TemporalActions'

export const initialState = Immutable.fromJS({
  text: '',
  geoJSON: null,
  inFlight: false,
  startDateTime: '',
  endDateTime: '',
  requestBody: '',
  facets: Immutable.Map()
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

    case UPDATE_GEOMETRY:
      newState = state.mergeDeep({geoJSON: action.geoJSON})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case UPDATE_QUERY:
      newState = state.mergeDeep({text: action.searchText})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case UPDATE_FACETS:
      newState = updateFacets(action.facet, state)
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case CLEAR_FACETS:
      newState = state.set('facets', initialState.get('facets'))
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case DateRange.START_DATE:
      newState = state.mergeDeep({startDateTime: action.datetime})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

    case DateRange.END_DATE:
      newState = state.mergeDeep({endDateTime: action.datetime})
      return newState.mergeDeep({requestBody: assembleRequestBody(newState)})

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
  let categories = state.get('facets')
  let catIterator = categories.entries()
  let entry = catIterator.next()
  while (!entry.done){
    const name = entry.value[0]
    const values = entry.value[1].toJS()
    filters.push({"type":"facet","name": name,"values": values})
    entry = catIterator.next()
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

const updateFacets = (facet, state) => {
  const {name, value, selected} = facet
  let categories = state.get('facets')

  if (selected){
    const oldValues = categories.get(name) ? categories.get(name) : Immutable.List()
    const newValues = Immutable.Set().merge(oldValues, Immutable.List([value]))
    categories = categories.setIn([name], newValues)
  } else {
    categories = categories.set(name, categories.get(name).filterNot(x => x === value))
    categories = categories.filter(x => x.size)
  }
  return Immutable.Map().setIn(['facets'], categories)
}
