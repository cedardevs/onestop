import _ from 'lodash'
import {buildSearchAction} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {
  assembleCollectionSearchRequest,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  collectionNewSearchRequested,
  collectionMoreResultsRequested,
  collectionNewSearchResultsRecieved,
  collectionMoreResultsRecieved,
  collectionSearchError,
} from './CollectionSearchStateActions'

const validRequestCheck = state => {
  const inFlight = state.search.collectionRequest.inFlight
  return !inFlight
}

const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  console.log(e.errors || e)
  dispatch(collectionSearchError(e.errors || e))
}

export const submitCollectionSearch = history => {
  // TODO rename to indicate that it updates the URL as well? - this is *not* just a background request - make a new action if we need that!!
  const prefetchHandler = dispatch => {
    dispatch(collectionNewSearchRequested())
    dispatch(updateURLAndNavigateToCollectionRoute(history))
  }

  const bodyBuilder = state => {
    const body = assembleCollectionSearchRequest(state, true)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (!(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    dispatch(
      collectionNewSearchResultsRecieved(
        payload.meta.total,
        payload.data,
        payload.meta.facets
      )
    )
  }

  return buildSearchAction(
    'collection',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

export const submitCollectionSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the collection view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway

  const prefetchHandler = dispatch => {
    dispatch(collectionMoreResultsRequested())
  }
  const bodyBuilder = state => {
    const body = assembleCollectionSearchRequest(state, true)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (!(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    dispatch(collectionMoreResultsRecieved(payload.data))
  }

  return buildSearchAction(
    'collection',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

const updateURLAndNavigateToCollectionRoute = history => {
  // was showCollections
  return (dispatch, getState) => {
    const state = getState()
    const query = encodeQueryString(
      (state && state.search && state.search.collectionFilter) || {}
    )
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: '/collections',
        search: `?${query}`,
      }
      history.push(locationDescriptor)
    }
  }
}
