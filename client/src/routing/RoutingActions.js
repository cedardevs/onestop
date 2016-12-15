export const SET_OPERATION = 'SET_OPERATION'

export const setOperation = (path, params) => {
  console.log("this??")
  return {
    type: SET_OPERATION,
    path,
    params
  }
}
