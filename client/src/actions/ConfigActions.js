import fetch from 'isomorphic-fetch'

export const SET_CONFIG = 'set_config'
export const CLEAR_CONFIG = 'clear_config'

export const setConfig = (config) => {
  return {
    type: SET_CONFIG,
    config: config
  }
}

export const clearConfig = () => {
  return {
    type: CLEAR_CONFIG
  }
}

export const fetchConfig = () => {
  return (dispatch, getState) => {
    const apiHost = getState().domain.config.apiHost || ''
    const url = apiHost + '/onestop/api/search/uiConfig'
    const params = {headers: {'Accept': 'application/json'}}
    return fetch(url, params)
        .then(response => response.json())
        .then((json) => dispatch(setConfig(json)))
        .catch(error => console.debug('no config file available'))
  }
}
