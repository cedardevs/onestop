import Immutable from 'seamless-immutable'
import {
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_MORE_RESULTS_REQUESTED,
  COLLECTION_NEW_SEARCH_RESULTS_RECIEVED,
  COLLECTION_MORE_RESULTS_RECIEVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/search/CollectionRequestActions'

export const initialState = Immutable({
  collectionSearchRequestInFlight: false,
})

export const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_NEW_SEARCH_REQUESTED:
    case COLLECTION_MORE_RESULTS_REQUESTED:
      return Immutable.set(state, 'collectionSearchRequestInFlight', true)

    case COLLECTION_NEW_SEARCH_RESULTS_RECIEVED:
    case COLLECTION_MORE_RESULTS_RECIEVED:
    case COLLECTION_SEARCH_ERROR:
      return Immutable.set(state, 'collectionSearchRequestInFlight', false)

    default:
      return state
  }
}

export default collectionRequest
