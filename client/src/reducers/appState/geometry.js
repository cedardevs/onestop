import Immutable from 'seamless-immutable'
import { NEW_GEOMETRY } from '../../search/map/MapActions'

export const initialState = Immutable({
  geoJSON: {}
})

export const geometry = (state = initialState, action) => {
  switch (action.type) {
    case NEW_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    default:
      return state
  }
}

export default geometry
