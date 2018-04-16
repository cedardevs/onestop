export const SET_LEFT_OPEN_CLOSE = 'SET_LEFT_OPEN_CLOSE'
export const openLeft = () => {
  return {
    type: SET_LEFT_OPEN_CLOSE,
    value: true,
  }
}
export const closeLeft = () => {
  return {
    type: SET_LEFT_OPEN_CLOSE,
    value: false,
  }
}

export const TOGGLE_RIGHT = 'TOGGLE_RIGHT'
export const toggleRight = () => {
  return {
    type: TOGGLE_RIGHT,
  }
}

export const TOGGLE_MAP = 'TOGGLE_MAP'
export const toggleMap = () => {
  return {
    type: TOGGLE_MAP,
  }
}
