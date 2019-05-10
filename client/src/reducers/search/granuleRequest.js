import Immutable from 'seamless-immutable'
import {
  GRANULE_NEW_SEARCH_START,
  GRANULE_PAGE_SEARCH_START,
  GRANULE_SEARCH_COMPLETE,
  GRANULE_SEARCH_ERROR,
} from '../../actions/search/GranuleRequestActions'

export const initialState = Immutable({
  granuleSearchRequestInFlight: false,
  // TODO maybe put error message here?
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_NEW_SEARCH_START:
    case GRANULE_PAGE_SEARCH_START:
      return Immutable.set(state, 'granuleSearchRequestInFlight', true)

    case GRANULE_SEARCH_COMPLETE:
    case GRANULE_SEARCH_ERROR:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    default:
      return state
  }
}

export default granuleRequest
