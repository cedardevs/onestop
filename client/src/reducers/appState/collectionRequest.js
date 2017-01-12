import Immutable from 'seamless-immutable'
import { SEARCH, SEARCH_COMPLETE } from '../../search/SearchActions'

const initialState = Immutable({ inFlight: false })

const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH:
      return Immutable.set(state, 'inFlight', true)

    case SEARCH_COMPLETE:
      return Immutable.set(state, 'inFlight', false)

    default:
      return state
  }
}

export default collectionRequest
