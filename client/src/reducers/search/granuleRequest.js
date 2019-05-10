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
  // TODO maybe put error message here?
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_REQUESTED:
    case GRANULE_MORE_RESULTS_REQUESTED:
      return Immutable.set(state, 'granuleSearchRequestInFlight', true)

    case GRANULE_NEW_SEARCH_RESULTS_RECIEVED:
    case GRANULE_MORE_RESULTS_RECIEVED:
    case GRANULE_SEARCH_ERROR:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    default:
      return state
  }
}

export default granuleRequest
