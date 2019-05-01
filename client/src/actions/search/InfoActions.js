import 'isomorphic-fetch'
import {apiPath} from '../../utils/urlUtils'

// synchronous actions
export const SET_INFO = 'SET_INFO'
export const setInfo = info => {
  return {
    type: SET_INFO,
    info: info,
  }
}

export const CLEAR_INFO = 'CLEAR_INFO'
export const clearInfo = () => {
  return {
    type: CLEAR_INFO,
  }
}

export const SET_TOTAL_COUNTS = 'SET_TOTAL_COUNTS'
export const setTotalCounts = counts => {
  return {
    type: SET_TOTAL_COUNTS,
    counts: counts,
  }
}

// asynchronous actions
export const fetchInfo = () => {
  return dispatch => {
    const url = apiPath() + '/actuator/info'
    const params = {headers: {Accept: 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then(json => dispatch(setInfo(json)))
      .catch(error => console.debug('no info file available'))
  }
}

export const fetchCounts = () => {
  return dispatch => {
    const urlCollectionCounts = apiPath() + '/collection'
    const urlGranuleCounts = apiPath() + '/granule'
    const params = {headers: {Accept: 'application/json'}}
    const json = {}
    fetch(urlCollectionCounts, params)
      .then(responseCollection => responseCollection.json())
      .then(jsonCollection => {
        json.collections = jsonCollection.data[0].count
        return fetch(urlGranuleCounts, params)
      })
      .then(responseGranule => responseGranule.json())
      .then(jsonGranule => {
        json.granules = jsonGranule.data[0].count
        dispatch(setTotalCounts(json))
      })
      .catch(error => console.debug('OneStop total record counts unavailable'))
  }
}
