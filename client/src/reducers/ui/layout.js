import Immutable from 'seamless-immutable'
import {isDetailPage, isGranuleListPage} from '../../utils/urlUtils'

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
  showAppliedFilterBubbles: false,
  onDetailPage: false,
})

export const layout = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      const path = action.payload.pathname
      const onDetailPage = isDetailPage(path)
      const onGranuleListPage = isGranuleListPage(path)
      const allowSearching = !(path.includes('508') || onDetailPage || onGranuleListPage)
      return Immutable.merge(state, {
        showLeft: allowSearching,
        showAppliedFilterBubbles: allowSearching,
        onDetailPage: onDetailPage,
      })
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
