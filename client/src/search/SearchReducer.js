import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from './SearchActions'

export const initialState = Immutable.Map({
  text: '',
  index: '',
  inFlight: false
})

export const search = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH:
      return state.merge({
        text: action.searchText,
        inFlight: true
      })

    case SEARCH_COMPLETE:
      return state.merge({
        text: action.searchText,
        inFlight: false
      })

    default:
      return state
  }
}

export default search
