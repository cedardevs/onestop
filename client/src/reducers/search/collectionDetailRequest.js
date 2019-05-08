import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_START,
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/get/CollectionDetailRequestActions'

export const initialState = Immutable({
  collectionDetailRequestInFlight: false,
})

export const collectionDetailRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_GET_DETAIL_START:
      return Immutable.set(state, 'collectionDetailRequestInFlight', action.id)

    case COLLECTION_GET_DETAIL_COMPLETE:
      return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    case COLLECTION_GET_DETAIL_ERROR:
      return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    default:
      return state
  }
}

export default collectionDetailRequest
