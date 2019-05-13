import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_START,
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/get/CollectionDetailRequestActions'

export const initialState = Immutable({
  inFlight: false,
  requestedID: null, // TODO change to empty string if this causes any issues in the loading message container thing
  errorMessage: '',
})

export const collectionDetailRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_GET_DETAIL_START:
      return Immutable.merge(state, {
        inFlight: true,
        requestedID: action.id,
        errorMessage: '',
      })

    case COLLECTION_GET_DETAIL_COMPLETE:
      return Immutable.merge(state, {
        inFlight: false,
        requestedID: null,
      })

    case COLLECTION_GET_DETAIL_ERROR:
      return Immutable.merge(state, {
        inFlight: false,
        errorMessage: action.errors, // TODO change components to use this, when appropriate
      })

    default:
      return state
  }
}

export default collectionDetailRequest
