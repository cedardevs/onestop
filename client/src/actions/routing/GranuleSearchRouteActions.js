import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {fetchGranuleSearch} from './AsyncHelpers'
import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {isPathNew, ROUTE, goTo} from '../../utils/urlUtils'
import {
  granuleResultsPageReceived,
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleNewSearchResultsReceived,
  granuleSearchError,
  granulesForCartClearError,
  granulesForCartError,
  granulesForCartRequested,
  granulesForCartResultsReceived,
  granuleResultsPageRequested,
} from './GranuleSearchStateActions'
import {
  warningExceedsMaxAddition,
  warningNothingNew,
  warningOverflow,
  warningOverflowFromEmpty,
} from '../../utils/cartUtils'

/*
  Since granule results always use the same section of the redux store (because this is all tied to the same Route), a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other, among other things.
*/
let controller // shared internal state to track controller, since only one request within this context is allowed to be inFlight

const getTotalGranuleCountFromState = state => {
  // TODO we need a selectors section of the code
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
  const inFlight = isAlreadyInFlight(state)
  if (inFlight && controller) {
    if (_.isEmpty(id)) {
      return true
    }
    controller.abort()
    controller = null
    return false
  }
  return inFlight || _.isEmpty(id)
}

const isCartRequestAlreadyInFlight = state => {
  // ditto TODO selectors code section needed
  const inFlight = state.search.granuleRequest.cartGranulesInFlight
  return inFlight
}

const isCartRequestInvalid = state => {
  // ditto TODO selectors code section needed
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
    if (controller && controller.signal.aborted) {
      // do not process error handling for aborted requests, just in case it gets to this point
      return
    }
    const granules = payload.data
    const facets = payload.meta.facets
    const total = payload.meta.total
    dispatch(granuleNewSearchResultsReceived(granules, facets, total))
  }
}

const granulesForCartSuccessHandler = (dispatch, getState, cartCapacity) => {
  const stateSnapshot = getState()
  const cartGranules = stateSnapshot.cart.selectedGranules

  return payload => {
    if (controller && controller.signal.aborted) {
      // do not process error handling for aborted requests, just in case it gets to this point
      return
    }
    const granules = payload.data
    const total = payload.meta.total

    // Prevent cart addition from overflowing the cart capacity, taking into account
    // duplicates that would artificially inflate the estimate. This check must be done after the actual
    // request so that we can compare duplicate IDs for every possible addition (not just what's been paged)
    // The concern is placing excessive information into local storage or the Redux store.
    const cartGranuleIds = Object.keys(cartGranules)
    const cartGranuleCount = cartGranuleIds.length
    const duplicateIds = cartGranuleIds.filter(cartGranuleId => {
      return (
        granules.find(granule => {
          return granule.id === cartGranuleId
        }) !== undefined
      )
    })
    const numUniqueAdditions = granules.length - duplicateIds.length

    // everything in this filter is already in the cart
    if (numUniqueAdditions === 0) {
      dispatch(granulesForCartError(warningNothingNew()))
      return
    }

    // this would overflow the cart
    if (cartGranuleCount + numUniqueAdditions > cartCapacity) {
      // the cart is empty (changes the wording a bit for user-friendliness)
      if (cartGranuleCount === 0) {
        dispatch(
          granulesForCartError(
            warningOverflowFromEmpty(numUniqueAdditions, cartCapacity)
          )
        )
        return
      }
      else {
        dispatch(
          granulesForCartError(
            warningOverflow(numUniqueAdditions, cartCapacity)
          )
        )
        return
      }
    }

    // and then add to redux state
    dispatch(granulesForCartResultsReceived(granules, total))
  }
}

const pageSuccessHandler = dispatch => {
  return payload => {
    if (controller && controller.signal.aborted) {
      // do not process error handling for aborted requests, just in case it gets to this point
      return
    }
    const granules = payload.data
    dispatch(
      granuleResultsPageReceived(
        granules,
        payload.meta.total,
        payload.meta.facets
      )
    )
  }
}

const granulePromise = (
  dispatch,
  filterState,
  requestFacets,
  successHandler
) => {
  // generate the request body based on filters, and if we need facets or not
  const body = granuleBodyBuilder(
    filterState,
    requestFacets,
    filterState.pageSize
  )
  if (!body) {
    // not covered by tests, since this should never actually occur due to the collectionId being provided, but included to prevent accidentally sending off really unreasonable requests
    dispatch(granuleSearchError('Invalid Request'))
    return
  }
  // return promise for search
  const [ promise, abort_controller ] = fetchGranuleSearch(
    body,
    successHandler(dispatch),
    e => {
      if (controller && controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(granuleSearchError(e.errors || e))
    }
  )

  controller = abort_controller

  return promise
}

const granulesForCartPromise = (
  dispatch,
  getState,
  granuleFilter,
  maxCartAddition,
  cartCapacity,
  requestFacets,
  successHandler
) => {
  const stateSnapshot = getState()
  const totalGranuleCount = getTotalGranuleCountFromState(stateSnapshot)

  // NOTES:
  // - We re-use the existing granule endpoint because real-time scroll requests are not recommended by ES.
  // `filterState` is a seamless-immutable object, so we have to use that API to copy/set into a new instance.

  // prevent cart addition requests from adding more than 1000 items at a time
  if (totalGranuleCount > maxCartAddition) {
    dispatch(
      granulesForCartError(
        warningExceedsMaxAddition(totalGranuleCount, maxCartAddition)
      )
    )
    return
  }

  // Ensure the filter page offset is 0, since we are grabbing as much as we can
  // from the beginning -- up to `maxCartAddition` in a single request.
  const filterStateModifiedForCartRequest = Immutable.set(
    granuleFilter,
    'pageOffset',
    0
  )

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
  const [ promise, abort_controller ] = fetchGranuleSearch(
    body,
    successHandler(dispatch, getState, cartCapacity),
    e => {
      if (controller && controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(granulesForCartError(e.errors || e))
    }
  )
  controller = abort_controller
  return promise
}

export const submitGranuleSearchForCart = (
  history,
  granuleFilter,
  maxCartAddition,
  cartCapacity
) => {
  return async (dispatch, getState) => {
    if (isCartRequestInvalid(getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(granulesForCartRequested())

    // start async request
    return granulesForCartPromise(
      dispatch,
      getState,
      granuleFilter,
      maxCartAddition,
      cartCapacity,
      false, // granule requests for cart don't need facet information (yet?)
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
    // in case we had previously received a warning for submitting filtered granules to the cart, clear the warning
    dispatch(granulesForCartClearError())

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
    // in case we had previously received a warning for submitting filtered granules to the cart, clear the warning
    dispatch(granulesForCartClearError())

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

export const submitGranuleSearchWithPage = (offset, max) => {
  return async (dispatch, getState) => {
    let state = getState()
    if (
      isRequestInvalid(
        state.search.granuleFilter.selectedCollectionIds[0],
        state
      )
    ) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun
    dispatch(granuleResultsPageRequested(offset, max))
    const updatedFilterState = getFilterFromState(getState())
    // start async request
    return granulePromise(
      dispatch,
      updatedFilterState,
      offset == 0,
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
    goTo(history, locationDescriptor)
  }
}

const navigateToCart = history => {
  const locationDescriptor = encodeLocationDescriptor(ROUTE.cart, {})
  if (isPathNew(history.location, locationDescriptor)) {
    goTo(history, locationDescriptor)
  }
}
