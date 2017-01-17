import Immutable from 'seamless-immutable'
import { NEW_GEOMETRY, REMOVE_GEOMETRY } from '../../search/map/MapActions'
import { CLEAR_SEARCH } from '../../search/SearchActions'

export const initialState = Immutable({
  geoJSON: {}
})

export const geometry = (state = initialState, action) => {
  switch (action.type) {
    case NEW_GEOMETRY:
      return Immutable.set(state, 'geoJSON', action.geoJSON)

    case CLEAR_SEARCH:
    case REMOVE_GEOMETRY:
      return initialState

    default:
      return state
  }
}

export default geometry
