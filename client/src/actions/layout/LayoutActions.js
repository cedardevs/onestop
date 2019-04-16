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
console.log('open')

export const openRight = () => {
  return {
    type: TOGGLE_RIGHT_OPEN,
    open: true,
  }
}
export const closeRight = () => {
  console.log('close')
  return {
    type: TOGGLE_RIGHT_OPEN,
    open: false,
  }
}

export const TOGGLE_MAP = 'TOGGLE_MAP'
export const toggleMap = () => {
  return {
    type: TOGGLE_MAP,
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
