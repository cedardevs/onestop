import Immutable from 'seamless-immutable'
import {SET_FOCUS} from '../../actions/FlowActions'

export const initialState = Immutable({
  focusedId: null,
})

// Note: the focusedId does not get unset when using the back button in the browser with the routing done by react-router, so we should not depend on it to identify if we are in a detail view
export const cardDetails = (state = initialState, action) => {
  switch (action.type) {
    case SET_FOCUS:
      return Immutable.merge(state, {focusedId: action.id})

    default:
      return state
  }
}

export default cardDetails
