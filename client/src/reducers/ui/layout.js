import Immutable from 'seamless-immutable'
import {
  SET_LEFT_OPEN_CLOSE,
  SET_SHOW_LEFT,
  TOGGLE_RIGHT,
  TOGGLE_MAP,
} from '../../actions/LayoutActions'

export const initialState = Immutable({
  showLeft: true,
  leftOpen: true,
  showRight: false,
  showMap: false,
})

export const layout = (state = initialState, action) => {
  switch (action.type) {
    case SET_LEFT_OPEN_CLOSE:
      return Immutable.set(state, 'leftOpen', action.value)
    case SET_SHOW_LEFT:
      return Immutable.set(state, 'showLeft', action.value)
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
