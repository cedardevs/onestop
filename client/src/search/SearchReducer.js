import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from './SearchActions'
import { UPDATE_GEOMETRY } from './map/MapActions'

export const initialState = Immutable.Map({
  text: '',
  index: '',
  geometry: '',
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

    case UPDATE_GEOMETRY:
      return state.merge({
        geometry: action.searchGeometry
      })

    default:
      return state
  }
}

export default search
