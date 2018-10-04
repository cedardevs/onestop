import Immutable from 'seamless-immutable'
import {
  CLEAR_CONFIG,
  SET_CONFIG,
  ENABLE_FEATURE,
} from '../../actions/ConfigActions'

export const initialState = Immutable({
  banner: {},
  featured: [],
  shoppingCartEnabled: false,
})

export const config = (state = initialState, action) => {
  switch (action.type) {
    case SET_CONFIG:
      return Immutable.merge(state, action.config)

    case CLEAR_CONFIG:
      return initialState

    case ENABLE_FEATURE:
      return Immutable.merge(state, {
        shoppingCartEnabled: action.shoppingCartEnabled,
      })

    default:
      return state
  }
}

export default config
