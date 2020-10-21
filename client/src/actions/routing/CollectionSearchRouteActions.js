import _ from 'lodash'
import {fetchCollectionSearch} from './AsyncHelpers'

import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew, goTo} from '../../utils/urlUtils'
import {
  collectionNewSearchRequested,
  collectionNewSearchResetFiltersRequested,
  collectionResultsPageRequested,
  collectionNewSearchResultsReceived,
  collectionResultsPageReceived,
  collectionSearchError,
} from './CollectionSearchStateActions'

let controller // shared internal state to track controller, since only one request within this context is allowed to be inFlight

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionFilter) || {}
}

const isRequestInvalid = state => {
  const inFlight = state.search.collectionRequest.inFlight
  if (inFlight && controller) {
    controller.abort()
    controller = null
    return false
  }
  return inFlight
}

const collectionBodyBuilder = (filterState, requestFacets) => {
  const body = assembleSearchRequest(
    filterState,
    requestFacets,
    filterState.pageSize
  )
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

const newSearchSuccessHandler = dispatch => {
  return payload => {
    dispatch(
      collectionNewSearchResultsReceived(
        payload.meta.total,
        payload.data,
        payload.meta.facets
      )
    )
  }
}

const pageSuccessHandler = dispatch => {
  return payload => {
    dispatch(
      collectionResultsPageReceived(
        payload.meta.total,
        payload.data,
        payload.meta.facets
      )
    )
  }
}

const collectionPromise = (
  dispatch,
  filterState,
  requestFacets,
  successHandler
) => {
  // generate the request body based on filters, and if we need facets or not
  const body = collectionBodyBuilder(filterState, requestFacets)
  if (!body) {
    dispatch(collectionSearchError('Invalid Request'))
    return
  }
  // return promise for search
  const [
    promise,
    abort_controller,
  ] = fetchCollectionSearch(body, successHandler(dispatch), e => {
    if (controller && controller.signal.aborted) {
      // do not process error handling for aborted requests, just in case it gets to this point
      return
    }
    else {
      dispatch(collectionSearchError(e.errors || e))
    }
  })
  controller = abort_controller
  return promise
}

export const submitCollectionSearchWithQueryText = (history, queryText) => {
  return submitCollectionSearchWithFilter(history, {queryText: queryText})
}

export const submitCollectionSearchWithFilter = (history, filterState) => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled
  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(collectionNewSearchResetFiltersRequested(filterState))
    const updatedFilterState = getFilterFromState(getState())
    // update URL if needed (required to display loading indicator on the correct page)
    navigateToCollectionUrl(history, updatedFilterState)
    // start async request
    return collectionPromise(
      dispatch,
      updatedFilterState,
      true,
      newSearchSuccessHandler
    )
  }
}

export const submitCollectionSearch = history => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled
  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, using existing filter state
    dispatch(collectionNewSearchRequested())
    const updatedFilterState = getFilterFromState(getState())
    // update URL if needed (required to display loading indicator on the correct page)
    navigateToCollectionUrl(history, updatedFilterState)
    // start async request
    return collectionPromise(
      dispatch,
      updatedFilterState,
      true,
      newSearchSuccessHandler
    )
  }
}

export const submitCollectionSearchWithPage = (offset, max) => {
  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun
    dispatch(collectionResultsPageRequested(offset, max))
    const updatedFilterState = getFilterFromState(getState())
    // start async request
    return collectionPromise(
      dispatch,
      updatedFilterState,
      offset ==
        0 /* changes for https://github.com/cedardevs/onestop/issues/1335 are a workaround hotfix - the underlying problem is that PageController appears to be firing off this request when page 1 loads initially. Recommend revisiting that underlying problem and reverting these changes. */,
      pageSuccessHandler
    )
  }
}

const navigateToCollectionUrl = (history, filterState) => {
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.collections,
    filterState
  )
  if (
    !_.isEmpty(locationDescriptor.search) &&
    isPathNew(history.location, locationDescriptor)
  ) {
    goTo(history, locationDescriptor)
  }
}
