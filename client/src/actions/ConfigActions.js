import 'isomorphic-fetch'

import _ from 'lodash'
import {apiPath} from '../utils/urlUtils'
import {getUser} from './UserActions'
import {HEADER_DROPDOWN_FEATURES} from '../utils/featureUtils'

// synchronous actions
export const SET_CONFIG = 'set_config'
export const setConfig = config => {
  return {
    type: SET_CONFIG,
    config: config,
  }
}

export const CLEAR_CONFIG = 'clear_config'
export const clearConfig = () => {
  return {
    type: CLEAR_CONFIG,
  }
}

export const TOGGLE_FEATURES = 'TOGGLE_FEATURES'
export const toggleFeatures = features => {
  const headerDropdownMenuFeatureAvailable = features.some(f =>
    HEADER_DROPDOWN_FEATURES.includes(f)
  )
  return {
    type: TOGGLE_FEATURES,
    featuresEnabled: features,
    headerDropdownMenuFeatureAvailable: headerDropdownMenuFeatureAvailable,
  }
}

export const INIT_ANALYTICS = 'INIT_ANALYTICS'
export const initAnalytics = () => {
  return {
    type: INIT_ANALYTICS,
  }
}

// asynchronous actions
export const fetchConfig = () => {
  return dispatch => {
    const url = apiPath() + '/uiConfig'
    const params = {headers: {Accept: 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then(json => {
        dispatch(setConfig(json))
        if (json.auth) {
          dispatch(getUser(json.auth.userProfileEndpoint))
        }
        dispatch(toggleFeatures(json.features))
      })
      .catch(error => console.debug('no config file available'))
  }
}
