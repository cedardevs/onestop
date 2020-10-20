export const SAVED_SEARCH_REQUEST = 'SAVED_SEARCH_REQUEST'
export const savedSearchRequest = () => {
  return {
    type: SAVED_SEARCH_REQUEST,
  }
}
export const SAVED_SEARCH_SUCCESS = 'SAVED_SEARCH_SUCCESS'
export const savedSearchSuccess = profile => {
  return {
    type: SAVED_SEARCH_SUCCESS,
    payload: profile,
  }
}
export const SAVED_SEARCH_FAILURE = 'SAVED_SEARCH_FAILURE'
export const savedSearchFailure = error => {
  return {
    type: SAVED_SEARCH_FAILURE,
    error: error,
  }
}

export const getSavedSearches = savedSearchEndpoint => {
  const requestOptions = {
    method: 'GET',
    redirect: 'error',
    mode: 'cors',
    credentials: 'include',
  }

  return dispatch => {
    dispatch(savedSearchRequest())

    return fetch(savedSearchEndpoint, requestOptions)
      .then(response => response.json())
      .then(
        response => {
          dispatch(savedSearchSuccess(response))
        },
        error => {
          dispatch(savedSearchFailure(error))
          throw error
        }
      )
  }
}

export const deleteSearch = (savedSearchEndpoint, id) => {
  const requestOptions = {
    method: 'DELETE',
    redirect: 'error',
    mode: 'cors',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }
  const endpoint = savedSearchEndpoint + '/' + id
  return dispatch => {
    dispatch(savedSearchRequest())

    return fetch(endpoint, requestOptions).then(
      response => {
        dispatch(savedSearchSuccess(response))
        dispatch(getSavedSearches(savedSearchEndpoint))
      },
      error => {
        dispatch(savedSearchFailure(error))
        throw error
      }
    )
  }
}

export const saveSearch = (
  savedSearchEndpoint,
  urlToSave,
  saveName,
  filter
) => {
  const requestOptions = {
    method: 'POST',
    redirect: 'error',
    mode: 'cors',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      value: urlToSave,
      name: saveName,
      filter: JSON.stringify(filter),
    }),
  }

  return dispatch => {
    dispatch(savedSearchRequest())

    return fetch(savedSearchEndpoint, requestOptions).then(
      response => {
        dispatch(savedSearchSuccess(response))
        dispatch(getSavedSearches(savedSearchEndpoint))
      },
      error => {
        dispatch(savedSearchFailure(error))
        throw error
      }
    )
  }
}
