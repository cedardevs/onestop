import _ from 'lodash'
import {fetchGranuleSearch} from './AsyncHelpers'
import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleMoreResultsRequested,
  granuleNewSearchResultsReceived,
  granuleMoreResultsReceived,
  granuleSearchError,
  granulesForCartRequested,
  granulesForCartError,
  granulesForCartResultsReceived,
} from './GranuleSearchStateActions'
import {insertGranules} from '../../utils/localStorageUtil'

/*
  Since granule results always use the same section of the redux store (because this is all tied to the same Route), a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other, among other things.
*/

const getTotalGranuleCountFromState = state => {
  return (
    (state &&
      state.search &&
      state.search.granuleResult &&
      state.search.granuleResult.totalGranuleCount) ||
    0
  )
}

const getFilterFromState = state => {
  return (state && state.search && state.search.granuleFilter) || {}
}

const isAlreadyInFlight = state => {
  const inFlight = state.search.granuleRequest.inFlight
  return inFlight
}

const isRequestInvalid = (id, state) => {
  return isAlreadyInFlight(state) || _.isEmpty(id)
}

const isCartRequestAlreadyInFlight = state => {
  const inFlight = state.search.granuleRequest.cartGranulesInFlight
  return inFlight
}

const isCartRequestInvalid = state => {
  return isCartRequestAlreadyInFlight(state)
}

export const granuleBodyBuilder = (filterState, requestFacets, pageSize) => {
  const body = assembleSearchRequest(filterState, requestFacets, pageSize)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

const newSearchSuccessHandler = dispatch => {
  return payload => {
    const granules = payload.data
    const facets = payload.meta.facets
    const total = payload.meta.total
    dispatch(granuleNewSearchResultsReceived(granules, facets, total))
  }
}

const granulesForCartSuccessHandler = dispatch => {
  return payload => {
    const granules = payload.data
    const total = payload.meta.total

    // add to local storage
    insertGranules(granules)

    // and then add to redux state
    dispatch(granulesForCartResultsReceived(granules, total))
  }
}

const pageSuccessHandler = dispatch => {
  return payload => {
    const granules = payload.data
    dispatch(granuleMoreResultsReceived(granules))
  }
}

const granulePromise = (
  dispatch,
  filterState,
  requestFacets,
  successHandler
) => {
  // generate the request body based on filters, and if we need facets or not
  const body = granuleBodyBuilder(filterState, requestFacets)
  if (!body) {
    // not covered by tests, since this should never actually occur due to the collectionId being provided, but included to prevent accidentally sending off really unreasonable requests
    dispatch(granuleSearchError('Invalid Request'))
    return
  }
  // return promise for search
  return fetchGranuleSearch(body, successHandler(dispatch), e => {
    dispatch(granuleSearchError(e.errors || e))
  })
}

const granulesForCartPromise = (
  dispatch,
  totalGranuleCount,
  filterState,
  requestFacets,
  successHandler
) => {
  console.log('granulesForCartPromise')
  console.log('totalGranuleCount', totalGranuleCount)

  // NOTES:
  // - We re-use the existing granule endpoint because real-time scroll requests are not recommended by ES.
  // `filterState` is a seamless-immutable object, so we have to use that API to copy/set into a new instance.

  // prevent cart addition requests from adding more than 1000 items at a time
  const maxCartAddition = 10
  if (totalGranuleCount > maxCartAddition) {
    dispatch(
      granulesForCartError(
        `${totalGranuleCount} granules exceeds the allowable ${maxCartAddition} that can be added to the cart at one time.`
      )
    )
    return
  }

  // Ensure the filter page offset is 0, since we are grabbing as much as we can
  // from the beginning -- up to `maxCartAddition` in a single request.
  const filterStateModifiedForCartRequest = filterState.set('pageOffset', 0)

  // generate the request body based on filters, and if we need facets or not
  const body = granuleBodyBuilder(
    filterStateModifiedForCartRequest,
    requestFacets,
    maxCartAddition
  )
  if (!body) {
    dispatch(granulesForCartError('Invalid Request'))
    return
  }
  // return promise for search
  return fetchGranuleSearch(body, successHandler(dispatch), e => {
    dispatch(granulesForCartError(e.errors || e))
  })
}

export const submitGranuleSearchForCart = (history, filterState) => {
  return async (dispatch, getState) => {
    if (isCartRequestInvalid(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(granulesForCartRequested(filterState))

    const stateSnapshot = getState()
    const totalGranuleCount = getTotalGranuleCountFromState(stateSnapshot)
    const updatedFilterState = getFilterFromState(stateSnapshot)

    // TODO: should we keep this?
    navigateToCart(history)

    // start async request
    return granulesForCartPromise(
      dispatch,
      totalGranuleCount,
      updatedFilterState,
      true,
      granulesForCartSuccessHandler
    )
  }
}

export const submitGranuleSearchWithFilter = (
  history,
  collectionId,
  filterState
) => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(collectionId, getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(granuleNewSearchResetFiltersRequested(collectionId, filterState))
    const updatedFilterState = getFilterFromState(getState())
    // update URL if needed (required to display loading indicator on the correct page)
    navigateToGranuleUrl(history, collectionId, updatedFilterState)
    // start async request
    return granulePromise(
      dispatch,
      updatedFilterState,
      true,
      newSearchSuccessHandler
    )
  }
}

export const submitGranuleSearch = (history, collectionId) => {
  // new granule search *for granules within a single collection*
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(collectionId, getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(granuleNewSearchRequested(collectionId))
    const updatedFilterState = getFilterFromState(getState())
    // update URL if needed (required to display loading indicator on the correct page)
    navigateToGranuleUrl(history, collectionId, updatedFilterState)
    // start async request
    return granulePromise(
      dispatch,
      updatedFilterState,
      true,
      newSearchSuccessHandler
    )
  }
}

export const submitGranuleSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the granule view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  // fetch the next page of granules granule search *for granules within a single collection*

  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isAlreadyInFlight(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun
    dispatch(granuleMoreResultsRequested())
    const updatedFilterState = getFilterFromState(getState())
    // start async request
    return granulePromise(
      dispatch,
      updatedFilterState,
      false,
      pageSuccessHandler
    )
  }
}

const navigateToGranuleUrl = (history, collectionId, filterState) => {
  if (!collectionId) {
    return
  }
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.granules,
    filterState,
    collectionId
  )
  if (isPathNew(history.location, locationDescriptor)) {
    history.push(locationDescriptor)
  }
}

const navigateToCart = history => {
  const locationDescriptor = encodeLocationDescriptor(ROUTE.cart, {})
  if (isPathNew(history.location, locationDescriptor)) {
    history.push(locationDescriptor)
  }
}
