import 'isomorphic-fetch'
import {apiPath} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'

const searchFetchParams = body => {
  return {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  }
}

const getFetchParams = {
  method: 'GET',
  headers: {
    Accept: 'application/json',
  },
}

export const fetchCollectionSearch = (body, successHandler, errorHandler) => {
  const endpoint = apiPath() + '/search/collection'
  return fetchIt(
    endpoint,
    searchFetchParams(body),
    successHandler,
    errorHandler
  )
}

export const fetchGranuleSearch = (body, successHandler, errorHandler) => {
  const endpoint = apiPath() + '/search/granule'
  return fetchIt(
    endpoint,
    searchFetchParams(body),
    successHandler,
    errorHandler
  )
}

export const fetchCollectionDetail = (id, successHandler, errorHandler) => {
  const endpoint = apiPath() + '/collection/' + id
  return fetchIt(endpoint, getFetchParams, successHandler, errorHandler)
}

const fetchIt = (endpoint, params, successHandler, errorHandler) => {
  return fetch(endpoint, params)
    .then(response => checkForErrors(response))
    .then(response => {
      return response.json()
    })
    .then(json => {
      successHandler(json)
    })
    .catch(ajaxError => {
      // TODO how to handle when ajaxError doesn't have response.json()...????
      if (ajaxError.response) {
        ajaxError.response.json().then(errorJson => errorHandler(errorJson))
      }
      errorHandler(ajaxError)
    })
    .catch(jsError => errorHandler(jsError))
}
