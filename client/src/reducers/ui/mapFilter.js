import Immutable from 'seamless-immutable'
import { TOGGLE_MAP, UPDATE_BOUNDS } from '../../actions/FlowActions'

export const initialState = Immutable({
  showMap: false,
  bounds: undefined,
  boundsSource: undefined
})

export const mapFilter = (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE_MAP:
      const previousShowMap = state.showMap
      return Immutable.set(state, 'showMap', !previousShowMap)
    case UPDATE_BOUNDS:
       const boundsSourceUpdateState = Immutable.set(state, 'boundsSource', action.source)
       return Immutable.set(boundsSourceUpdateState, 'bounds', action.to)
    default:
      return state
  }
}

export default mapFilter
