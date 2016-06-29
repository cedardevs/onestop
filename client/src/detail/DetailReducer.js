import Immutable from 'immutable'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {SET_CARD_STATUS, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

export const initialState = Immutable.Map({})

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      let newState = {}
      action.items.forEach(function (val, key) {
        newState[key] = {
          details: val,
          cardStatus: CardStatus.SHOW_FRONT
        }
      })
      return Immutable.fromJS(newState)
    case SET_CARD_STATUS:
      let cardStatus = state.getIn([action.id, 'cardStatus'])
      switch (cardStatus) {
        case CardStatus.SHOW_FRONT:
          return state.setIn([action.id, 'cardStatus'], CardStatus.SHOW_BACK )
        case CardStatus.SHOW_BACK:
        default:
          return state.setIn([action.id, 'cardStatus'], CardStatus.SHOW_FRONT )
      }
    default:
      return state
  }
}

export default details
