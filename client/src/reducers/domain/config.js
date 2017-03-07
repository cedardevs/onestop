import Immutable from 'seamless-immutable'
import {CLEAR_CONFIG, SET_CONFIG} from '../../actions/ConfigActions'

export const initialState = Immutable({
  banner: {},
  apiHost: '',
  featured: []
})

export const config = (state = initialState, action) => {
  switch (action.type) {
    case SET_CONFIG:
      return Immutable.merge(state, action.config)

    case CLEAR_CONFIG:
      return initialState

    default:
      return state
  }
}

export default config
