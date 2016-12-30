import Immutable from 'seamless-immutable';
import { DateRange } from './TemporalActions'
import { CLEAR_SEARCH } from '../SearchActions'

export const initialState = Immutable({
  startDateTime: '',
  endDateTime: ''
})

const temporal = (state = initialState, action) => {
  switch(action.type) {
    case DateRange.START_DATE:
      return Immutable.merge(state, {startDateTime: action.datetime})

    case DateRange.END_DATE:
      return Immutable.merge(state, {endDateTime: action.datetime})

    case CLEAR_SEARCH:
      return initialState

    default:
      return state
  }
}

export default temporal
