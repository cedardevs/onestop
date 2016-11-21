import fetch from 'isomorphic-fetch'
import { push } from 'react-router-redux'
import { showLoading, hideLoading } from '../../loading/LoadingActions'
import { showErrors } from '../../error/ErrorActions'

export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const FETCHING_GRANULES = 'fetching_granules'
export const FETCHED_GRANULES = 'fetched_granules'
export const CLEAR_GRANULES = 'clear_granules'

export const toggleGranuleFocus = (granuleId) => {
  return {
    type: TOGGLE_GRANULE_FOCUS,
    id: granuleId
  }
}

export const clearGranules = () => {
  return {
    type: CLEAR_GRANULES
  }
}

export const fetchingGranules = () => {
  return {
    type: FETCHING_GRANULES
  }
}

export const fetchedGranules = (granuleList) => {
  return {
    type: FETCHED_GRANULES,
    granules: granuleList
  }
}

export const fetchGranules = () => {
  return (dispatch, getState) => {
    if (getState().getIn(['granules', 'inFlight'])) {
      return Promise.resolve() // let the calling code know there's nothing to wait for
    }

    let selectedCollections = getState().getIn(['collections', 'selectedIds']).toJS()
    if (!selectedCollections) {
      return Promise.resolve()
    }
    let searchBody = JSON.parse(getState().getIn(['search', 'requestBody']) || '{}')
    searchBody.filters = searchBody.filters || []
    searchBody.filters.push({"type": "collection", "values": selectedCollections})
    searchBody.facets = false
    searchBody = JSON.stringify(searchBody)

    const apiHost = getState().get('apiHost') || ''
    const apiRoot = apiHost + '/onestop/api/search'
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: searchBody
    }

    dispatch(showLoading())
    dispatch(fetchingGranules())

    return fetch(apiRoot, fetchParams)
        .then(response => {
          if (response.status < 200 || response.status >= 400) {
            var error = new Error(response.statusText)
            error.response = response
            throw error
          } else {
            return response
          }
        })
        .then(response => response.json())
        .then(json => {
          dispatch(fetchedGranules(json.data))
          dispatch(hideLoading())
          dispatch(push('results/granules'))
        })
        .catch(ajaxError => {
          ajaxError.response.json().then(errorJson => handleErrors(dispatch, errorJson))
        })
        .catch(jsError => {
          handleErrors(dispatch, jsError)
        })
  }
}

const handleErrors = (dispatch, e) => {
  dispatch(hideLoading())
  dispatch(showErrors(e.errors || e))
  dispatch(fetchedGranules([]))
}