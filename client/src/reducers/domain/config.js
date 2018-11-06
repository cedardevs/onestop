import Immutable from 'seamless-immutable'
import {
  CLEAR_CONFIG,
  SET_CONFIG,
  TOGGLE_FEATURES,
} from '../../actions/ConfigActions'

export const initialState = Immutable({
  banner: {},
  featured: [],
  featuresEnabled: [],
  headerDropdownMenuFeatureAvailable: false,
})

export const config = (state = initialState, action) => {
  switch (action.type) {
    case SET_CONFIG:
      // console.log("SET_CONFIG:action:", JSON.stringify(action, null , 3))
      return Immutable.merge(state, action.config)

    case CLEAR_CONFIG:
      return initialState

    case TOGGLE_FEATURES:
      // console.log("TOGGLE_FEATURES:action:", JSON.stringify(action, null , 3))
      return Immutable.merge(state, {
        featuresEnabled: action.featuresEnabled,
        headerDropdownMenuFeatureAvailable:
          action.headerDropdownMenuFeatureAvailable,
      })

    default:
      return state
  }
}

export default config
