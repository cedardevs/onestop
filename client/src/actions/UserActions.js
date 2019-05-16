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
export const USER_LOGOUT = 'USER_LOGOUT'

export const getUser = userProfileEndpoint => {
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
        response => {
          dispatch(userProfileSuccess(response))
        },
        error => {
          dispatch(userProfileFailure(error))
          throw error
        }
      )
  }
}

export const logoutUser = () => {
  return {type: USER_LOGOUT}
}
