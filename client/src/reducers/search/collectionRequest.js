import Immutable from 'seamless-immutable'
import {
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_RESULTS_PAGE_REQUESTED,
  COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  COLLECTION_NEW_SEARCH_RESULTS_RECEIVED,
  COLLECTION_RESULTS_PAGE_RECEIVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/routing/CollectionSearchStateActions'

export const initialState = Immutable({
  inFlight: false,
  errorMessage: '',
})

export const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_NEW_SEARCH_REQUESTED:
    case COLLECTION_NEW_SEARCH_RESET_FILTERS_REQUESTED:
    case COLLECTION_RESULTS_PAGE_REQUESTED:
      return Immutable.merge(state, {
        inFlight: true,
        errorMessage: '',
      })

    case COLLECTION_NEW_SEARCH_RESULTS_RECEIVED:
    case COLLECTION_RESULTS_PAGE_RECEIVED:
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
