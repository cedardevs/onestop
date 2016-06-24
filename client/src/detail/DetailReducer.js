import { combineReducers } from 'redux'
import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from '../search/SearchActions'
import {FETCH_DETAILS, RECEIVE_DETAILS, FLIP_CARD, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

export const initialState = {
  id: null,
  details: null,
  cardStatus: CardStatus.SHOW_FRONT
}

export const details = (state = initialState, action) => {
  switch (action.type) {
    case FLIP_CARD:
      console.log("+++++++++++++++++++++flip card: " + action.id)
      if (state.get('id') == action.id){
        return state.merge({
          flipped:  !state.get('flipped')
        })
      }
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
