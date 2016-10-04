import fetch from 'isomorphic-fetch'
import { push } from 'react-router-redux'
import queryString from 'query-string'
import { facetsReceived } from './facet/FacetActions'

export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'
export const UPDATE_QUERY = 'update_query'
export const CLEAR_SEARCH = 'clear_search'

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

export const clearSearch = () => {
  return {
    type: CLEAR_SEARCH
  }
}


export const triggerSearch = (testing, procSelectedFacets) => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    let state = getState()

    if (state.getIn(['search', 'inFlight']) === true) {
      return Promise.resolve()
    }

    const searchBody = state.getIn(['search', 'requestBody'])
    // To avoid returning all results when hitting search w/empty fields
    if(!searchBody) {
      dispatch(push('/')) // Redirect to home
      return
    }
    dispatch(startSearch())

    // Append query to URL
    let parsedSearchBody = JSON.parse(searchBody)
    for (const key in parsedSearchBody){
      parsedSearchBody[key] = JSON.stringify(parsedSearchBody[key])
    }
    const urlQueryString = queryString.stringify(parsedSearchBody)
    dispatch(push('results?' + urlQueryString))

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

    return fetch(apiRoot, fetchParams)
        .then(response => response.json())
        .then(json => {
          dispatch(facetsReceived(json.meta, procSelectedFacets))
          dispatch(completeSearch(assignResourcesToMap(json.data)))
        })
  }
}

const assignResourcesToMap = (resourceList) => {
  var map = new Map()
  resourceList.forEach(resource => {
    map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
  })
  return map
}
