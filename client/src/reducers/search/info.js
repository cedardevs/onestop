import Immutable from 'seamless-immutable'
import {
  CLEAR_INFO,
  SET_INFO,
  SET_TOTAL_COUNTS,
} from '../../actions/fetch/InfoActions'

export const initialState = Immutable({
  version: '',
  collectionsCount: 0,
  granulesCount: 0,
})

export const info = (state = initialState, action) => {
  switch (action.type) {
    case SET_INFO:
      return Immutable.merge(state, action.info)

    case CLEAR_INFO:
      return initialState

    case SET_TOTAL_COUNTS:
      return Immutable.merge(state, {
        collectionsCount: action.counts.collections,
        granulesCount: action.counts.granules,
      })

    default:
      return state
  }
}

export default info
