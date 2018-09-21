import fetch from 'isomorphic-fetch'

import {getApiPath} from '../reducers/domain/api'
import _ from 'lodash'

export const SET_CONFIG = 'set_config'
export const CLEAR_CONFIG = 'clear_config'
export const ENABLE_FEATURE = 'ENABLE_FEATURE'

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

export const enableFeature = featuresList => {
  const featureToggles = _.map(featuresList, toggle => toggle.featureName)
  const shoppingCartEnabled = featureToggles.includes('shoppingCart')
  return {
    type: ENABLE_FEATURE,
    shoppingCartEnabled: shoppingCartEnabled,
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
        dispatch(enableFeature(json.enabledFeatureToggles))
      })
      .catch(error => console.debug('no config file available'))
  }
}
