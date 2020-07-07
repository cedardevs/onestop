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
    console.log('Fetching saved searches')
    // notify reducer of our intention to request user profile
    dispatch(savedSearchRequest())

    // initiate asynchronous request to user profile endpoint
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
