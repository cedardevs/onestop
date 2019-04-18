export const GET_USER_REQUEST = 'GET_USER_REQUEST'
export const GET_USER_SUCCESS = 'GET_USER_SUCCESS'
export const GET_USER_FAILURE = 'GET_USER_FAILURE'
export const LOGOUT_USER = 'LOGOUT_USER'

export const getUser = userEndpoint => {
  const requestOptions = {
    method: 'GET',
    redirect: 'error',
    mode: 'cors',
    credentials: 'include',
  }
  return dispatch => {
    dispatch({type: GET_USER_REQUEST})
    return fetch(userEndpoint, requestOptions)
      .then(response => response.json())
      .then(
        response => {
          dispatch({type: GET_USER_SUCCESS, payload: response})
        },
        error => {
          dispatch({type: GET_USER_FAILURE, error: error})
          throw error
        }
      )
  }
}

export const logoutUser = () => {
  return {type: LOGOUT_USER}
}
