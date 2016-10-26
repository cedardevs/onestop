import Immutable from 'immutable'
import {CLEAR_ERRORS, SET_ERRORS} from './ErrorActions'

export const initialState = Immutable.List()

const errors = (state = initialState, action) => {
  switch (action.type) {
    case SET_ERRORS:
      return Immutable.fromJS(action.errors)

    case CLEAR_ERRORS:
      return initialState

    default:
      return state
  }
}

export default errors