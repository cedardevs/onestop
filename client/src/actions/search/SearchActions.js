import {
  assembleSearchRequest,
  decodeQueryString,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  collectionDetailGranulesRequest,
  collectionDetailGranulesSuccess,
  collectionDetailRequest,
  collectionDetailSuccess,
  collectionSearchRequest,
  collectionSearchSuccess,
} from './CollectionRequestActions'
import _ from 'lodash'
import {showErrors} from '../ErrorActions'
import {
  collectionClearFacets,
  collectionClearSelectedIds,
  collectionToggleSelectedId,
  collectionUpdateFilters,
} from './CollectionFilterActions'
import {
  apiPath,
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'
import {
  collectionClearDetailGranulesResult,
  collectionClearResults,
  collectionMetadataReceived,
  collectionUpdateDetailGranulesTotal,
  collectionUpdateTotal,
} from './CollectionResultActions'
import {fetchConfig} from '../ConfigActions'
import {fetchCounts, fetchInfo} from './InfoActions'

// synchronous actions
export const LOADING_SHOW = 'LOADING_SHOW'
export const showLoading = () => {
  return {
    type: LOADING_SHOW,
  }
}

export const LOADING_HIDE = 'LOADING_HIDE'
export const hideLoading = () => {
  return {
    type: LOADING_HIDE,
  }
}

export const triggerSearch = (retrieveFacets = true) => {
  const bodyBuilder = state => {
    const body = assembleSearchRequest(state, false, retrieveFacets)
    const inFlight =
      state.search.collectionRequest.collectionSearchRequestInFlight
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (inFlight || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const prefetchHandler = dispatch => {
    dispatch(showLoading())
    dispatch(collectionSearchRequest())
  }
  const successHandler = (dispatch, payload) => {
    const result = _.reduce(
      payload.data,
      (map, resource) => {
        return map.set(
          resource.id,
          _.assign({type: resource.type}, resource.attributes)
        )
      },
      new Map()
    )

    if (retrieveFacets) {
      dispatch(collectionMetadataReceived(payload.meta))
    }
    dispatch(collectionUpdateTotal(payload.meta.total))
    dispatch(collectionSearchSuccess(result))
    dispatch(hideLoading())
  }
  const errorHandler = (dispatch, e) => {
    dispatch(hideLoading())
    dispatch(showErrors(e.errors || e))
    dispatch(collectionClearFacets())
    dispatch(collectionSearchSuccess(new Map()))
  }

  return buildSearchAction(
    'collection',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

export const fetchGranules = () => {
  const bodyBuilder = state => {
    const granuleInFlight =
      state.search.collectionRequest.collectionDetailGranulesRequestInFlight
    let selectedCollections = state.search.collectionFilter.selectedIds
    if (granuleInFlight || !selectedCollections) {
      return undefined
    }
    return assembleSearchRequest(state, true, false)
  }
  const prefetchHandler = dispatch => {
    dispatch(showLoading())
    dispatch(collectionDetailGranulesRequest())
  }
  const successHandler = (dispatch, payload) => {
    dispatch(collectionUpdateDetailGranulesTotal(payload.meta.total))
    dispatch(collectionDetailGranulesSuccess(payload.data))
    dispatch(hideLoading())
  }
  const errorHandler = (dispatch, e) => {
    dispatch(hideLoading())
    dispatch(showErrors(e.errors || e))
    dispatch(collectionDetailGranulesSuccess([]))
  }

  return buildSearchAction(
    'granule',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

const buildSearchAction = (
  endpointName,
  bodyBuilder,
  prefetchHandler,
  successHandler,
  errorHandler
) => {
  return (dispatch, getState) => {
    let state = getState()

    const body = bodyBuilder(state)
    if (!body) {
      // cannot or should not fetch
      return Promise.resolve()
    }

    prefetchHandler(dispatch)

    const endpoint = apiPath() + '/search/' + endpointName

    const fetchParams = {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    }

    return fetch(endpoint, fetchParams)
      .then(response => checkForErrors(response))
      .then(response => response.json())
      .then(json => successHandler(dispatch, json))
      .catch(ajaxError => {
        return ajaxError.response
          .json()
          .then(errorJson => errorHandler(dispatch, errorJson))
      })
      .catch(jsError => errorHandler(dispatch, jsError))
  }
}

export const getCollection = collectionId => {
  const prefetchHandler = dispatch => {
    dispatch(showLoading())
    dispatch(collectionDetailRequest(collectionId))
  }

  const successHandler = (dispatch, payload) => {
    dispatch(collectionDetailSuccess(payload.data[0], payload.meta))
    dispatch(hideLoading())
  }

  const errorHandler = (dispatch, e) => {
    dispatch(hideLoading())
    dispatch(showErrors(e.errors || e))
    dispatch(collectionDetailSuccess(null))
  }

  return buildGetAction(
    'collection',
    collectionId,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

const buildGetAction = (
  endpointName,
  id,
  prefetchHandler,
  successHandler,
  errorHandler
) => {
  return dispatch => {
    prefetchHandler(dispatch)
    const endpoint = apiPath() + '/' + endpointName + '/' + id
    const fetchParams = {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
    }

    return fetch(endpoint, fetchParams)
      .then(response => checkForErrors(response))
      .then(responseChecked => responseChecked.json())
      .then(json => successHandler(dispatch, json))
      .catch(ajaxError =>
        ajaxError.response
          .json()
          .then(errorJson => errorHandler(dispatch, errorJson))
      )
      .catch(jsError => errorHandler(dispatch, jsError))
  }
}

export const getSitemap = () => {
  return buildSitemapAction()
}

const buildSitemapAction = () => {
  return dispatch => {
    const endpoint = apiPath() + '/sitemap.xml'
    const fetchParams = {
      method: 'GET',
    }
    return (
      fetch(endpoint, fetchParams)
        .then(response => checkForErrors(response))
        // TODO: can we leverage dispatch here to use router like we are elsewhere instead of window.location.href?
        .then(response => (window.location.href = response.url))
    )
  }
}

export const loadCollections = newQueryString => {
  return (dispatch, getState) => {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.collectionFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      dispatch(collectionClearResults())
      dispatch(collectionClearDetailGranulesResult())
      dispatch(collectionClearSelectedIds())
      dispatch(collectionUpdateFilters(searchFromQuery))
      dispatch(triggerSearch())
    }
  }
}

export const initialize = () => {
  return dispatch => {
    dispatch(fetchConfig())
    dispatch(fetchInfo())
    dispatch(fetchCounts())
  }
}

export const showCollections = history => {
  return (dispatch, getState) => {
    dispatch(collectionClearSelectedIds())
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: '/collections',
        search: `?${query}`,
      }
      history.push(locationDescriptor)
    }
  }
}

export const showGranulesList = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`,
      search: `?${query}`,
    }
    history.push(locationDescriptor)
  }
}

export const loadGranulesList = path => {
  return dispatch => {
    const detailId = getCollectionIdFromGranuleListPath(path)
    dispatch(getCollection(detailId))
    dispatch(collectionClearSelectedIds())
    dispatch(collectionToggleSelectedId(detailId))
    dispatch(collectionClearDetailGranulesResult())
    dispatch(fetchGranules())
  }
}

export const showDetails = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    const locationDescriptor = {
      pathname: `/collections/details/${id}`,
      search: _.isEmpty(query) ? null : `?${query}`,
    }
    history.push(locationDescriptor)
  }
}

export const loadDetails = path => {
  return (dispatch, getState) => {
    if (!getState().search.collectionRequest.collectionDetailRequestInFlight) {
      const detailId = getCollectionIdFromDetailPath(path)
      dispatch(getCollection(detailId))
    }
  }
}

export const showHome = history => {
  return dispatch => {
    dispatch(collectionUpdateFilters())
    history.push('/')
    dispatch(collectionClearResults())
  }
}
