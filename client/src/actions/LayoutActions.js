// export const TOGGLE_LEFT = 'TOGGLE_LEFT'
// export const toggleLeft = () => {
//   return {
//     type: TOGGLE_LEFT,
//   }
// }

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

export const SET_SHOW_LEFT = 'SET_SHOW_LEFT'
export const showLeft = () => {
  return {
    type: SET_SHOW_LEFT,
    value: true,
  }
}
export const hideLeft = () => {
  return {
    type: SET_SHOW_LEFT,
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
