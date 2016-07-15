import Immutable from 'immutable'
import {INDEX_CHANGE, SEARCH, SEARCH_COMPLETE} from './SearchActions'
import { TEMPORAL_SEARCH } from './temporal/TemporalActions'

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

    case TEMPORAL_SEARCH:
      return state.merge({
       beforedatetime: action.before,
       afterdatetime: action.after
      })

    default:
      return state
  }
}

export default search
