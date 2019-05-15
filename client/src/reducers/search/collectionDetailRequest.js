import Immutable from 'seamless-immutable'
import {
  COLLECTION_DETAIL_REQUESTED,
  COLLECTION_DETAIL_RECIEVED,
  COLLECTION_DETAIL_ERROR,
} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  inFlight: false,
  requestedID: null, // TODO change to empty string if this causes any issues in the loading message container thing
  errorMessage: '',
})

export const collectionDetailRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_REQUESTED:
      return Immutable.merge(state, {
        inFlight: true,
        requestedID: action.id,
        errorMessage: '',
      })

    case COLLECTION_DETAIL_RECIEVED:
      return Immutable.merge(state, {
        inFlight: false,
        requestedID: null,
      })

    case COLLECTION_DETAIL_ERROR:
      return Immutable.merge(state, {
        inFlight: false,
        errorMessage: action.errors, // TODO change components to use this, when appropriate
      })

    default:
      return state
  }
}

export default collectionDetailRequest
