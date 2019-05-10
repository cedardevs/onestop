import {apiPath} from '../../utils/urlUtils'
import {checkForErrors} from '../../utils/responseUtils'

// TODO rename this file to FetchHelper or something?

export const buildSearchAction = (
  endpointName,
  validRequestCheck,
  prefetchHandler,
  bodyBuilder,
  successHandler,
  errorHandler
) => {
  return (dispatch, getState) => {
    // let state = getState()

    if (!validRequestCheck(getState())) {
      return Promise.resolve()
    }
    prefetchHandler(dispatch) // TODO does moving this above bodyBuilder break anything?

    const body = bodyBuilder(getState()) // prefetchHandler may change state, particularly if pagination is involved
    if (!body) {
      // cannot or should not fetch
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
  // TODO add Async to the name here?
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
  // TODO this is less a builder than a fetchSitemap
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
    // dispatch(collectionUpdateFilters()) // TODO reset filter state, results, etc (if needed) - note though that these are cleared when a new search is triggered regardless, so it's probably not important
    history.push('/')
  }
}
