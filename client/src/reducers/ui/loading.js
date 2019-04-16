import {
  LOADING_SHOW,
  LOADING_HIDE,
} from '../../actions/search/collections/FlowActions'

export const initialState = false

export const loading = (state = initialState, action) => {
  switch (action.type) {
    case LOADING_SHOW:
      return true

    case LOADING_HIDE:
      return false

    default:
      return state
  }
}

export default loading
