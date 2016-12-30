import Immutable from 'seamless-immutable'
import _ from 'lodash'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {SET_FOCUS, SET_CARD_STATUS, CardStatus} from './DetailActions'

export const initialState = Immutable({
  focusedId: null
})

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      let newState = {focusedId: null}
      _.forOwn(action.items, (val, key) => {
        newState[key] = {
          title: val.title,
          thumbnail: val.thumbnail,
          description: val.description,
          cardStatus: CardStatus.SHOW_FRONT
        }
      })
      return Immutable(newState)

    case SET_FOCUS:
      return Immutable.merge(state, {focusedId: action.id})

    case SET_CARD_STATUS:
      let cardStatus = state[action.id].cardStatus
      switch (cardStatus) {
        case CardStatus.SHOW_FRONT:
          return Immutable.setIn(state, [action.id, 'cardStatus'], CardStatus.SHOW_BACK )
        case CardStatus.SHOW_BACK:
        default:
          return Immutable.setIn(state, [action.id, 'cardStatus'], CardStatus.SHOW_FRONT )
      }

    default:
      return state
  }
}

export default details
