import _ from 'lodash'
import {fetchCollectionSearch} from './AsyncHelpers'

import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  collectionNewSearchRequested,
  collectionNewSearchResetFiltersRequested,
  collectionResultsPageRequested,
  collectionNewSearchResultsReceived,
  collectionResultsPageReceived,
  collectionSearchError,
} from './CollectionSearchStateActions'

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionFilter) || {}
}

const isRequestInvalid = state => {
  const inFlight = state.search.collectionRequest.inFlight
  return inFlight
}

const collectionBodyBuilder = (filterState, requestFacets) => {
  const body = assembleSearchRequest(filterState, requestFacets)
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
    dispatch(collectionResultsPageReceived(payload.data))
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
  return fetchCollectionSearch(body, successHandler(dispatch), e => {
    dispatch(collectionSearchError(e.errors || e))
  })
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
      false,
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
    history.push(locationDescriptor)
  }
}
