import Immutable from 'seamless-immutable'
import {CLEAR_INFO, SET_INFO, SET_TOTAL_COUNTS} from '../../actions/InfoActions'

export const initialState = Immutable({
  version: '',
  collectionsCount: 0,
  granulesCount: 0,
})

export const info = (state = initialState, action) => {
  switch (action.type) {
    case SET_INFO:
      // console.log("SET_INFO:action:", JSON.stringify(action, null , 3))
      return Immutable.merge(state, action.info)

    case CLEAR_INFO:
      return initialState

    case SET_TOTAL_COUNTS:
      // console.log("SET_TOTAL_COUNTS:action:", JSON.stringify(action, null , 3))
      return Immutable.merge(state, {
        collectionsCount: action.counts.collections,
        granulesCount: action.counts.granules,
      })

    default:
      return state
  }
}

export default info
