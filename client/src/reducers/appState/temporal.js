import Immutable from 'seamless-immutable'
import { DateRange } from '../../search/temporal/TemporalActions'

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

    default:
      return state
  }
}

export default temporal
