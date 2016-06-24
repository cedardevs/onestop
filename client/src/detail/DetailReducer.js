import { combineReducers } from 'redux'
import Immutable from 'immutable'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {FETCH_DETAILS, RECEIVE_DETAILS, SET_CARD_STATUS, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

export const initialState = {
  id: null,
  details: null,
  cardStatus: CardStatus.SHOW_FRONT
}

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      var cardMap = {}
      action.items.forEach(function(val,key){
        cardMap[key] = initialState
      })
      return Immutable.fromJS(cardMap)
    case SET_CARD_STATUS:
      console.log("+++++++++++++++++++++flip card: " + action.id)
      return state
    case FETCH_DETAILS:
      return Immutable.fromJS({id: action.id, flipped: false})
    case RECEIVE_DETAILS:
      return state.merge({
        details: action.details
      })
    default:
      return state
  }
}

export default details
