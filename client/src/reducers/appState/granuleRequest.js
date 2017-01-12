import Immutable from 'seamless-immutable'
import { FETCHING_GRANULES, FETCHED_GRANULES, CLEAR_GRANULES }
  from '../../result/granules/GranulesActions'

export const initialState = Immutable({
  inFlight: false
})

export const granules = (state = initialState, action) => {
  switch(action.type) {
    case FETCHING_GRANULES:
      return Immutable.set(state, 'inFlight', true)

    case FETCHED_GRANULES:
      return Immutable.set(state, 'inFlight', false)

    default:
      return state
  }
}

export default granules
