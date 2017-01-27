import fetch from 'isomorphic-fetch'
import _ from 'lodash'
import { showLoading, hideLoading } from '../loading/LoadingActions'
import { showErrors } from '../error/ErrorActions'
import { facetsReceived, clearFacets } from '../search/facet/FacetActions'
import { assembleSearchRequest } from '../utils/queryUtils'

export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'
export const UPDATE_QUERY = 'update_query'
export const CLEAR_SEARCH = 'clear_search'
export const COUNT_HITS = 'count_hits'

export const updateQuery    = (searchText) => ({type: UPDATE_QUERY, searchText})
export const startSearch    = ()           => ({type: SEARCH})
export const completeSearch = (items)      => ({type: SEARCH_COMPLETE, items})
export const clearSearch    = ()           => ({type: CLEAR_SEARCH})
export const countHits      = (totalHits)  => ({type: COUNT_HITS, totalHits})

export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const FETCHING_GRANULES = 'fetching_granules'
export const FETCHED_GRANULES = 'fetched_granules'
export const CLEAR_GRANULES = 'clear_granules'

export const toggleGranuleFocus = (id)       => ({type: TOGGLE_GRANULE_FOCUS, id})
export const clearGranules      = ()         => ({type: CLEAR_GRANULES})
export const fetchingGranules   = ()         => ({type: FETCHING_GRANULES})
export const fetchedGranules    = (granules) => ({type: FETCHED_GRANULES, granules})

export const triggerSearch = (testing) => {
  const bodyBuilder = (state) => {
    const body = assembleSearchRequest(state)
    const inFlight = state.behavior.request.collectionInFlight
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (inFlight || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const prefetchHandler = (dispatch) => {
    dispatch(showLoading())
    dispatch(startSearch())
  }
  const successHandler = (dispatch, payload) => {
    const result = _.reduce(payload.data, (map, resource) => {
      return map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
    }, new Map())

    dispatch(facetsReceived(payload.meta))
    dispatch(countHits(payload.meta.total))
    dispatch(completeSearch(result))
    dispatch(hideLoading())
  }
  const errorHandler = (dispatch, e) => {
    dispatch(hideLoading())
    dispatch(showErrors(e.errors || e))
    dispatch(clearFacets())
    dispatch(completeSearch(new Map()))
  }

  return buildSearchAction(bodyBuilder, prefetchHandler, successHandler, errorHandler, testing)
}

export const fetchGranules = () => {
  const bodyBuilder = (state) => {
    const granuleInFlight = state.behavior.request.granuleInFlight
    let selectedCollections = state.behavior.search.selectedIds
    if (granuleInFlight || !selectedCollections) {
      return undefined
    }
    return assembleSearchRequest(state, true)
  }
  const prefetchHandler = (dispatch) => {
    dispatch(showLoading())
    dispatch(fetchingGranules())
  }
  const successHandler = (dispatch, payload) => {
    dispatch(fetchedGranules(payload.data))
    dispatch(hideLoading())
  }
  const errorHandler = (dispatch, e) => {
    dispatch(hideLoading())
    dispatch(showErrors(e.errors || e))
    dispatch(fetchedGranules([]))
  }

  return buildSearchAction(bodyBuilder, prefetchHandler, successHandler, errorHandler)
}


const buildSearchAction = (bodyBuilder, prefetchHandler, successHandler, errorHandler, testing) => {
  return (dispatch, getState) => {
    let state = getState()

    const body = bodyBuilder(state)
    if (!body) { // cannot or should not fetch
      return Promise.resolve()
    }

    prefetchHandler(dispatch)

    const host = testing || state.apiHost || ''
    const endpoint = host + "/onestop/api/search"
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    }

    return fetch(endpoint, fetchParams)
        .then(response => checkForErrors(response))
        .then(response => response.json())
        .then(json => successHandler(dispatch, json))
        .catch(ajaxError => ajaxError.response.json().then(errorJson => errorHandler(dispatch, errorJson)))
        .catch(jsError => errorHandler(dispatch, jsError))
  }
}

const checkForErrors = (response) => {
  if (response.status < 200 || response.status >= 400) {
    let error = new Error(response.statusText)
    error.response = response
    throw error
  } else {
    return response
  }
}
