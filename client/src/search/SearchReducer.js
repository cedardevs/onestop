import Immutable from 'immutable'
import {INDEX_CHANGE, SEARCH, SEARCH_COMPLETE} from './SearchActions'
import { START_DATE, END_DATE } from './temporal/TemporalActions'

export const initialState = Immutable.Map({
  text: '',
  index: '',
  inFlight: false,
  startDateTime: '',
  endDateTime: ''
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

    case START_DATE:
      return state.merge({
       startDateTime: action.datetime
      })

    case END_DATE:
      return state.merge({
       endDateTime: action.datetime
      })

    default:
      return state
  }
}

export default search
