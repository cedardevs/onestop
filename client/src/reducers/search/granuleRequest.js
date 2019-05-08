import Immutable from 'seamless-immutable'
import {
  GRANULE_SEARCH_START,
  GRANULE_SEARCH_COMPLETE,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'

export const initialState = Immutable({
  granuleSearchRequestInFlight: false,
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_SEARCH_START:
      return Immutable.set(state, 'granuleSearchRequestInFlight', true)

    case GRANULE_SEARCH_COMPLETE:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    case GRANULE_SEARCH_ERROR:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    default:
      return state
  }
}

export default granuleRequest
