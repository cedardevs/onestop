import fetch from 'isomorphic-fetch'

import {getApiPath} from '../reducers/domain/api'
import {HEADER_DROPDOWN_FEATURES} from '../utils/featureUtils'

import _ from 'lodash'

export const SET_CONFIG = 'set_config'
export const CLEAR_CONFIG = 'clear_config'
export const TOGGLE_FEATURES = 'TOGGLE_FEATURES'

export const setConfig = config => {
  return {
    type: SET_CONFIG,
    config: config,
  }
}

export const clearConfig = () => {
  return {
    type: CLEAR_CONFIG,
  }
}

export const toggleFeatures = featuresList => {
  const featuresEnabled = _.map(featuresList, toggle => toggle.featureName)
  const headerDropdownMenuFeatureAvailable = featuresEnabled.some(f =>
    HEADER_DROPDOWN_FEATURES.includes(f)
  )

  return {
    type: TOGGLE_FEATURES,
    featuresEnabled: featuresEnabled,
    headerDropdownMenuFeatureAvailable: headerDropdownMenuFeatureAvailable,
  }
}

export const fetchConfig = () => {
  return (dispatch, getState) => {
    const url = getApiPath(getState()) + '/uiConfig'
    const params = {headers: {Accept: 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then(json => {
        dispatch(setConfig(json))
        dispatch(toggleFeatures(json.enabledFeatureToggles))
      })
      .catch(error => console.debug('no config file available'))
  }
}
