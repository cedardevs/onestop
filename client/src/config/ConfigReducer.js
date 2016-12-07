import Immutable from 'immutable'
import {CLEAR_CONFIG, SET_CONFIG} from './ConfigActions'

export const initialState = Immutable.fromJS({
  apiHost: '',
  banner: {
    message: null,
    colors: {
      text: 'white',
      background: 'red'
    }
  },
  granuleDetails: false
})

export const config = (state = initialState, action) => {
  switch (action.type) {
    case SET_CONFIG:
      return state.mergeDeep(action.config)

    case CLEAR_CONFIG:
      return initialState

    default:
      return state
  }
}

export default config
