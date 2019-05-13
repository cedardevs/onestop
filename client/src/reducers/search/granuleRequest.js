import Immutable from 'seamless-immutable'
import {
  GRANULE_NEW_SEARCH_REQUESTED,
  GRANULE_MORE_RESULTS_REQUESTED,
  GRANULE_NEW_SEARCH_RESULTS_RECIEVED,
  GRANULE_MORE_RESULTS_RECIEVED,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'

export const initialState = Immutable({
  granuleSearchRequestInFlight: false,
  errorMessage: '',
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_REQUESTED:
    case GRANULE_MORE_RESULTS_REQUESTED:
      return Immutable.merge(state, {
        granuleSearchRequestInFlight: true,
        errorMessage: '',
      })

    case GRANULE_NEW_SEARCH_RESULTS_RECIEVED:
    case GRANULE_MORE_RESULTS_RECIEVED:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    case GRANULE_SEARCH_ERROR:
      return Immutable.merge(state, {
        granuleSearchRequestInFlight: false,
        errorMessage: action.errors,
      })

    default:
      return state
  }
}

export default granuleRequest
