import {push} from 'connected-react-router'

// synchronous actions
export const CLEAR_ERRORS = 'CLEAR_ERRORS'
export const clearErrors = () => {
  return {
    type: CLEAR_ERRORS,
  }
}

export const SET_ERRORS = 'SET_ERRORS'
export const setErrors = errors => {
  return {
    type: SET_ERRORS,
    errors,
  }
}

// composite actions
export const showErrors = errors => {
  return dispatch => {
    dispatch(setErrors(errors))
    dispatch(push('/error'))
  }
}
