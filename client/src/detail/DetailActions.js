export const SET_FOCUS = 'SET_FOCUS'
export const SET_CARD_STATUS = 'SET_CARD_STATUS'

export const CardStatus = {
  SHOW_FRONT: 'SHOW_FRONT',
  SHOW_BACK: 'SHOW_BACK'
}

export const setCardStatus = (id) => {
  return {
    type: SET_CARD_STATUS,
    id: id
  }
}

export const setFocus = (id) => {
  return {
    type: SET_FOCUS,
    id: id
  }
}
