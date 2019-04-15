import Immutable from 'seamless-immutable'
import {
  CLEAR_CONFIG,
  SET_CONFIG,
  TOGGLE_FEATURES,
} from '../../actions/ConfigActions'
import {INIT_ANALYTICS} from '../../actions/AnalyticsActions'
export const initialState = Immutable({
  disclaimer: {},
  featured: [],
  featuresEnabled: [],
  headerDropdownMenuFeatureAvailable: false,
})

export const config = (state = initialState, action) => {
  switch (action.type) {
    case SET_CONFIG:
      return Immutable.merge(state, action.config)

    case CLEAR_CONFIG:
      return initialState

    case TOGGLE_FEATURES:
      return Immutable.merge(state, {
        featuresEnabled: action.featuresEnabled,
        headerDropdownMenuFeatureAvailable:
          action.headerDropdownMenuFeatureAvailable,
      })

    case INIT_ANALYTICS:
      return Immutable.merge(state, {
        analyticsInitiated: true,
      })
    default:
      return state
  }
}

export default config
