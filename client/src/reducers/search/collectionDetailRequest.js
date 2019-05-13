import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_START,
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/get/CollectionDetailRequestActions'

export const initialState = Immutable({
  collectionDetailRequestInFlight: false,
  requestedID: null,
})

export const collectionDetailRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_GET_DETAIL_START:
      return Immutable.merge(state, {collectionDetailRequestInFlight: true, requestedID: action.id})

    case COLLECTION_GET_DETAIL_COMPLETE:
    case COLLECTION_GET_DETAIL_ERROR:
      return Immutable.merge(state, {collectionDetailRequestInFlight: false, requestedID: null})

    default:
      return state
  }
}

export default collectionDetailRequest
