import Immutable from 'immutable';
import { DateRange } from './TemporalActions'
import { CLEAR_SEARCH } from '../SearchActions'

export const initialState = Immutable.fromJS({
  startDateTime: '',
  endDateTime: ''
})

const temporal = (state = initialState, action) => {
  switch(action.type) {
    case DateRange.START_DATE:
      return state.mergeDeep({startDateTime: action.datetime})

    case DateRange.END_DATE:
      return state.mergeDeep({endDateTime: action.datetime})

    case CLEAR_SEARCH:
      return initialState

    default:
      return state
  }
}

export default temporal
