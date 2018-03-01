import Immutable from 'seamless-immutable'
import _ from 'lodash'

import {
  SET_LEFT_OPEN_CLOSE,
  TOGGLE_RIGHT,
  TOGGLE_MAP,
} from '../../actions/LayoutActions'

import {LOCATION_CHANGE} from 'react-router-redux'

export const initialState = Immutable({
  showLeft: true,
  leftOpen: true,
  showRight: false,
  showMap: false,
})

export const layout = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      const path = action.payload.pathname
      const is508 =
        (_.startsWith(path, '/508/') && path !== '/508/') ||
        (_.startsWith(path, '508/') && path !== '508/')
      const detailIdRegex = /\/details\/([-\w]+)/
      const detailIdMatches = detailIdRegex.exec(path)
      const granuleListRegex = /\/granules\/([-\w]+)/
      const granuleIdMatches = granuleListRegex.exec(path)
      return Immutable.set(
        state,
        'showLeft',
        !(is508 || detailIdMatches || granuleIdMatches)
      )
    case SET_LEFT_OPEN_CLOSE:
      return Immutable.set(state, 'leftOpen', action.value)
    case TOGGLE_RIGHT:
      const previousShowRight = state.showLeft
      return Immutable.set(state, 'showRight', !previousShowRight)
    case TOGGLE_MAP:
      const previousShowMap = state.showMap
      return Immutable.set(state, 'showMap', !previousShowMap)
    default:
      return state
  }
}

export default layout
