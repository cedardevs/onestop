import Immutable from 'seamless-immutable'
import {
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED,
  GRANULE_MORE_RESULTS_REQUESTED,
  GRANULE_NEW_SEARCH_RESULTS_RECEIVED,
  GRANULE_MORE_RESULTS_RECEIVED,
  GRANULE_SEARCH_ERROR,
  GRANULES_FOR_CART_REQUESTED,
  GRANULES_FOR_CART_RESULTS_RECEIVED,
  GRANULES_FOR_CART_ERROR,
} from '../../actions/routing/GranuleSearchStateActions'

export const initialState = Immutable({
  inFlight: false,
  cartGranulesInFlight: false,
  errorMessage: '',
  cartGranulesErrorMessage: '',
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_REQUESTED:
    case GRANULE_NEW_SEARCH_RESET_FILTERS_REQUESTED:
    case GRANULE_MORE_RESULTS_REQUESTED:
      return Immutable.merge(state, {
        inFlight: true,
        errorMessage: '',
      })

    case GRANULE_NEW_SEARCH_RESULTS_RECEIVED:
    case GRANULE_MORE_RESULTS_RECEIVED:
      return Immutable.set(state, 'inFlight', false)

    case GRANULE_SEARCH_ERROR:
      return Immutable.merge(state, {
        inFlight: false,
        errorMessage: action.errors,
      })

    case GRANULES_FOR_CART_REQUESTED:
      return Immutable.merge(state, {
        cartGranulesInFlight: true,
        errorMessage: '',
      })

    case GRANULES_FOR_CART_RESULTS_RECEIVED:
      return Immutable.set(state, 'cartGranulesInFlight', false)

    case GRANULES_FOR_CART_ERROR:
      return Immutable.merge(state, {
        cartGranulesInFlight: false,
        cartGranulesErrorMessage: action.errors,
      })

    default:
      return state
  }
}

export default granuleRequest
