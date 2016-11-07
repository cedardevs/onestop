import { push } from 'react-router-redux'

export const CLEAR_ERRORS = 'CLEAR_ERRORS'
export const SET_ERRORS = 'SET_ERRORS'

export const clearErrors = (errors) => {
  return {
    type: CLEAR_ERRORS
  }
}

export const setErrors = (errors) => {
  return {
    type: SET_ERRORS,
    errors
  }
}

export const showErrors = (errors) => {
  return (dispatch, getState) => {
    dispatch(setErrors(errors))
    dispatch(push('error'))
  }
}