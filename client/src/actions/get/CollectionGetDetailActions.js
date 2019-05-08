import _ from 'lodash'
import {buildGetAction} from './SearchActions'
import {showErrors} from '../ErrorActions'

import {encodeQueryString} from '../../utils/queryUtils'
import {
  collectionGetDetailStart,
  collectionGetDetailComplete,
  collectionGetDetailError,
} from '../get/CollectionDetailRequestActions'

export const getCollection = collectionId => {
  // TODO used only by initSearchActions???
  const prefetchHandler = dispatch => {
    // dispatch(showLoading())
    dispatch(collectionGetDetailStart(collectionId)) // TODO
  }

  const successHandler = (dispatch, payload) => {
    dispatch(collectionGetDetailComplete(payload.data[0], payload.meta))
    dispatch(hideLoading())
  }

  const errorHandler = (dispatch, e) => {
    // dispatch(hideLoading()) // TODO
    // dispatch(showErrors(e.errors || e)) // TODO
    dispatch(collectionGetDetailError(e.errors || e)) // TODO
  }

  return buildGetAction(
    'collection',
    collectionId,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

export const showDetails = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    const locationDescriptor = {
      pathname: `/collections/details/${id}`,
      search: _.isEmpty(query) ? null : `?${query}`,
    }
    history.push(locationDescriptor)
  }
}
