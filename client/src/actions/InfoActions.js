import 'isomorphic-fetch'
import {API_PATH} from '../utils/urlUtils'

// synchronous actions
export const SET_INFO = 'set_info'
export const setInfo = info => {
  return {
    type: SET_INFO,
    info: info,
  }
}

export const CLEAR_INFO = 'clear_info'
export const clearInfo = () => {
  return {
    type: CLEAR_INFO,
  }
}

export const SET_TOTAL_COUNTS = 'set_total_counts'
export const setTotalCounts = counts => {
  return {
    type: SET_TOTAL_COUNTS,
    counts: counts,
  }
}

// asynchronous actions
export const fetchInfo = () => {
  return (dispatch, getState) => {
    const url = API_PATH + '/actuator/info'
    const params = {headers: {Accept: 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then(json => dispatch(setInfo(json)))
      .catch(error => console.debug('no info file available'))
  }
}

export const fetchCounts = () => {
  return (dispatch, getState) => {
    const urlCollectionCounts = API_PATH + '/collection'
    const urlGranuleCounts = API_PATH + '/granule'
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
