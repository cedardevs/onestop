import Immutable from 'immutable'
import { NEW_GEOMETRY, REMOVE_GEOMETRY } from './MapActions'

export const initialState = Immutable.fromJS({
  geoJSON: null
})

export const map = (state = initialState, action) => {
  switch (action.type) {
    case NEW_GEOMETRY:
      return state.set('geoJSON', Immutable.fromJS(action.geoJSON))

    case REMOVE_GEOMETRY:
      return initialState

    default:
      return state
  }
}

export default map
