import fetch from 'isomorphic-fetch'

export const SET_INFO = 'set_info'
export const CLEAR_INFO = 'clear_info'
export const SET_TOTAL_COUNTS = 'set_total_counts'

export const setInfo = (info) => {
  return {
    type: SET_INFO,
    info: info
  }
}

export const clearInfo = () => {
  return {
    type: CLEAR_INFO
  }
}

export const setTotalCounts = (counts) => {
  return {
    type: SET_TOTAL_COUNTS,
    counts: counts
  }
}

export const fetchInfo = () => {
  return (dispatch, getState) => {
    const apiHost = getState().domain.info.apiHost || ''
    const url = apiHost + '/onestop/api/info'
    const params = {headers: {'Accept': 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then((json) => dispatch(setInfo(json)))
      .catch(error => console.debug('no info file available'))
  }
}

export const fetchCounts = () => {
  return (dispatch, getState) => {
    const apiHost = getState().domain.info.apiHost || ''
    const url = apiHost + '/onestop/api/search/totalCounts'
    const params = {headers: {'Accept': 'application/json'}}
    return fetch(url, params)
      .then(response => response.json())
      .then(json => parseCounts(json, dispatch))
      .catch(error => console.debug('OneStop total record counts unavailable'))
  }
}

const parseCounts = (json, dispatch) => {
  let counts = {
    collections: 0,
    granules: 0
  }

  json.data.forEach(e => {
    if (e.id === 'collection') {
      counts.collections = e.count
    }
    else if (e.id === 'granule') {
      counts.granules = e.count
    }
  })

  dispatch(setTotalCounts(counts))
}
