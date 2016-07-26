import fetch from 'isomorphic-fetch'
import { push } from 'react-router-redux'
import moment from 'moment'

export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'
export const UPDATE_QUERY = 'update_query'

export const updateQuery = (searchText) => {
  return {
    type: UPDATE_QUERY,
    searchText
  }
}

export const startSearch = () => {
  return {
    type: SEARCH
  }
}

export const completeSearch = (items) => {
  return {
    type: SEARCH_COMPLETE,
    items
  }
}

export const triggerSearch = () => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    let state = getState()

    if (state.getIn(['search', 'inFlight']) === true) {
      return Promise.resolve()
    }
    dispatch(startSearch())

    const geoJSON = state.getIn(['search', 'geoJSON'])
    let filters = []
    if (geoJSON !== ""){
      filters.push(
        { type: 'geometry', geometry: geoJSON.toJS().geometry }
      )
    }

    const queries = []
    const queryText = state.getIn(['search', 'text'])
    if (queryText) {
      queries.push({type: 'queryText', value: queryText})
    }

    // datetime filters 
    let startDateTime = state.getIn(['search', 'startDateTime'])
    let endDateTime = state.getIn(['search', 'endDateTime'])
    filters.push(dateTime(startDateTime, endDateTime))

    const searchBody = JSON.stringify({queries, filters})

    const apiRoot = "/api/search"
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: searchBody
    }

    return fetch(apiRoot, fetchParams)
        .then(response => response.json())
        .then(json => dispatch(completeSearch(assignResourcesToMap(json.data))))
        .then(() => dispatch(push('results')))
  }
}

const assignResourcesToMap = (resourceList) => {
  var map = new Map()
  resourceList.forEach(resource => {
    map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
  })
  return map
}

export const dateTime = (startDateTime, endDateTime) => {
  let defaultDate = moment().format()
  // start datetime != null where end datetime is either null or not
  if (startDateTime) {
    if (endDateTime) {
      return  { type: 'datetime', after: startDateTime, before: endDateTime }
    } else{
      endDateTime = defaultDate
      return  { type: 'datetime', after: startDateTime, before: endDateTime }
    }
  }
  // if start datetime ==null  where end datetime is either null or not
  else if (startDateTime ==="") {
    if (endDateTime) {
      return  { type: 'datetime', before: endDateTime }
    } else{
      endDateTime = defaultDate
      return  { type: 'datetime', before: endDateTime }
    }
  }
  // where end and start date time  is null
  else {
      endDateTime = defaultDate
      return  { type: 'datetime', before: endDateTime }
    }
}
