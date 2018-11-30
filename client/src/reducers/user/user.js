import Immutable from 'seamless-immutable'
import {
  GET_USER_REQUEST,
  GET_USER_SUCCESS,
  GET_USER_FAILURE,
  LOGOUT_USER,
} from '../../actions/UserActions'

export const initialState = Immutable({
  isAuthenticated: false,
  info: null,
  expired: false,
  isFetching: false,
})

export const info = (state = initialState, action) => {
  switch (action.type) {
    case GET_USER_REQUEST:
      const isFetchingState = state.setIn([ 'isFetching' ], true, action.item)
      return isFetchingState

    case GET_USER_SUCCESS:
      if (action.payload.email) {
        const userState = state
          .setIn([ 'info' ], action.payload)
          .setIn([ 'isFetching' ], false)
          .setIn([ 'isAuthenticated' ], true)
          .setIn([ 'expired' ], false)
        return userState
      }
      else {
        return state.setIn([ 'expired' ], true)
      }

    case GET_USER_FAILURE:
      const userFailState = state
        .setIn([ 'error' ], action.error)
        .setIn([ 'isFetching' ], false)
      return userFailState

    case LOGOUT_USER:
      const userLogoutState = state
        .setIn([ 'info' ], {})
        .setIn([ 'expired' ], true)
        .setIn([ 'isAuthenticated' ], true)
      return userLogoutState
    default:
      return state
  }
}

export default info
