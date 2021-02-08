import Immutable from 'seamless-immutable'
import {
  USER_LOGGED_OUT,
  USER_PROFILE_FAILURE,
  USER_PROFILE_REQUEST,
  USER_PROFILE_SUCCESS,
} from '../actions/UserActions'
import {
  SAVED_SEARCH_FAILURE,
  SAVED_SEARCH_REQUEST,
  SAVED_SEARCH_SUCCESS,
} from '../actions/SavedSearchActions'
import {searchListToMap} from '../utils/resultUtils'

export const initialState = Immutable({
  isAuthenticated: false,
  profile: null,
  expired: false,
  isFetching: false,
  isFetchingSearches: false,
  searches: [],
})

export const user = (state = initialState, action) => {
  switch (action.type) {
    case USER_PROFILE_REQUEST:
      return state.setIn([ 'isFetching' ], true, action.item)

    case USER_PROFILE_SUCCESS:
      if (action.payload.data) {
        return state
          .setIn([ 'profile' ], action.payload.data)
          .setIn([ 'isFetching' ], false)
          .setIn([ 'isAuthenticated' ], true)
          .setIn([ 'expired' ], false)
      }
      else {
        return state.setIn([ 'isFetching' ], false).setIn([ 'expired' ], true)
      }

    case USER_PROFILE_FAILURE:
      return state
        .setIn([ 'error' ], action.error)
        .setIn([ 'isFetching' ], false)

    case USER_LOGGED_OUT:
      return state
        .setIn([ 'profile' ], {})
        .setIn([ 'expired' ], true)
        .setIn([ 'isAuthenticated' ], false)

    case SAVED_SEARCH_REQUEST:
      return state.setIn([ 'isFetchingSearches' ], true, action.item)

    case SAVED_SEARCH_SUCCESS:
      if (action.payload.data) {
        return state
          .setIn([ 'searches' ], searchListToMap(action.payload.data))
          .setIn([ 'isFetchingSearches' ], false)
      }

    case SAVED_SEARCH_FAILURE:
      return state
        .setIn([ 'error' ], action.error)
        .setIn([ 'isFetchingSearches' ], false)
    default:
      return state
  }
}

export default user
