// synchronous actions
export const TOGGLE_LEFT_OPEN = 'TOGGLE_LEFT_OPEN'
export const openLeft = () => {
  return {
    type: TOGGLE_LEFT_OPEN,
    open: true,
  }
}
export const closeLeft = () => {
  return {
    type: TOGGLE_LEFT_OPEN,
    open: false,
  }
}

export const TOGGLE_RIGHT_OPEN = 'TOGGLE_RIGHT_OPEN'
export const openRight = () => {
  return {
    type: TOGGLE_RIGHT_OPEN,
    open: true,
  }
}
export const closeRight = () => {
  return {
    type: TOGGLE_RIGHT_OPEN,
    open: false,
  }
}

export const TOGGLE_MAP_OPEN = 'TOGGLE_MAP_OPEN'
export const toggleMapOpen = () => {
  return {
    type: TOGGLE_MAP_OPEN,
  }
}
export const TOGGLE_MAP_CLOSE = 'TOGGLE_MAP_CLOSE'
export const toggleMapClose = () => {
  return {
    type: TOGGLE_MAP_CLOSE,
  }
}

export const SET_HEADER_MENU_OPEN = 'SET_HEADER_MENU_OPEN'
export const setHeaderMenuOpen = isOpen => {
  return {
    type: SET_HEADER_MENU_OPEN,
    value: isOpen,
  }
}

export const SHOW_GRANULE_VIDEO = 'SHOW_GRANULE_VIDEO'
export const showGranuleVideo = granuleId => {
  return {
    type: SHOW_GRANULE_VIDEO,
    granuleVideo: granuleId,
  }
}
