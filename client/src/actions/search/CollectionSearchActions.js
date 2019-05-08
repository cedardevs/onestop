import _ from 'lodash'
import {buildSearchAction} from './SearchActions'
import {showErrors} from '../ErrorActions'

import {assembleSearchRequest, encodeQueryString} from '../../utils/queryUtils'
import {
  collectionSearchStart,
  collectionSearchComplete,
  collectionSearchError,
} from './CollectionRequestActions'
// import { // TODO none of these are used - can the actions be removed entirely or are they in use elsewhere?
//   collectionClearFacets,
//   collectionClearSelectedIds,
//   collectionToggleSelectedId,
//   collectionUpdateFilters,
// } from './CollectionFilterActions'
// import {
//   collectionMetadataReceived,
//   collectionUpdateTotal,
// } from './CollectionResultActions'

export const triggerCollectionSearch = (
  clearPreviousResults = false,
  retrieveFacets = true
) => {
  // TODO rename to collection something something
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
    dispatch(collectionSearchStart())
  }
  const successHandler = (dispatch, payload) => {
    const result = _.reduce(
      // TODO is this the right place to do the reduce? or in the ... reducer...
      payload.data,
      (map, resource) => {
        return map.set(
          resource.id,
          _.assign({type: resource.type}, resource.attributes)
        )
      },
      new Map()
    )

    dispatch(
      collectionSearchComplete(
        clearPreviousResults,
        payload.meta.total,
        result,
        retrieveFacets ? payload.meta : null
      )
    )
  }
  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO show errors
    dispatch(collectionSearchError(e.errors || e))
  }

  return buildSearchAction(
    'collection',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
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
