import Immutable from 'seamless-immutable'
import { SET_FOCUS } from '../../actions/FlowActions'

export const initialState = Immutable({
  focusedId: null
})

export const cardDetails = (state = initialState, action) => {
  switch (action.type) {
    case SET_FOCUS:
      return Immutable.merge(state, {focusedId: action.id})

    default:
      return state
  }
}

export default cardDetails
