import fetch from 'isomorphic-fetch'
import { push } from 'react-router-redux'
import queryString from 'query-string'

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

export const triggerSearch = (queryParams, testing) => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    let state = getState()

    if (state.getIn(['search', 'inFlight']) === true) {
      return Promise.resolve()
    }

    const searchBody = queryParams || state.getIn(['search', 'requestBody'])
    if(!searchBody) { return } // To avoid returning all results when hitting search w/empty fields

    dispatch(startSearch())

    let apiRoot = "/api/search"
    if(testing) { apiRoot = testing + apiRoot }
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: searchBody
    }
    // Append query to URL
    let parsedSearchBody = JSON.parse(searchBody)
    for (const key in parsedSearchBody){
      parsedSearchBody[key] = JSON.stringify(parsedSearchBody[key])
    }
    const urlQueryString = queryString.stringify(parsedSearchBody)

    return fetch(apiRoot, fetchParams)
        .then(response => response.json())
        .then((json) => { setTimeout(() => { dispatch(completeSearch(assignResourcesToMap(json.data))) }, 1000) })
        //.then(json => dispatch(completeSearch(assignResourcesToMap(json.data)))) // ^^ Added a 1-second delay for demo purposes
        .then(() => dispatch(push('results?' + urlQueryString)))
  }
}

const assignResourcesToMap = (resourceList) => {
  var map = new Map()
  resourceList.forEach(resource => {
    map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
  })
  return map
}
