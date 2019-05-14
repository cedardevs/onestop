import 'isomorphic-fetch'
import {apiPath} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'

export const buildSearchAction = (
  endpointName,
  validRequestCheck,
  prefetchHandler,
  bodyBuilder,
  successHandler,
  errorHandler
) => {
  return (dispatch, getState) => {
    if (!validRequestCheck(getState())) {
      return Promise.resolve()
    }

    prefetchHandler(dispatch)

    const body = bodyBuilder(getState()) // call getState again, since prefetchHandler may change state, particularly if pagination is involved
    if (!body) {
      // cannot or should not fetch TODO throw somethign to reset inFlight!!!
      return Promise.resolve()
    }

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
  validRequestCheck,
  prefetchHandler,
  successHandler,
  errorHandler
) => {
  return (dispatch, getState) => {
    if (!validRequestCheck(getState())) {
      return Promise.resolve()
    }

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
