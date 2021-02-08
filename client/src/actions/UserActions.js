import {getSavedSearches} from './SavedSearchActions'

export const USER_PROFILE_REQUEST = 'USER_PROFILE_REQUEST'
export const userProfileRequest = () => {
  return {
    type: USER_PROFILE_REQUEST,
  }
}
export const USER_PROFILE_SUCCESS = 'USER_PROFILE_SUCCESS'
export const userProfileSuccess = profile => {
  return {
    type: USER_PROFILE_SUCCESS,
    payload: profile,
  }
}
export const USER_PROFILE_FAILURE = 'USER_PROFILE_FAILURE'
export const userProfileFailure = error => {
  return {
    type: USER_PROFILE_FAILURE,
    error: error,
  }
}
export const USER_LOGGED_OUT = 'USER_LOGGED_OUT'
export const loggedOut = () => {
  return {
    type: USER_LOGGED_OUT,
  }
}

export const getUser = (
  userProfileEndpoint,
  savedSearchEndpoint = undefined
) => {
  const requestOptions = {
    method: 'GET',
    redirect: 'error',
    mode: 'cors',
    credentials: 'include',
  }

  return dispatch => {
    // notify reducer of our intention to request user profile
    dispatch(userProfileRequest())

    // initiate asynchronous request to user profile endpoint
    return fetch(userProfileEndpoint, requestOptions)
      .then(response => response.json())
      .then(
        body => {
          if (body.hasOwnProperty('data')) {
            dispatch(userProfileSuccess(body))
            if (savedSearchEndpoint) {
              dispatch(getSavedSearches(savedSearchEndpoint))
            }
          }
          else {
            dispatch(userProfileFailure(body))
          }
        },
        error => {
          dispatch(userProfileFailure(error))
          throw error
        }
      )
  }
}

export const logoutUser = userLogoutEndpoint => {
  return dispatch => {
    return fetch(userLogoutEndpoint, {method: 'POST'}).then(response => {
      dispatch(loggedOut())
    })
  }
}
