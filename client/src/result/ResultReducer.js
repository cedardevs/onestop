import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from '../search/SearchActions'

export const initialState = Immutable.OrderedMap()

const results = (state = initialState, action) => {
  switch(action.type) {
    case SEARCH:
      state = Immutable.OrderedMap()
      return state

    case SEARCH_COMPLETE:
      state = Immutable.OrderedMap()
      action.items.forEach((value, key) => {
        state = state.set(key, Immutable.Map(value))
      })
      return state

    default:
      return state
  }
}

export default results
