import Immutable from 'seamless-immutable'
import {
  TOGGLE_LEFT,
  TOGGLE_RIGHT,
  TOGGLE_MAP,
} from '../../actions/LayoutActions'

export const initialState = Immutable({
  showLeft: true,
  showRight: false,
  showMap: false,
})

export const layout = (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE_LEFT:
      const previousShowLeft = state.showLeft
      return Immutable.set(state, 'showLeft', !previousShowLeft)
    case TOGGLE_RIGHT:
      const previousShowRight = state.showLeft
      return Immutable.set(state, 'showRight', !previousShowRight)
    case TOGGLE_MAP:
      const previousShowMap = state.showMap
      return Immutable.set(state, 'showMap', !previousShowMap)
    default:
      return state
  }
}

export default layout
