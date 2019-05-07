import {
  assembleSearchRequest,
  assembleGranuleSearchRequest,
  decodeQueryString,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  // collectionDetailGranulesRequest,
  // collectionDetailGranulesSuccess,
  collectionDetailRequest,
  collectionDetailSuccess,
  collectionSearchRequest,
  collectionSearchSuccess,
} from './CollectionRequestActions'

import {
  granuleSearchRequest,
  granuleSearchSuccess,
  granuleSearchStart,
  granuleSearchComplete,
  granuleSearchError,
} from './GranuleRequestActions'
import {
  triggerGranuleSearch
} from './GranuleSearchActions'
import {
  // collectionClearFacets,
  // collectionClearSelectedIds,
  // collectionToggleSelectedId,
  granuleUpdateFilters,
} from './GranuleFilterActions'
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
  // collectionUpdateDetailGranulesTotal,
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

export const buildSearchAction = (
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
      .then(response => {
        return response.json()})
      .then(json => successHandler(dispatch, json))
      .catch(ajaxError => {
        // TODO how to handle when ajaxError doesn't have response.json()...????
        if (ajaxError.response) {
        return ajaxError.response
          .json()
          .then(errorJson => errorHandler(dispatch, errorJson))
        }
        //return error
        errorHandler(dispatch, ajaxError)
          // : ajaxError
      })
      .catch(jsError => errorHandler(dispatch, jsError))
  }
}

export const buildGetAction = (
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

export const initialize = () => {
  return dispatch => {
    dispatch(fetchConfig())
    dispatch(fetchInfo())
    dispatch(fetchCounts())
  }
}

// export const showCollections = history => {
//   return (dispatch, getState) => {
//     dispatch(collectionClearSelectedIds())
//     const query = encodeQueryString(getState())
//     if (!_.isEmpty(query)) {
//       const locationDescriptor = {
//         pathname: '/collections',
//         search: `?${query}`,
//       }
//       history.push(locationDescriptor)
//     }
//   }
// }

// export const showGranulesList = (history, id) => { // TODO replace with showGranules from GranuleSearchActions? need to decombobulate the extra layer of state though...
//   if (!id) {
//     return
//   }
//   return (dispatch, getState) => {
//     const query = encodeQueryString(getState())
//     const locationDescriptor = {
//       pathname: `/collections/granules/${id}`,
//       search: `?${query}`,
//     }
//     history.push(locationDescriptor)
//   }
// }

// export const loadGranulesList = (path, newQueryString) => {
//
//   return (dispatch, getState) => {
//
//     if (newQueryString.indexOf('?') === 0) {
//       newQueryString = newQueryString.slice(1)
//     }
//     const searchFromQuery = decodeQueryString(newQueryString)
//     const searchFromState = _.get(getState(), 'search.granuleFilter')
//     if (!_.isEqual(searchFromQuery, searchFromState)) {
//       const detailId = getCollectionIdFromGranuleListPath(path)
//       dispatch(getCollection(detailId))
//       dispatch(collectionClearSelectedIds())
//       dispatch(collectionToggleSelectedId(detailId))
//       dispatch(collectionClearDetailGranulesResult())
//       dispatch(granuleUpdateFilters(searchFromQuery))
//       dispatch(triggerGranuleSearch())
//     }
//   }
// }

// export const showDetails = (history, id) => {
//   if (!id) {
//     return
//   }
//   return (dispatch, getState) => {
//     const query = encodeQueryString(getState())
//     const locationDescriptor = {
//       pathname: `/collections/details/${id}`,
//       search: _.isEmpty(query) ? null : `?${query}`,
//     }
//     history.push(locationDescriptor)
//   }
// }
//
// export const loadDetails = path => {
//   return (dispatch, getState) => {
//     if (!getState().search.collectionRequest.collectionDetailRequestInFlight) {
//       const detailId = getCollectionIdFromDetailPath(path)
//       dispatch(getCollection(detailId))
//     }
//   }
// }

export const showHome = history => {
  return dispatch => {
    dispatch(collectionUpdateFilters())
    history.push('/')
    dispatch(collectionClearResults())
  }
}
