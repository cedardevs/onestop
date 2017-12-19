import Immutable from 'seamless-immutable'
import {SET_API_BASE} from '../../actions/FlowActions'

export const initialState = Immutable({
  host: '',
  path: '/onestop/',
})

export const api = (state = initialState, action) => {
  switch (action.type) {
    case SET_API_BASE:
      return Immutable.merge(state, {path: action.path})

    default:
      return state
  }
}

export default api
