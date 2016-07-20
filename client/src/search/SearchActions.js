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

    let filters = []
    const geometry = state.getIn(['search', 'geometry'])
    if (geometry !== ""){
      filters.push(
        { type: 'geometry', geometry: geometry.toJS() }
      )
    }
    let startDateTime = state.getIn(['search', 'startDateTime'])
    let endDateTime = state.getIn(['search', 'endDateTime'])
    if (startDateTime) {
      if (endDateTime) {
        endDateTime = moment().format()
      }
      filters.push(
        { type: 'datetime', after: startDateTime, before: endDateTime }
      )
    }

    const queries = []
    const queryText = state.getIn(['search', 'text'])
    if (queryText) {
      queries.push({type: 'queryText', value: queryText})
    }

    const searchBody = JSON.stringify({queries, filters})

    // { type: 'datetime', after: startDateTime, before: endDateTime }
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

const filterGeometry = (state) => {
  let geometry = state.getIn(['search', 'geometry'])
  if (geometry !== ""){
    filters.push(
      { type: 'geometry', geometry: geometry.toJS() }
    )
  }
}

const filterDates = (state) => {

}
