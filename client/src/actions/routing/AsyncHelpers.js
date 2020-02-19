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
  var controller = new AbortController()
  return [
    fetch(endpoint, {...params, signal: controller.signal})
      .then(response => checkForErrors(response))
      .then(response => {
        return response.json()
      })
      .then(json => {
        if (controller.signal.aborted) {
          // prevent setting results if this request has been aborted - they are no longer valid
          return
        }
        successHandler(json)
      })
      .catch(ajaxError => {
        if (controller.signal.aborted) {
          // do not call error handler to complete in-flight for aborted calls - there is no error, and the request is still in flight
          return
        }
        errorHandler(ajaxError)
        // }
      })
      .catch(jsError => {
        errorHandler(jsError)
      }),
    controller,
  ]
}
