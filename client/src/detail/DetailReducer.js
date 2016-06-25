import { combineReducers } from 'redux'
import { Map } from 'immutable'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {FETCH_DETAILS, RECEIVE_DETAILS, SET_CARD_STATUS, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

const initialState = Map({})

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      const newState = {}
      action.items.forEach(function(val,key){
        newState[key] = {
          details: val,
          cardStatus: CardStatus.SHOW_FRONT
        }
      })
      const mapState = Map(newState)
      return mapState
    case SET_CARD_STATUS:
      console.log("State: " + state)
      var tempState = state.toJS()
      switch (tempState[action.id].cardStatus){
        case CardStatus.SHOW_FRONT:
          tempState[action.id].cardStatus = CardStatus.SHOW_BACK
          return Map(tempState)
        case CardStatus.SHOW_BACK:
        default:
          tempState[action.id].cardStatus = CardStatus.SHOW_FRONT
          return Map(tempState)
      }
    case FETCH_DETAILS:
      //return Immutable.fromJS({id: action.id, flipped: false})
      return state
    case RECEIVE_DETAILS:
      // return state.merge({
      //   details: action.details
      // })
      return state
    default:
      return state
  }
}

export default details
