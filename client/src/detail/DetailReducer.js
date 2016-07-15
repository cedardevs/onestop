import Immutable from 'immutable'
import {SEARCH_COMPLETE} from '../search/SearchActions'
import {SET_FOCUS, SET_CARD_STATUS, CardStatus} from './DetailActions'

export const initialState = Immutable.Map({
  focusedId: null
})

export const details = (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_COMPLETE:
      let newState = {}
      action.items.forEach(function (val, key) {
        newState[key] = {
          title: val.title,
          thumbnail: val.thumbnail,
          description: val.description,
          cardStatus: CardStatus.SHOW_FRONT
        }
      })
      return Immutable.fromJS(newState)

    case SET_FOCUS:
      return state.merge({focusedId: action.id})

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
