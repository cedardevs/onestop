import fetch from 'isomorphic-fetch'
import { push } from 'react-router-redux'
import _ from 'lodash'
import { showLoading, hideLoading } from '../loading/LoadingActions'
import { showErrors } from '../error/ErrorActions'
import queryString from 'query-string'
import { facetsReceived, clearFacets } from './facet/FacetActions'
import store from '../store'

export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'
export const UPDATE_QUERY = 'update_query'
export const CLEAR_SEARCH = 'clear_search'
export const COUNT_HITS = 'count_hits'

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
    view: 'collections',
    items,
    //TODO: This will be unnecessary when search helper functions are moved to actions
    appState: store.getState().search.requestBody
  }
}

export const clearSearch = () => {
  return {
    type: CLEAR_SEARCH
  }
}

export const countHits = (totalHits) => {
  return {
    type: COUNT_HITS,
    totalHits
  }
}


export const triggerSearch = (testing) => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    let state = getState()

    if (state.search.inFlight === true) {
      return Promise.resolve()
    }

    const searchBody = state.search.requestBody
    // To avoid returning all results when hitting search w/empty fields
    if(!searchBody) {
      return
    }
    dispatch(showLoading())
    dispatch(startSearch())

    let apiRoot = "/onestop/api/search"
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
        .then(response => {
          if (response.status < 200 || response.status >= 400) {
            var error = new Error(response.statusText)
            error.response = response
            throw error
          } else {
            return response
          }
        })
        .then(response => response.json())
        .then(json => {
          dispatch(facetsReceived(json.meta))
          dispatch(countHits(json.meta.total))
          dispatch(completeSearch(assignResourcesToMap(json.data)))
          dispatch(hideLoading())
        })
        .catch(ajaxError => ajaxError.response.json().then(errorJson => handleErrors(dispatch, errorJson)))
        .catch(jsError => handleErrors(dispatch, jsError))
  }
}

const assignResourcesToMap = (resourceList) => {
  var map = new Map()
  _.forOwn(resourceList, resource => {
    map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
  })
  return map
}

const buildQueryString = (searchBody) => {
  // Append query to URL
  let parsedSearchBody = JSON.parse(searchBody)
  for (const key in parsedSearchBody) {
    parsedSearchBody[key] = JSON.stringify(parsedSearchBody[key])
  }
  return queryString.stringify(parsedSearchBody)
}

const handleErrors = (dispatch, e) => {
  dispatch(hideLoading())
  dispatch(showErrors(e.errors || e))
  dispatch(clearFacets())
  dispatch(completeSearch(assignResourcesToMap([])))
}
