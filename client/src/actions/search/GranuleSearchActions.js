import {
  buildSearchAction,
} from './SearchActions'
import {
  assembleGranuleSearchRequest,
  // decodeQueryString,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  // collectionDetailGranulesRequest,
  // collectionDetailGranulesSuccess,
  // collectionDetailRequest,
  // collectionDetailSuccess,
  granuleSearchRequest,
  granuleSearchSuccess,
} from './GranuleRequestActions'
// import _ from 'lodash'
import {showErrors} from '../ErrorActions'
import {
  granuleClearFacets,
//   collectionClearSelectedIds,
//   collectionToggleSelectedId,
//   collectionUpdateFilters,
} from './GranuleFilterActions'
// import {
//   apiPath,
//   getCollectionIdFromDetailPath,
//   getCollectionIdFromGranuleListPath,
// } from '../../utils/urlUtils'
// import {checkForErrors} from '../../utils/responseUtils'
import {
//   collectionClearDetailGranulesResult,
//   collectionClearResults,
  granuleMetadataReceived,
//   collectionUpdateDetailGranulesTotal,
  granuleUpdateTotal,
} from './GranuleResultActions'
// import {fetchConfig} from '../ConfigActions'
// import {fetchCounts, fetchInfo} from './InfoActions'

// synchronous actions
// export const LOADING_SHOW = 'LOADING_SHOW'
// export const showLoading = () => {
//   return {
//     type: LOADING_SHOW,
//   }
// }
//
// export const LOADING_HIDE = 'LOADING_HIDE'
// export const hideLoading = () => {
//   return {
//     type: LOADING_HIDE,
//   }
// }

export const triggerGranuleSearch = (retrieveFacets = true) => {
  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, retrieveFacets)
    const inFlight =
      state.search.granuleRequest.granuleSearchRequestInFlight
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (inFlight || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const prefetchHandler = dispatch => {
    // dispatch(showLoading()) // TODO detangle loading states!
    dispatch(granuleSearchRequest())
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
      dispatch(granuleMetadataReceived(payload.meta))
    }
    dispatch(granuleUpdateTotal(payload.meta.total))
    console.log("trigger granule search (new path)")
    dispatch(granuleSearchSuccess(payload.data))
    // dispatch(hideLoading()) // TODO detangle loading states!
  }
  const errorHandler = (dispatch, e) => {
    // dispatch(hideLoading()) // TODO detangle loading states!
    dispatch(showErrors(e.errors || e))
    dispatch(granuleClearFacets())
    dispatch(granuleSearchSuccess(new Map()))
  }

  return buildSearchAction(
    'granule',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

// export const fetchGranules = () => {
//   const bodyBuilder = state => {
//     const granuleInFlight =
//       state.search.collectionRequest.collectionDetailGranulesRequestInFlight
//     let selectedCollections = state.search.collectionFilter.selectedIds
//     if (granuleInFlight || !selectedCollections) {
//       return undefined
//     }
//     return assembleSearchRequest(state, true, false)
//   }
//   const prefetchHandler = dispatch => {
//     dispatch(showLoading())
//     dispatch(collectionDetailGranulesRequest())
//   }
//   const successHandler = (dispatch, payload) => {
//     dispatch(collectionUpdateDetailGranulesTotal(payload.meta.total))
//     dispatch(collectionDetailGranulesSuccess(payload.data))
//     dispatch(hideLoading())
//   }
//   const errorHandler = (dispatch, e) => {
//     dispatch(hideLoading())
//     dispatch(showErrors(e.errors || e))
//     dispatch(collectionDetailGranulesSuccess([]))
//   }
//
//   return buildSearchAction(
//     'granule',
//     bodyBuilder,
//     prefetchHandler,
//     successHandler,
//     errorHandler
//   )
// }
//
// export const getCollection = collectionId => {
//   const prefetchHandler = dispatch => {
//     dispatch(showLoading())
//     dispatch(collectionDetailRequest(collectionId))
//   }
//
//   const successHandler = (dispatch, payload) => {
//     dispatch(collectionDetailSuccess(payload.data[0], payload.meta))
//     dispatch(hideLoading())
//   }
//
//   const errorHandler = (dispatch, e) => {
//     dispatch(hideLoading())
//     dispatch(showErrors(e.errors || e))
//     dispatch(collectionDetailSuccess(null))
//   }
//
//   return buildGetAction(
//     'collection',
//     collectionId,
//     prefetchHandler,
//     successHandler,
//     errorHandler
//   )
// }
//
// const buildGetAction = (
//   endpointName,
//   id,
//   prefetchHandler,
//   successHandler,
//   errorHandler
// ) => {
//   return dispatch => {
//     prefetchHandler(dispatch)
//     const endpoint = apiPath() + '/' + endpointName + '/' + id
//     const fetchParams = {
//       method: 'GET',
//       headers: {
//         Accept: 'application/json',
//       },
//     }
//
//     return fetch(endpoint, fetchParams)
//       .then(response => checkForErrors(response))
//       .then(responseChecked => responseChecked.json())
//       .then(json => successHandler(dispatch, json))
//       .catch(ajaxError =>
//         ajaxError.response
//           .json()
//           .then(errorJson => errorHandler(dispatch, errorJson))
//       )
//       .catch(jsError => errorHandler(dispatch, jsError))
//   }
// }
//
// export const getSitemap = () => {
//   return buildSitemapAction()
// }
//
// const buildSitemapAction = () => {
//   return dispatch => {
//     const endpoint = apiPath() + '/sitemap.xml'
//     const fetchParams = {
//       method: 'GET',
//     }
//     return (
//       fetch(endpoint, fetchParams)
//         .then(response => checkForErrors(response))
//         // TODO: can we leverage dispatch here to use router like we are elsewhere instead of window.location.href?
//         .then(response => (window.location.href = response.url))
//     )
//   }
// }
//
// export const loadCollections = newQueryString => {
//   return (dispatch, getState) => {
//     if (newQueryString.indexOf('?') === 0) {
//       newQueryString = newQueryString.slice(1)
//     }
//     const searchFromQuery = decodeQueryString(newQueryString)
//     const searchFromState = _.get(getState(), 'search.collectionFilter')
//     if (!_.isEqual(searchFromQuery, searchFromState)) {
//       dispatch(collectionClearResults())
//       dispatch(collectionClearDetailGranulesResult())
//       dispatch(collectionClearSelectedIds())
//       dispatch(collectionUpdateFilters(searchFromQuery))
//       dispatch(triggerSearch())
//     }
//   }
// }
//
// export const initialize = () => {
//   return dispatch => {
//     dispatch(fetchConfig())
//     dispatch(fetchInfo())
//     dispatch(fetchCounts())
//   }
// }
//
export const showGranules = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    // dispatch(collectionClearSelectedIds()) TODO leaving this alone since we specifically do *not* want to reset the selected collection ID for granule searching, I think
    const query = encodeQueryString(getState(), 'granuleFilter') //TODO put the id in there too?
    // if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
        search: !_.isEmpty(query) ? `?${query}` : '',
      }
      history.push(locationDescriptor)
    // }
  }
}
//
// export const showGranulesList = (history, id) => {
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
//
// export const loadGranulesList = path => {
//   return dispatch => {
//     const detailId = getCollectionIdFromGranuleListPath(path)
//     dispatch(getCollection(detailId))
//     dispatch(collectionClearSelectedIds())
//     dispatch(collectionToggleSelectedId(detailId))
//     dispatch(collectionClearDetailGranulesResult())
//     dispatch(fetchGranules())
//   }
// }
//
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
//
// export const showHome = history => {
//   return dispatch => {
//     dispatch(collectionUpdateFilters())
//     history.push('/')
//     dispatch(collectionClearResults())
//   }
// }
