import _ from 'lodash'
import {fetchGranuleSearch, fetchCollectionDetail} from './AsyncHelpers'

import {encodeLocationDescriptor} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  collectionDetailRequested,
  collectionDetailReceived,
  collectionDetailError,
  granuleMatchingCountRequested,
  granuleMatchingCountReceived,
  granuleMatchingCountError,
} from './CollectionDetailStateActions'
import {granuleBodyBuilder} from './GranuleSearchRouteActions'

// shared internal state to track controller, since only one request within this context is allowed to be inFlight
let controller
let background_controller

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionDetailFilter) || {}
}

const isRequestInvalid = (id, state) => {
  const {collectionDetailRequest} = state.search
  const inFlight =
    collectionDetailRequest.inFlight ||
    collectionDetailRequest.backgroundInFlight
  if (inFlight && (controller || background_controller)) {
    if (_.isEmpty(id)) {
      return true
    }
    if (controller) {
      controller.abort()
      controller = null
    }
    if (background_controller) {
      background_controller.abort()
      background_controller = null
    }
    return false
  }
  return inFlight || _.isEmpty(id)
}

const detailPromise = (dispatch, id, filterState) => {
  // generate the request body with no facets, and a page size of zero, since we only need the count
  const body = granuleBodyBuilder(filterState, false, 0)
  if (!body) {
    // not covered by tests, since this should never actually occur due to the collectionId being provided, but included to prevent accidentally sending off really unreasonable requests
    dispatch(granuleMatchingCountError('Invalid Request'))
    return
  }

  // promise for main request: GET by ID
  const detailPromiseMain = detailPromiseIsolated(dispatch, id)
  // promise for secondary request: how many granules are in this collection when the search filters are applied?
  const [ granuleCountPromise, granleCountController ] = fetchGranuleSearch(
    body,
    payload => {
      if (background_controller && background_controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(granuleMatchingCountReceived(payload.meta.total))
    },
    e => {
      if (background_controller && background_controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(granuleMatchingCountError(e.errors || e))
    }
  )
  background_controller = granleCountController

  // TODO test that these requests are fired in parallel
  return Promise.all([ detailPromiseMain, granuleCountPromise ])
}

const detailPromiseIsolated = (dispatch, collectionId) => {
  // promise for main request: GET by ID
  const [ detailPromiseMain, detailPromiseController ] = fetchCollectionDetail(
    collectionId,
    payload => {
      if (controller && controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(
        collectionDetailReceived(payload.data[0], payload.meta.totalGranules)
      )
    },
    e => {
      if (controller && controller.signal.aborted) {
        // do not process error handling for aborted requests, just in case it gets to this point
        return
      }
      dispatch(collectionDetailError(e.errors || e))
    }
  )
  controller = detailPromiseController
  return detailPromiseMain
}

export const submitCollectionDetail = (history, id, filterState) => {
  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(id, getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(collectionDetailRequested(id, filterState))
    const updatedFilterState = getFilterFromState(getState())
    if (
      updatedFilterState.geoJSON ||
      updatedFilterState.endDateTime ||
      _.size(updatedFilterState.selectedFacets) >= 1 ||
      updatedFilterState.startDateTime
    ) {
      dispatch(granuleMatchingCountRequested())
    }
    // update URL if needed (required to display loading indicator on the correct page)
    navigateToDetailUrl(history, updatedFilterState)
    // start async request
    return detailPromise(dispatch, id, updatedFilterState)
  }
}

export const submitCollectionDetailIsolated = collectionId => {
  // use middleware to dispatch an async function
  return async (dispatch, getState) => {
    if (isRequestInvalid(collectionId, getState())) {
      // short circuit silently if minimum request requirements are not met
      return
    }
    // send notifications that request has begun, updating filter state if needed
    dispatch(collectionDetailRequested(collectionId, {}))

    // start async request
    return detailPromiseIsolated(dispatch, collectionId)
  }
}

const navigateToDetailUrl = (history, filterState) => {
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.details,
    filterState
  )

  if (isPathNew(history.location, locationDescriptor)) {
    history.push(locationDescriptor)
  }
}
