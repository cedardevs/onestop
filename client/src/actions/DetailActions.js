export const SET_FOCUS = 'SET_FOCUS'

export const CardStatus = {
  SHOW_FRONT: 'SHOW_FRONT',
  SHOW_BACK: 'SHOW_BACK'
}

export const setFocus = (id) => {
  return {
    type: SET_FOCUS,
    id: id
  }
}
