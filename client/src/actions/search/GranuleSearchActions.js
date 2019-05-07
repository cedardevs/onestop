import {
  buildSearchAction,
} from './SearchActions'
import {
  assembleGranuleSearchRequest,
  decodeQueryString,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  // collectionDetailGranulesRequest,
  // collectionDetailGranulesSuccess,
  // collectionDetailRequest,
  // collectionDetailSuccess,
  granuleSearchStart,
  granuleSearchComplete,
  granuleSearchError,

} from './GranuleRequestActions'
// import _ from 'lodash'
import {showErrors} from '../ErrorActions'
import {
  granuleClearFacets,
  // collectionClearSelectedIds,
  // collectionToggleSelectedId,
  granuleUpdateFilters,
//   collectionUpdateFilters,
} from './GranuleFilterActions'
import {
  collectionClearSelectedIds,
  collectionToggleSelectedId,
//   collectionUpdateFilters,
} from './CollectionFilterActions'
import {
//   apiPath,
//   getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../../utils/urlUtils'
// import {checkForErrors} from '../../utils/responseUtils'
import {
//   collectionClearResults,
  granuleMetadataReceived,
//   collectionUpdateDetailGranulesTotal,
  granuleUpdateTotal,
} from './GranuleResultActions'
// import {fetchConfig} from '../ConfigActions'
// import {fetchCounts, fetchInfo} from './InfoActions'
import {
  getCollection
} from './CollectionSearchActions'
import {
  collectionClearDetailGranulesResult,
  // collectionClearResults,
  // collectionMetadataReceived,
  // collectionUpdateDetailGranulesTotal,
  // collectionUpdateTotal,
} from './CollectionResultActions'

export const triggerGranuleSearch = (clearPreviousResults = false, retrieveFacets = true) => {
  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, retrieveFacets)
    const inFlight =
      state.search.granuleRequest.granuleSearchRequestInFlight
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.collectionFilter.selectedIds
    if (inFlight || !selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const prefetchHandler = dispatch => {
    dispatch(granuleSearchStart()) // TODO add params?
  }
  const successHandler = (dispatch, payload) => {

    dispatch(granuleSearchComplete(clearPreviousResults, payload.meta.total, payload.data, retrieveFacets? payload.meta: null))
  }
  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO show errors
    dispatch(granuleSearchError(e.errors || e))
  }

  return buildSearchAction(
    'granule',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

export const loadGranulesList = (path, newQueryString) => {

  return (dispatch, getState) => {

    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.granuleFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      const detailId = getCollectionIdFromGranuleListPath(path)
      dispatch(getCollection(detailId))
      dispatch(collectionClearSelectedIds())
      dispatch(collectionToggleSelectedId(detailId))
      dispatch(collectionClearDetailGranulesResult())
      dispatch(granuleUpdateFilters(searchFromQuery))
      dispatch(triggerGranuleSearch())
    }
  }
}

export const showGranules = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    // dispatch(collectionClearSelectedIds()) TODO leaving this alone since we specifically do *not* want to reset the selected collection ID for granule searching, I think
    const query = encodeQueryString(getState(), 'granuleFilter') //TODO put the id in there too?
      const locationDescriptor = {
        pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
        search: !_.isEmpty(query) ? `?${query}` : '',
      }
      history.push(locationDescriptor)
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
