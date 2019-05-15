import _ from 'lodash'
import {buildGetAction} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {encodeQueryString} from '../../utils/queryUtils'
import {
  collectionDetailRequested,
  collectionDetailRecieved,
  collectionDetailError,
} from './CollectionDetailStateActions'
import {granuleUpdateFilters} from './GranuleSearchStateActions'

export const submitCollectionDetail = (history, collectionId) => {
  const validRequestCheck = state => {
    const inFlight = state.search.collectionDetailRequest.inFlight
    return !inFlight
  }

  const prefetchHandler = dispatch => {
    dispatch(collectionDetailRequested(collectionId))
    dispatch(updateURLAndNavigateToCollectionDetailRoute(history, collectionId))
  }

  const successHandler = (dispatch, payload) => {
    dispatch(
      collectionDetailRecieved(payload.data[0], payload.meta.totalGranules)
    )
  }

  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO
    dispatch(collectionDetailError(e.errors || e)) // TODO
  }

  return buildGetAction(
    'collection',
    collectionId,
    validRequestCheck,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

const updateURLAndNavigateToCollectionDetailRoute = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const state = getState()
    const query = encodeQueryString(
      (state && state.search && state.search.collectionFilter) || {}
    )
    const locationDescriptor = {
      pathname: `/collections/details/${id}`,
      search: _.isEmpty(query) ? null : `?${query}`,
    }
    history.push(locationDescriptor)
  }
}
