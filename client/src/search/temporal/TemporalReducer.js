import Immutable from 'immutable';
import { DateRange } from './TemporalActions'

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

    default:
      return state
  }
}

export default temporal
