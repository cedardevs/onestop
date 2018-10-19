import Immutable from 'seamless-immutable'

export const initialState = Immutable({
  host: null,
  path: null,
})

export const api = (state = initialState, action) => {
  // This state is not modified by any actions. It exists to facilitate unit testing. TODO - in theory this can be removed by switching to fetch-mock
  return state
}

// SELECTORS
export const getApiPath = () => {
  return '/onestop/api'
}

export default api
