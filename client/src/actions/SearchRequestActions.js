import fetch from 'isomorphic-fetch'
import _ from 'lodash'
import { showLoading, hideLoading } from './FlowActions'
import { showErrors } from './ErrorActions'
import { assembleSearchRequest } from '../utils/queryUtils'

export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'
export const COUNT_HITS = 'count_hits'

export const startSearch    = ()           => ({type: SEARCH})
export const completeSearch = (items)      => ({type: SEARCH_COMPLETE, items})
export const countHits      = (totalHits)  => ({type: COUNT_HITS, totalHits})

export const CLEAR_COLLECTIONS = 'clear_collections'
export const INCREMENT_COLLECTIONS_OFFSET = 'increment_collections_offset'

export const clearCollections = () => ({type: CLEAR_COLLECTIONS})
export const incrementCollectionsOffset = () => ({type: INCREMENT_COLLECTIONS_OFFSET})

export const FETCHING_GRANULES = 'fetching_granules'
export const FETCHED_GRANULES = 'fetched_granules'
export const CLEAR_GRANULES = 'clear_granules'
export const INCREMENT_GRANULES_OFFSET = 'increment_granules_offset'
export const COUNT_GRANULES = 'count_granules'

export const clearGranules           = ()              => ({type: CLEAR_GRANULES})
export const fetchingGranules        = ()              => ({type: FETCHING_GRANULES})
export const fetchedGranules         = (granules)      => ({type: FETCHED_GRANULES, granules})
export const incrementGranulesOffset = ()              => ({type: INCREMENT_GRANULES_OFFSET})
export const countGranules           = (totalGranules) => ({type: COUNT_GRANULES, totalGranules})

export const FACETS_RECEIVED = 'FACETS_RECEIVED'
export const CLEAR_FACETS = 'CLEAR_FACETS'

export const facetsReceived = (metadata) => ({type: FACETS_RECEIVED, metadata})
export const clearFacets    = ()         => ({type: CLEAR_FACETS})

export const triggerSearch = (retrieveFacets = true) => {
  const bodyBuilder = (state) => {
    const body = assembleSearchRequest(state, false, retrieveFacets)
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

    if(retrieveFacets) { dispatch(facetsReceived(payload.meta)) }
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

  return buildSearchAction(bodyBuilder, prefetchHandler, successHandler, errorHandler)
}

export const fetchGranules = () => {
  const bodyBuilder = (state) => {
    const granuleInFlight = state.behavior.request.granuleInFlight
    let selectedCollections = state.behavior.search.selectedIds
    if (granuleInFlight || !selectedCollections) {
      return undefined
    }
    return assembleSearchRequest(state, true, false)
  }
  const prefetchHandler = (dispatch) => {
    dispatch(showLoading())
    dispatch(fetchingGranules())
  }
  const successHandler = (dispatch, payload) => {
    dispatch(countGranules(payload.meta.total))
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


const buildSearchAction = (bodyBuilder, prefetchHandler, successHandler, errorHandler) => {
  return (dispatch, getState) => {
    let state = getState()

    const body = bodyBuilder(state)
    if (!body) { // cannot or should not fetch
      return Promise.resolve()
    }

    prefetchHandler(dispatch)

    const host = state.domain.config.apiHost || ''
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
