import { Map } from 'immutable'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {SET_CARD_STATUS, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

export const initialState = Map({})

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      const newState = {}
      action.items.forEach(function (val, key) {
        newState[key] = {
          details: val,
          cardStatus: CardStatus.SHOW_FRONT
        }
      })
        
      const mapState = Map(newState)
      return mapState
    
    case SET_CARD_STATUS:
      var tempState = state.toJS()
      switch (tempState[action.id].cardStatus) {
        case CardStatus.SHOW_FRONT:
          tempState[action.id].cardStatus = CardStatus.SHOW_BACK
          return Map(tempState)
        case CardStatus.SHOW_BACK:
        default:
          tempState[action.id].cardStatus = CardStatus.SHOW_FRONT
          return Map(tempState)
      }
      
    default:
      return state
  }
}

export default details
