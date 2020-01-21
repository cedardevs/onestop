import Immutable from 'seamless-immutable'
import {isDetailPage, isGranuleListPage} from '../utils/urlUtils'

import {
  TOGGLE_LEFT_OPEN,
  TOGGLE_RIGHT_OPEN,
  TOGGLE_MAP_OPEN,
  TOGGLE_MAP_CLOSE,
  SET_HEADER_MENU_OPEN,
  SHOW_GRANULE_VIDEO,
} from '../actions/LayoutActions'

import {LOCATION_CHANGE} from 'connected-react-router'

export const initialState = Immutable({
  showLeft: true,
  leftOpen: true,
  showRight: false,
  showMap: false,
  showAppliedFilterBubbles: false,
  onDetailPage: false,
  headerMenuOpen: false,
  granuleVideo: null,
})

export const layout = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      if (!action.payload) {
        return state
      }
      const path = action.payload.pathname
      const onDetailPage = isDetailPage(path)
      const onGranuleListPage = isGranuleListPage(path)
      const allowSearching = !(onDetailPage || onGranuleListPage)
      return Immutable.merge(state, {
        showLeft: allowSearching,
        showAppliedFilterBubbles: allowSearching,
        // onDetailPage: onDetailPage,
      })
    case TOGGLE_LEFT_OPEN:
      return Immutable.set(state, 'leftOpen', action.open)
    case TOGGLE_RIGHT_OPEN:
      return Immutable.set(state, 'rightOpen', action.open)
    case TOGGLE_MAP_OPEN:
      return Immutable.set(state, 'showMap', true)
    case TOGGLE_MAP_CLOSE:
      return Immutable.set(state, 'showMap', false)
    case SHOW_GRANULE_VIDEO:
      return Immutable.set(state, 'granuleVideo', action.granuleVideo)
    case SET_HEADER_MENU_OPEN:
      return Immutable.set(state, 'headerMenuOpen', action.value)
    default:
      return state
  }
}

export default layout
