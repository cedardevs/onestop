import {apiPath} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'

import {collectionClearResults} from './CollectionResultActions'
import {collectionUpdateFilters} from './CollectionFilterActions'

// TODO rename this file to FetchHelper or something?

export const buildSearchAction = (
  endpointName,
  bodyBuilder,
  prefetchHandler,
  successHandler,
  errorHandler
) => {
  return (dispatch, getState) => {
    let state = getState()

    const body = bodyBuilder(state)
    if (!body) {
      // cannot or should not fetch
      return Promise.resolve()
    }

    prefetchHandler(dispatch)

    const endpoint = apiPath() + '/search/' + endpointName

    const fetchParams = {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    }

    return fetch(endpoint, fetchParams)
      .then(response => checkForErrors(response))
      .then(response => {
        return response.json()
      })
      .then(json => successHandler(dispatch, json))
      .catch(ajaxError => {
        // TODO how to handle when ajaxError doesn't have response.json()...????
        if (ajaxError.response) {
          return ajaxError.response
            .json()
            .then(errorJson => errorHandler(dispatch, errorJson))
        }
        //return error
        errorHandler(dispatch, ajaxError)
        // : ajaxError
      })
      .catch(jsError => errorHandler(dispatch, jsError))
  }
}

export const buildGetAction = (
  endpointName,
  id,
  prefetchHandler,
  successHandler,
  errorHandler
) => {
  return dispatch => {
    prefetchHandler(dispatch)
    const endpoint = apiPath() + '/' + endpointName + '/' + id
    const fetchParams = {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
    }

    return fetch(endpoint, fetchParams)
      .then(response => checkForErrors(response))
      .then(responseChecked => responseChecked.json())
      .then(json => successHandler(dispatch, json))
      .catch(ajaxError =>
        ajaxError.response
          .json()
          .then(errorJson => errorHandler(dispatch, errorJson))
      )
      .catch(jsError => errorHandler(dispatch, jsError))
  }
}

export const buildSitemapAction = () => {
  return dispatch => {
    const endpoint = apiPath() + '/sitemap.xml'
    const fetchParams = {
      method: 'GET',
    }
    return (
      fetch(endpoint, fetchParams)
        .then(response => checkForErrors(response))
        // TODO: can we leverage dispatch here to use router like we are elsewhere instead of window.location.href?
        .then(response => (window.location.href = response.url))
    )
  }
}

export const showHome = history => {
  // TODO move this to ???
  return dispatch => {
    dispatch(collectionUpdateFilters())
    history.push('/')
    dispatch(collectionClearResults()) // TODO clear granule results here also
  }
}
