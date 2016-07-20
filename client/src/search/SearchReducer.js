import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from './SearchActions'
import { UPDATE_GEOMETRY } from './map/MapActions'
import { DateRange } from './temporal/TemporalActions'

export const initialState = Immutable.Map({
  text: '',
  index: '',
  geoJSON: '',
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

    case UPDATE_GEOMETRY:
      return state.merge({
        geometry: action.geoJSON
      })

    case DateRange.START_DATE:
      return state.merge({
       startDateTime: action.datetime
      })

    case DateRange.END_DATE:
      return state.merge({
       endDateTime: action.datetime
      })

    default:
      return state
  }
}

export default search
