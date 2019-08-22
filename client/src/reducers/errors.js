import Immutable from 'seamless-immutable'
import {CLEAR_ERRORS, SET_ERRORS} from '../actions/ErrorActions'

export const initialState = Immutable(new Set())

const errors = (state = initialState, action) => {
  switch (action.type) {
    case SET_ERRORS:
      return Immutable(action.errors)

    case CLEAR_ERRORS:
      return initialState

    default:
      return state
  }
}

export default errors
