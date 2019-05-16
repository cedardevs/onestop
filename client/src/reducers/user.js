import Immutable from 'seamless-immutable'
import {
  USER_PROFILE_REQUEST,
  USER_PROFILE_SUCCESS,
  USER_PROFILE_FAILURE,
  USER_LOGOUT,
} from '../actions/UserActions'

export const initialState = Immutable({
  isAuthenticated: false,
  profile: null,
  expired: false,
  isFetching: false,
})

export const user = (state = initialState, action) => {
  switch (action.type) {
    case USER_PROFILE_REQUEST:
      return state.setIn([ 'isFetching' ], true, action.item)

    case USER_PROFILE_SUCCESS:
      if (action.payload.email) {
        return state
          .setIn([ 'profile' ], action.profile)
          .setIn([ 'isFetching' ], false)
          .setIn([ 'isAuthenticated' ], true)
          .setIn([ 'expired' ], false)
      }
      else {
        return state.setIn([ 'expired' ], true)
      }

    case USER_PROFILE_FAILURE:
      return state
        .setIn([ 'error' ], action.error)
        .setIn([ 'isFetching' ], false)

    case USER_LOGOUT:
      return state
        .setIn([ 'profile' ], {})
        .setIn([ 'expired' ], true)
        .setIn([ 'isAuthenticated' ], false)
    default:
      return state
  }
}

export default user
