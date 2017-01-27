import Immutable from 'seamless-immutable'
import { SEARCH, SEARCH_COMPLETE } from '../../actions/SearchRequestActions'
import { FETCHING_GRANULES, FETCHED_GRANULES } from '../../actions/SearchRequestActions'

export const initialState = Immutable({
  collectionInFlight: false,
  granuleInFlight: false
})

export const request = (state = initialState, action) => {
  switch(action.type) {
    case SEARCH:
      return Immutable.set(state, 'collectionInFlight', true)

    case SEARCH_COMPLETE:
      return Immutable.set(state, 'collectionInFlight', false)

    case FETCHING_GRANULES:
      return Immutable.set(state, 'granuleInFlight', true)

    case FETCHED_GRANULES:
      return Immutable.set(state, 'granuleInFlight', false)

    default:
      return state
  }
}

export default request

