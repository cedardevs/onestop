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

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionDetailFilter) || {}
}

const isRequestInvalid = (id, state) => {
  const {collectionDetailRequest} = state.search
  const inFlight =
    collectionDetailRequest.inFlight ||
    collectionDetailRequest.backgroundInFlight
  return inFlight || _.isEmpty(id)
}

const helper = (dispatch, id, filterState) => {
  // TODO rename this
  const body = granuleBodyBuilder(filterState, false, 0)
  if (!body) {
    // not covered by tests, since this should never actually occur due to the collectionId being provided, but included to prevent accidentally sending off really unreasonable requests
    dispatch(granuleMatchingCountError('Invalid Request'))
    return
  }

  const detailPromise = fetchCollectionDetail(
    id,
    payload => {
      dispatch(
        collectionDetailReceived(payload.data[0], payload.meta.totalGranules)
      )
    },
    e => {
      dispatch(collectionDetailError(e.errors || e))
    }
  )
  const granuleCountPromise = fetchGranuleSearch(
    body,
    payload => {
      dispatch(granuleMatchingCountReceived(payload.meta.total))
    },
    e => {
      dispatch(granuleMatchingCountError(e.errors || e))
    }
  )

  // TODO test that these requests are fired in parallel
  return Promise.all([ detailPromise, granuleCountPromise ])
}

export const submitCollectionDetailAndUpdateUrl = (
  history,
  id,
  filterState
) => {
  return async (dispatch, getState) => {
    if (isRequestInvalid(id, getState())) {
      return
    }

    dispatch(collectionDetailRequested(id, filterState))
    dispatch(granuleMatchingCountRequested())
    const updatedFilterState = getFilterFromState(getState())
    navigateToDetailUrl(history, updatedFilterState)
    return helper(dispatch, id, updatedFilterState)
  }
}

export const submitCollectionDetail = (id, filterState) => {
  return async (dispatch, getState) => {
    if (isRequestInvalid(id, getState())) {
      return
    }

    dispatch(collectionDetailRequested(id, filterState))
    dispatch(granuleMatchingCountRequested())
    const updatedFilterState = getFilterFromState(getState())
    return helper(dispatch, id, updatedFilterState)
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
