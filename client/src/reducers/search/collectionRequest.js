import Immutable from 'seamless-immutable'
import {
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_MORE_RESULTS_REQUESTED,
  COLLECTION_NEW_SEARCH_RESULTS_RECIEVED,
  COLLECTION_MORE_RESULTS_RECIEVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/search/CollectionRequestActions'

export const initialState = Immutable({
  inFlight: false,
  errorMessage: '',
})

export const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_NEW_SEARCH_REQUESTED:
    case COLLECTION_MORE_RESULTS_REQUESTED:
      return Immutable.merge(state, {
        inFlight: true,
        errorMessage: '',
      })

    case COLLECTION_NEW_SEARCH_RESULTS_RECIEVED:
    case COLLECTION_MORE_RESULTS_RECIEVED:
      return Immutable.set(state, 'inFlight', false)

    case COLLECTION_SEARCH_ERROR:
      return Immutable.merge(state, {
        inFlight: false,
        errorMessage: action.errors,
      })

    default:
      return state
  }
}

export default collectionRequest
