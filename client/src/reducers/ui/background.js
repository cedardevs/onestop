import Immutable from 'seamless-immutable'
import { TOGGLE_BACKGROUND_IMAGE } from '../../actions/FlowActions'

export const initialState = Immutable({
  showImage: true
})

export const background = (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE_BACKGROUND_IMAGE:
      const newState = Immutable({showImage: action.visible})
      return newState

    default:
      return state
  }
}

export default background
