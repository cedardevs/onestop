import _ from 'lodash'
import {buildGetAction, buildSearchAction} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {assembleSearchRequest, encodeQueryString} from '../../utils/queryUtils'
import {
  collectionDetailRequested,
  collectionDetailReceived,
  collectionDetailError,
  granuleMatchingCountRequested,
  granuleMatchingCountReceived,
  granuleMatchingCountError,
} from './CollectionDetailStateActions'
import {
  initialState,
  collectionDetailFilter, // TODO needing this here as a helper is probably bad? or at least says something really important about paradigms...
} from '../../reducers/search/collectionDetailFilter'

const granuleFilterState = state => {
  return (state && state.search && state.search.collectionDetailFilter) || {}
}

export const submitCollectionDetail = (history, id, filterState) => {
  const validRequestCheck = state => {
    const inFlight =
      state.search.collectionDetailRequest.inFlight ||
      state.search.collectionDetailRequest.backgroundInFlight
    return !inFlight
  }

  const prefetchHandler = dispatch => {
    dispatch(collectionDetailRequested(id, filterState))
    // TODO since ^^^ call modifies the state, we need to somehow grab the updated state before updating the URL, etc.
    // TODO this might be a problem in collection/granule search too - check!
    dispatch(
      updateURLAndNavigateToCollectionDetailRoute(
        history,
        id,
        collectionDetailFilter(
          initialState,
          collectionDetailRequested(id, filterState)
        )
      )
    )
    dispatch(submitBackgroundFilteredGranuleCount()) // gets the state from redux, since collectionDetailRequested updates selectedIds
  }

  const successHandler = (dispatch, payload) => {
    dispatch(
      collectionDetailReceived(payload.data[0], payload.meta.totalGranules)
    )
  }

  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO
    dispatch(collectionDetailError(e.errors || e)) // TODO
  }

  return buildGetAction(
    'collection',
    id,
    validRequestCheck,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

export const submitBackgroundFilteredGranuleCount = () => {
  const prefetchHandler = dispatch => {
    dispatch(granuleMatchingCountRequested())
  }

  const bodyBuilder = state => {
    // TODO make the 0 page size a param of the other granuleBodyBuilder and reuse, so they won't be accidentally different?
    const filterState = granuleFilterState(state)
    const body = assembleSearchRequest(filterState, false, 0)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (!(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    dispatch(granuleMatchingCountReceived(payload.meta.total))
  }

  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO
    dispatch(granuleMatchingCountError(e.errors || e)) // TODO
  }

  return buildSearchAction(
    'granule',
    () => true, // valid backgroundInFlight checked by submitCollectionDetail
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

const updateURLAndNavigateToCollectionDetailRoute = (
  history,
  id,
  filterState
) => {
  if (!id) {
    return
  }
  return dispatch => {
    const query = encodeQueryString(filterState)
    console.log('detail url', query, filterState)
    const locationDescriptor = {
      pathname: `/collections/details/${id}`,
      search: _.isEmpty(query) ? null : `?${query}`,
    }
    history.push(locationDescriptor)
  }
}
