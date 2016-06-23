import { combineReducers } from 'redux'
import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE} from '../search/SearchActions'
import {FETCH_DETAILS, RECEIVE_DETAILS, FLIP_CARD, CardStatus} from './DetailActions'
const { SHOW_FRONT } = CardStatus

export const initialState = {
  id: null,
  details: null,
  flipped: false
}

export const card = (state = SHOW_FRONT, action) => {
  switch (action.type) {
    case FLIP_CARD:
      console.log("+++++++++++++++++++++flip card: " + action.id)
      if (state.get('id') == action.id){
        return state.merge({
          flipped:  !state.get('flipped')
        })
      }
    default:
      return state
  }
}

export const data = (state = initialState, action) => {
  switch (action.type) {
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

const details = combineReducers({
  card,
  data
})

export default details
